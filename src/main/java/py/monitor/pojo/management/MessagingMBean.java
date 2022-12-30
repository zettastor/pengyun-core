/*
 * Copyright (c) 2022-2022. PengYunNetWork
 *
 * This program is free software: you can use, redistribute, and/or modify it
 * under the terms of the GNU Affero General Public License, version 3 or later ("AGPL"),
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 *  You should have received a copy of the GNU Affero General Public License along with
 *  this program. If not, see <http://www.gnu.org/licenses/>.
 */

package py.monitor.pojo.management;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.xml.datatype.XMLGregorianCalendar;
import py.monitor.pojo.management.annotation.Description;
import py.monitor.pojo.management.annotation.MBean;
import py.monitor.pojo.management.annotation.ManagedAttribute;
import py.monitor.pojo.management.annotation.ManagedOperation;
import py.monitor.pojo.management.annotation.ManagedOperation.Impact;
import py.monitor.pojo.management.exception.ManagementException;

@MBean(objectName = "py.monitor.aop.pojo:type=py.monitor.aop.pojo.MessagingMBean,name=Default")
@Description("Generic MBean for monitoring input/output processing")
public class MessagingMBean extends AbstractMBean {
  private AtomicLong inputCount;
  private AtomicLong inputLatest;

  private AtomicLong outputCount;
  private AtomicLong outputLatest;

  private TimeUnit durationUnit;
  private AtomicLong durationLatest;
  private AtomicLong durationTotal;
  private AtomicLong durationMax;
  private AtomicLong durationMin;

  private AtomicLong failedCount;
  private AtomicLong failedLatest;
  private Throwable failedLatestCause;

  public MessagingMBean() throws MalformedObjectNameException {
    super();
  }

  public MessagingMBean(String name) throws MalformedObjectNameException {
    super(name);
  }

  public MessagingMBean(ObjectName objectName) {
    super(objectName);
  }

  public synchronized void notifyInput() {
    inputCount.incrementAndGet();
    inputLatest.set(now());
  }

  public synchronized void notifyOutput() {
    long latest = inputLatest.get();
    if (latest == NONE) {
      notifyOutput(-1);
    } else {
      notifyOutput(now() - latest);
    }
  }

  @Deprecated
  public synchronized void notifyOutput(long durationMillis) {
    notifyOutput(durationMillis, TimeUnit.MILLISECONDS);
  }

  public void notifyOutput(long inDuration, TimeUnit inUnit) {
    long workDuration = durationUnit.convert(inDuration, inUnit);
    outputLatest.set(now());
    outputCount.incrementAndGet();

    if (workDuration >= 0) {
      durationLatest.set(workDuration);
      durationTotal.addAndGet(workDuration);

      Long minMillis = getDurationMin();
      if ((minMillis == null) || (minMillis != null && workDuration < minMillis)) {
        durationMin.set(workDuration);
      }

      Long maxMillis = getDurationMax();
      if ((maxMillis == null) || (workDuration > maxMillis)) {
        durationMax.set(workDuration);
      }
    }
  }

  public synchronized void notifyFailed() {
    notifyFailed(null);
  }

  public synchronized void notifyFailed(Throwable cause) {
    failedCount.incrementAndGet();
    failedLatest.set(now());
    failedLatestCause = cause;
  }

  public void reregisterMonitor() throws ManagementException {
    registration.unregister();
    registration.register();
  }

  @Override
  @ManagedOperation(Impact.ACTION)
  @Description("Reset this MBean's metrics")
  public synchronized void resetMbean() {
    super.resetMbean();
    inputLatest = none();
    outputLatest = none();
    failedLatest = none();
    failedLatestCause = null;
    inputCount = zero();
    outputCount = zero();
    failedCount = zero();
    durationUnit = TimeUnit.MILLISECONDS;
    durationLatest = none();
    durationMin = none();
    durationMax = none();
    durationTotal = zero();
  }

  @ManagedAttribute
  @Description("Number of messages received")
  public long getInputCount() {
    return inputCount.get();
  }

  @ManagedAttribute
  @Description("Time of last received message")
  public XMLGregorianCalendar getInputLatest() {
    return date(noneAsNull(inputLatest));
  }

  @ManagedAttribute
  @Description("Time since latest received message (seconds)")
  public Long getInputLatestAgeSeconds() {
    return age(noneAsNull(inputLatest), SECONDS);
  }

  @ManagedAttribute
  @Description("Number of processed messages")
  public long getOutputCount() {
    return outputCount.get();
  }

  @ManagedAttribute
  @Description("Time of the latest processed message")
  public XMLGregorianCalendar getOutputLatest() {
    return date(noneAsNull(outputLatest));
  }

  @ManagedAttribute
  @Description("Time since latest processed message (seconds)")
  public Long getOutputLatestAgeSeconds() {
    return age(noneAsNull(outputLatest), SECONDS);
  }

  @ManagedAttribute
  public String getDuration() {
    return durationUnit.toString();
  }

  @ManagedAttribute
  @Description("The time unit of the reported durations."
      + "One of: NANOSECONDS, MICROSECONDS, MILLISECONDS, SECONDS, MINUTES, HOURS, DAY. "
      + "May be abbreviated.")
  public void setDuration(final String durationName) {
    final String trimName = (durationName != null) ? durationName.trim() : null;
    if (durationName == null || trimName.isEmpty()) {
      throw new IllegalArgumentException("Empty value not allowed");
    }
    for (TimeUnit unit : TimeUnit.values()) {
      if (unit.toString().toUpperCase().startsWith(trimName.toUpperCase())) {
        setDurationUnit(unit);
        return;
      }
    }
    throw new IllegalArgumentException("Unknown time unit: '" + trimName + "'");
  }

  public TimeUnit getDurationUnit() {
    return durationUnit;
  }

  public void setDurationUnit(TimeUnit durationUnit) {
    this.durationUnit = durationUnit;
  }

  @ManagedAttribute
  @Description("Processing time of the latest message (ms)")
  public Long getDurationLatest() {
    return noneAsNull(durationLatest);
  }

  @ManagedAttribute
  @Description("Total processing time of all messages (ms)")
  public long getDurationTotal() {
    return durationTotal.get();
  }

  @ManagedAttribute
  @Description("Average processing time (ms)")
  public Long getDurationAverage() {
    long processedDurationTotalMillis = getDurationTotal();
    long processedCount = getOutputCount();
    return (processedCount != 0) ? processedDurationTotalMillis / processedCount : null;
  }

  @ManagedAttribute
  @Description("Min processing time (ms)")
  public Long getDurationMin() {
    return noneAsNull(durationMin);
  }

  @ManagedAttribute
  @Description("Max processing time (ms)")
  public Long getDurationMax() {
    return noneAsNull(durationMax);
  }

  @ManagedAttribute
  @Description("Number of processes that failed")
  public long getFailedCount() {
    return failedCount.get();
  }

  @ManagedAttribute
  @Description("Time of the latest failed message processing")
  public XMLGregorianCalendar getFailedLatest() {
    return date(noneAsNull(failedLatest));
  }

  @ManagedAttribute
  @Description("Time since latest failed message processing (seconds)")
  public Long getFailedLatestAgeSeconds() {
    return age(noneAsNull(failedLatest), SECONDS);
  }

  @ManagedAttribute
  @Description("The failure reason of the latest failed message processing")
  public String getFailedLatestReason() {
    if (failedLatestCause == null) {
      return null;
    }
    return failedLatestCause.toString();
  }

  @ManagedAttribute
  @Description("The failure stacktrace of the latest failed message processing "
      + "(one line per element)")
  public String[] getFailedLatestStacktrace() {
    if (failedLatestCause == null) {
      return null;
    }
    StringWriter sw = new StringWriter();
    failedLatestCause.printStackTrace(new PrintWriter(sw));
    ArrayList<String> lines = new ArrayList<String>(1000);
    BufferedReader reader = new BufferedReader(new StringReader(sw.toString()));
    String line;
    try {
      while ((line = reader.readLine()) != null) {
        lines.add(line);
      }
    } catch (IOException ignore) {
      // nothing
    }
    return lines.toArray(new String[lines.size()]);
  }
}
