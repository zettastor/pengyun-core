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

package py.test;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.mockito.MockitoAnnotations;
import org.slf4j.LoggerFactory;
import py.common.RequestIdBuilder;

public class TestBase {
  public static final long DEFAULT_STORAGE_POOL_ID = RequestIdBuilder.get();
  protected static org.slf4j.Logger logger = LoggerFactory.getLogger(TestBase.class);
  @Rule
  public TestRule watcher = new TestWatcher() {
    protected void starting(Description description) {
      logger.warn("------------------------------------------------------------");
      logger.warn("Starting test: {}#{} ", getTestClassName(), description.getMethodName());
      logger.warn("-------------------------------------------------------------");
    }

    protected void finished(Description description) {
      logger.warn("------------------------------------------------------------");
      logger.warn("Finished test: {}#{} ", getTestClassName(), description.getMethodName());
      logger.warn("-------------------------------------------------------------");
    }
  };

  public TestBase() {
    MockitoAnnotations.initMocks(this);
  }

  @BeforeClass
  public static void initLogger() throws Exception {
    PatternLayout layout = new PatternLayout();
    String conversionPattern = "%-5p[%d][%t]%C(%L):%m%n";
    layout.setConversionPattern(conversionPattern);

    ConsoleAppender consoleAppender = new ConsoleAppender();
    consoleAppender.setLayout(layout);
    consoleAppender.setTarget("System.out");
    consoleAppender.setEncoding("UTF-8");
    consoleAppender.activateOptions();

    FileAppender fileAppender = new FileAppender(layout, "unit-test.log", false);

    Logger rootLogger = Logger.getRootLogger();
    rootLogger.setLevel(Level.DEBUG);
   
    rootLogger.removeAllAppenders();
    rootLogger.addAppender(consoleAppender);
    rootLogger.addAppender(fileAppender);

    Logger logger1 = Logger.getLogger("org.hibernate");
    logger1.setLevel(Level.WARN);

    Logger logger2 = Logger.getLogger("org.springframework");
    logger2.setLevel(Level.WARN);

    Logger logger3 = Logger.getLogger("com.opensymphony");
    logger3.setLevel(Level.WARN);

    Logger logger4 = Logger.getLogger("org.apache");
    logger4.setLevel(Level.DEBUG);

    Logger logger5 = Logger.getLogger("com.googlecode");
    logger5.setLevel(Level.DEBUG);

    Logger logger6 = Logger.getLogger("com.twitter.common.stats");
    logger6.setLevel(Level.WARN);

    Logger logger7 = Logger.getLogger("com.mchange");
    logger7.setLevel(Level.WARN);
  }

  public static Level getLogLevel() {
    return Logger.getRootLogger().getLevel();
  }

  public static void setLogLevel(Level level) {
    Logger.getRootLogger().setLevel(level);
  }

  public static void setLogLevel(String fileter, Level level) {
    Logger logger = Logger.getLogger(fileter);
    logger.setLevel(level);
  }

  public static String getRandomString(int length) {
    String base = "abcdefghijklmnopqrstuvwxyz0123456789";
    Random random = new Random();
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < length; i++) {
      int number = random.nextInt(base.length());
      sb.append(base.charAt(number));
    }
    return sb.toString();
  }

  public static Set<Long> buildIdSet(int count) {
    Set<Long> idSet = new HashSet<Long>();
    for (int i = 0; i < count; i++) {
      idSet.add(RequestIdBuilder.get());
    }
    return idSet;
  }

  public static List<Long> buildIdList(int count) {
    List<Long> idList = new ArrayList<Long>();
    for (int i = 0; i < count; i++) {
      idList.add(RequestIdBuilder.get());
    }
    return idList;
  }

  public static Multimap<Long, Long> buildMultiMap(int keyCount) {
    Multimap<Long, Long> buildMultiMap = Multimaps
        .synchronizedSetMultimap(HashMultimap.<Long, Long>create());
    for (int i = 0; i < keyCount; i++) {
      buildMultiMap.putAll(RequestIdBuilder.get(), buildIdSet(i));
    }
    return buildMultiMap;
  }

  public static void sleep(String description, long bound) {
    long counter = 0;
    while (counter < bound) {
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        logger.error("Caught an exception", e);
      } finally {
        counter += 1000;
        logger.debug("{},time remain: {} (seconds)", description, (bound - counter) / 1000);
      }
    }
  }

  public String getTestClassName() {
    return this.getClass().getName();
  }

  public void silence(Class clazz) {
  }

  public void silence(Level level, Class clazz) {
  }

  @Before
  public void init() throws Exception {
   
  }

  @After
  public void cleanUp() throws Exception {
  }
}
