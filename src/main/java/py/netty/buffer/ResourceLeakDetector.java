/*
 * Copyright 2013 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package py.netty.buffer;

import static io.netty.util.internal.StringUtil.EMPTY_STRING;
import static io.netty.util.internal.StringUtil.NEWLINE;
import static io.netty.util.internal.StringUtil.simpleClassName;

import io.netty.util.ResourceLeak;
import io.netty.util.ResourceLeakDetectorFactory;
import io.netty.util.ResourceLeakHint;
import io.netty.util.ResourceLeakTracker;
import io.netty.util.internal.EmptyArrays;
import io.netty.util.internal.PlatformDependent;
import io.netty.util.internal.SystemPropertyUtil;
import java.lang.ref.ReferenceQueue;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceLeakDetector<T> {
  static final int SAMPLING_INTERVAL;
  private static final String PROP_LEVEL_OLD = "io.netty.leakDetectionLevel";
  private static final String PROP_LEVEL = "io.netty.leakDetection.level";
  private static final Level DEFAULT_LEVEL = Level.SIMPLE;
  private static final String PROP_TARGET_RECORDS = "io.netty.leakDetection.targetRecords";
  private static final int DEFAULT_TARGET_RECORDS = 4;
  private static final String PROP_SAMPLING_INTERVAL = "io.netty.leakDetection.samplingInterval";

  private static final int DEFAULT_SAMPLING_INTERVAL = 128;
  private static final int TARGET_RECORDS;
  private static final Logger logger = LoggerFactory.getLogger(ResourceLeakDetector.class);
  private static final AtomicReference<String[]> excludedMethods =
      new AtomicReference<String[]>(EmptyArrays.EMPTY_STRINGS);
  private static Level level;

  static {
    final boolean disabled;
    if (SystemPropertyUtil.get("io.netty.noResourceLeakDetection") != null) {
      disabled = SystemPropertyUtil.getBoolean("io.netty.noResourceLeakDetection", false);
      logger.debug("-Dio.netty.noResourceLeakDetection: {}", disabled);
      logger.warn(
          "-Dio.netty.noResourceLeakDetection is deprecated. Use '-D{}={}' instead.",
          PROP_LEVEL, DEFAULT_LEVEL.name().toLowerCase());
    } else {
      disabled = false;
    }

    Level defaultLevel = disabled ? Level.DISABLED : DEFAULT_LEVEL;

    String levelStr = SystemPropertyUtil.get(PROP_LEVEL_OLD, defaultLevel.name());

    levelStr = SystemPropertyUtil.get(PROP_LEVEL, levelStr);
    Level level = Level.parseLevel(levelStr);

    TARGET_RECORDS = SystemPropertyUtil.getInt(PROP_TARGET_RECORDS, DEFAULT_TARGET_RECORDS);
    SAMPLING_INTERVAL = SystemPropertyUtil
        .getInt(PROP_SAMPLING_INTERVAL, DEFAULT_SAMPLING_INTERVAL);

    ResourceLeakDetector.level = level;
    if (logger.isDebugEnabled()) {
      logger.debug("-D{}: {}", PROP_LEVEL, level.name().toLowerCase());
      logger.debug("-D{}: {}", PROP_TARGET_RECORDS, TARGET_RECORDS);
    }
  }

  private final Set<DefaultResourceLeak<?>> allLeaks =
      Collections.newSetFromMap(new ConcurrentHashMap<DefaultResourceLeak<?>, Boolean>());
  private final ReferenceQueue<Object> refQueue = new ReferenceQueue<Object>();
  private final ConcurrentMap<String, Boolean> reportedLeaks = PlatformDependent
      .newConcurrentHashMap();
  private final String resourceType;
  private final int samplingInterval;

  @Deprecated
  public ResourceLeakDetector(Class<?> resourceType) {
    this(simpleClassName(resourceType));
  }

  @Deprecated
  public ResourceLeakDetector(String resourceType) {
    this(resourceType, DEFAULT_SAMPLING_INTERVAL, Long.MAX_VALUE);
  }

  @Deprecated
  public ResourceLeakDetector(Class<?> resourceType, int samplingInterval, long maxActive) {
    this(resourceType, samplingInterval);
  }

  @SuppressWarnings("deprecation")
  public ResourceLeakDetector(Class<?> resourceType, int samplingInterval) {
    this(simpleClassName(resourceType), samplingInterval, Long.MAX_VALUE);
  }

  @Deprecated
  public ResourceLeakDetector(String resourceType, int samplingInterval, long maxActive) {
    if (resourceType == null) {
      throw new NullPointerException("resourceType");
    }

    this.resourceType = resourceType;
    this.samplingInterval = samplingInterval;
  }

  public static boolean isEnabled() {
    return getLevel().ordinal() > Level.DISABLED.ordinal();
  }

  @Deprecated
  public static void setEnabled(boolean enabled) {
    setLevel(enabled ? Level.SIMPLE : Level.DISABLED);
  }

  public static Level getLevel() {
    return level;
  }

  public static void setLevel(Level level) {
    if (level == null) {
      throw new NullPointerException("level");
    }
    ResourceLeakDetector.level = level;
  }

  public static void addExclusions(Class clz, String... methodNames) {
    Set<String> nameSet = new HashSet<String>(Arrays.asList(methodNames));

    for (Method method : clz.getDeclaredMethods()) {
      if (nameSet.remove(method.getName()) && nameSet.isEmpty()) {
        break;
      }
    }
    if (!nameSet.isEmpty()) {
      throw new IllegalArgumentException("Can't find '" + nameSet + "' in " + clz.getName());
    }
    String[] oldMethods;
    String[] newMethods;
    do {
      oldMethods = excludedMethods.get();
      newMethods = Arrays.copyOf(oldMethods, oldMethods.length + 2 * methodNames.length);
      for (int i = 0; i < methodNames.length; i++) {
        newMethods[oldMethods.length + i * 2] = clz.getName();
        newMethods[oldMethods.length + i * 2 + 1] = methodNames[i];
      }
    } while (!excludedMethods.compareAndSet(oldMethods, newMethods));
  }

  @Deprecated
  public final ResourceLeak open(T obj) {
    return track0(obj);
  }

  @SuppressWarnings("unchecked")
  public final ResourceLeakTracker<T> track(T obj) {
    return track0(obj);
  }

  @SuppressWarnings("unchecked")
  private DefaultResourceLeak track0(T obj) {
    return new DefaultResourceLeak(obj, refQueue, allLeaks);
  }

  protected void reportTracedLeak(String resourceType, String records) {
    logger.error(
        "LEAK: {}.release() was not called before it's garbage-collected. "
            + "See http://netty.io/wiki/reference-counted-objects.html for more information.{}",
        resourceType, records);
  }

  protected void reportUntracedLeak(String resourceType) {
    logger.error("LEAK: {}.release() was not called before it's garbage-collected. "
            + "Enable advanced leak reporting to find out where the leak occurred. "
            + "To enable advanced leak reporting, "
            + "specify the JVM option '-D{}={}' or call {}.setLevel() "
            + "See http://netty.io/wiki/reference-counted-objects.html for more information.",
        resourceType, PROP_LEVEL, Level.ADVANCED.name().toLowerCase(), simpleClassName(this));
  }

  @Deprecated
  protected void reportInstancesLeak(String resourceType) {
  }

  public enum Level {
    DISABLED,

    SIMPLE,

    ADVANCED,

    PARANOID;

    static Level parseLevel(String levelStr) {
      String trimmedLevelStr = levelStr.trim();
      for (Level l : values()) {
        if (trimmedLevelStr.equalsIgnoreCase(l.name()) || trimmedLevelStr
            .equals(String.valueOf(l.ordinal()))) {
          return l;
        }
      }
      return DEFAULT_LEVEL;
    }
  }

  @SuppressWarnings("deprecation")
  private static final class DefaultResourceLeak<T> implements ResourceLeakTracker<T>,
      ResourceLeak {
    @SuppressWarnings("unchecked")
    private static final AtomicReferenceFieldUpdater<DefaultResourceLeak<?>, Record> headUpdater =
        (AtomicReferenceFieldUpdater)
            AtomicReferenceFieldUpdater.newUpdater(DefaultResourceLeak.class, Record.class, "head");

    @SuppressWarnings("unchecked")
    private static final AtomicIntegerFieldUpdater<DefaultResourceLeak<?>> droppedRecordsUpdater =
        (AtomicIntegerFieldUpdater)
            AtomicIntegerFieldUpdater.newUpdater(DefaultResourceLeak.class, "droppedRecords");
    private final int trackedHash;
    @SuppressWarnings("unused")
    private volatile Record head;
    @SuppressWarnings("unused")
    private volatile int droppedRecords;

    DefaultResourceLeak(
        Object referent,
        ReferenceQueue<Object> refQueue,
        Set<DefaultResourceLeak<?>> allLeaks) {
      trackedHash = System.identityHashCode(referent);
      logger.debug("created an object {} id {} ", referent, trackedHash, new Throwable());

    }

    @Override
    public void record() {
      record0(null);
    }

    @Override
    public void record(Object hint) {
      record0(hint);
    }

    @Override
    public boolean close() {
      logger.debug("id {}", trackedHash, new Throwable());
      return true;
    }

    @Override
    public boolean close(T trackedObject) {
      assert trackedHash == System.identityHashCode(trackedObject);

      logger.debug("The tractedObject {} is closed, id {}", trackedObject, trackedHash,
          new Throwable());
      return true;
    }

    private void record0(Object hint) {
      logger.debug("buf {}, id {}", hint, trackedHash, new Throwable());
    }

    @Override
    public String toString() {
      Record oldHead = headUpdater.getAndSet(this, null);
      if (oldHead == null) {
        return EMPTY_STRING;
      }

      final int dropped = droppedRecordsUpdater.get(this);
      int duped = 0;

      int present = oldHead.pos + 1;

      StringBuilder buf = new StringBuilder(present * 2048).append(NEWLINE);
      buf.append("Recent access records: ").append(NEWLINE);

      int i = 1;
      Set<String> seen = new HashSet<String>(present);
      for (; oldHead != Record.BOTTOM; oldHead = oldHead.next) {
        String s = oldHead.toString();
        if (seen.add(s)) {
          if (oldHead.next == Record.BOTTOM) {
            buf.append("Created at:").append(NEWLINE).append(s);
          } else {
            buf.append('#').append(i++).append(':').append(NEWLINE).append(s);
          }
        } else {
          duped++;
        }
      }

      if (duped > 0) {
        buf.append(": ")
            .append(duped)
            .append(" leak records were discarded because they were duplicates")
            .append(NEWLINE);
      }

      if (dropped > 0) {
        buf.append(": ")
            .append(dropped)
            .append(" leak records were discarded because the leak record count is targeted to ")
            .append(TARGET_RECORDS)
            .append(". Use system property ")
            .append(PROP_TARGET_RECORDS)
            .append(" to increase the limit.")
            .append(NEWLINE);
      }

      buf.setLength(buf.length() - NEWLINE.length());
      return buf.toString();
    }
  }

  private static final class Record extends Throwable {
    private static final long serialVersionUID = 6065153674892850720L;

    private static final Record BOTTOM = new Record();

    private final String hintString;
    private final Record next;
    private final int pos;

    Record(Record next, Object hint) {
      hintString = hint instanceof ResourceLeakHint ? ((ResourceLeakHint) hint).toHintString()
          : hint.toString();
      this.next = next;
      this.pos = next.pos + 1;
    }

    Record(Record next) {
      hintString = null;
      this.next = next;
      this.pos = next.pos + 1;
    }

    private Record() {
      hintString = null;
      next = null;
      pos = -1;
    }

    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(2048);
      if (hintString != null) {
        buf.append("\tHint: ").append(hintString).append(NEWLINE);
      }

      StackTraceElement[] array = getStackTrace();

      out:
      for (int i = 3; i < array.length; i++) {
        StackTraceElement element = array[i];

        String[] exclusions = excludedMethods.get();
        for (int k = 0; k < exclusions.length; k += 2) {
          if (exclusions[k].equals(element.getClassName())
              && exclusions[k + 1].equals(element.getMethodName())) {
            continue out;
          }
        }

        buf.append('\t');
        buf.append(element.toString());
        buf.append(NEWLINE);
      }
      return buf.toString();
    }
  }
}
