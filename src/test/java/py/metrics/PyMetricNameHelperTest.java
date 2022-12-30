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

package py.metrics;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.monitor.exception.UnsupportedIdentifierTypeException;
import py.monitor.jmx.server.ResourceType;
import py.test.TestBase;

public class PyMetricNameHelperTest extends TestBase {
  private static final Logger logger = LoggerFactory.getLogger(PyMetricNameHelperTest.class);

  @Test
  public void testGenerateIdentifier() throws UnsupportedIdentifierTypeException {
    String testStr = "xxxx";
    PyMetricNameHelper<Long> identifier = new PyMetricNameHelper<Long>(testStr, ResourceType.VOLUME,
        1234L);
    String temp = identifier.generate();
    logger.debug(temp);
    Assert.assertEquals(
        testStr + "." + PyMetricNameHelper.getMetricsIdentifierPrefix() + "VOLUME"
            + PyMetricNameHelper.getSeperator() + "1234" + PyMetricNameHelper
            .getMetricsIdentifierPostfix(),
        temp);

    PyMetricNameHelper<String> identifier1 = new PyMetricNameHelper<String>(testStr,
        ResourceType.VOLUME, "2345");
    temp = identifier1.generate();
    logger.debug(temp);
    Assert.assertEquals(
        testStr + "." + PyMetricNameHelper.getMetricsIdentifierPrefix() + "VOLUME"
            + PyMetricNameHelper.getSeperator() + "2345" + PyMetricNameHelper
            .getMetricsIdentifierPostfix(),
        temp);
  }

  @Test
  public void testFormatter_long() {
    try {
      String testStr = "test." + PyMetricNameHelper.getMetricsIdentifierPrefix() + "VOLUME"
          + PyMetricNameHelper.getSeperator() + "1234" + PyMetricNameHelper
          .getMetricsIdentifierPostfix();

      logger.debug("testing string expression is {}", testStr);
      PyMetricNameHelper<Long> identifier = new PyMetricNameHelper<Long>(Long.class, testStr);
      logger.debug("indentifier is {}", identifier);

      Assert.assertEquals(new PyMetricNameHelper<Long>("test", ResourceType.VOLUME, 1234L),
          identifier);
    } catch (Exception e) {
      logger.error("Caught an exception", e);
      Assert.fail();
    }
  }

  @Test
  public void testFormatter_string() {
    try {
      String testStr = "test." + PyMetricNameHelper.getMetricsIdentifierPrefix() + "VOLUME"
          + PyMetricNameHelper.getSeperator() + "1234" + PyMetricNameHelper
          .getMetricsIdentifierPostfix();

      logger.debug("testing string expression is {}", testStr);
      PyMetricNameHelper<String> identifier = new PyMetricNameHelper<String>(String.class, testStr);
      logger.debug("indentifier is {}", identifier);

      Assert.assertEquals(new PyMetricNameHelper<String>("test", ResourceType.VOLUME, "1234"),
          identifier);
    } catch (Exception e) {
      logger.error("Caught an exception", e);
      Assert.fail();
    }
  }

  @Test
  public void testFormatter_others() {
    try {
      String testStr = "test." + PyMetricNameHelper.getMetricsIdentifierPrefix() + "VOLUME"
          + PyMetricNameHelper.getSeperator() + "1234" + PyMetricNameHelper
          .getMetricsIdentifierPostfix()
          + "asdfasdf";

      logger.debug("testing string expression is {}", testStr);
      PyMetricNameHelper<Integer> identifier = new PyMetricNameHelper<Integer>(Integer.class,
          testStr);
      Assert.fail();
    } catch (UnsupportedIdentifierTypeException e) {
      logger.debug("Caught expected exception", e);
      Assert.assertTrue(true);
    } catch (Exception e) {
      logger.error("Caught unexpected exception", e);
      Assert.fail();
    }
  }

  @Test
  public void testFormatter_specialCharactors() {
    try {
      String metricsName = "r@!!@#$$%^#%o*&vider=pe<_)(n?g.";
      String testStr = metricsName + PyMetricNameHelper.getNameSeperator()
          + PyMetricNameHelper.getMetricsIdentifierPrefix() + "VOLUME" + PyMetricNameHelper
          .getSeperator()
          + "1234" + PyMetricNameHelper.getMetricsIdentifierPostfix();
      logger.debug("testing string expression is {}", testStr);
      PyMetricNameHelper<String> identifier = new PyMetricNameHelper<String>(String.class, testStr);
      logger.debug("identifier is {}", identifier);

      PyMetricNameHelper<String> temp = new PyMetricNameHelper<String>(metricsName,
          ResourceType.VOLUME, "1234");
      logger.debug("----------------->{}", temp);
      Assert.assertEquals(temp, identifier);

      logger.debug("\n================================");

      testStr = metricsName + ".__<<VOLUME:1234>>__";
      logger.debug("testing string expression is {}", testStr);
      identifier = new PyMetricNameHelper<String>(String.class, testStr);
      logger.debug("identifier is {}", identifier);
      Assert
          .assertNotEquals(new PyMetricNameHelper<String>(metricsName, ResourceType.VOLUME, "1234"),
              identifier);

      logger.debug("\n================================");

      testStr = metricsName + ".__<<VOLUME:1234>>__";
      logger.debug("testing string expression is {}", testStr);
      identifier = new PyMetricNameHelper<String>(String.class, testStr);
      logger.debug("identifier is {}", identifier);
      Assert
          .assertNotEquals(new PyMetricNameHelper<String>(metricsName, ResourceType.VOLUME, "1234"),
              identifier);

      logger.debug("\n================================");

      testStr = metricsName;
      logger.debug("testing string expression is {}", testStr);
      identifier = new PyMetricNameHelper<String>(String.class, testStr);
      logger.debug("identifier is {}", identifier);
    } catch (Exception e) {
      logger.error("Caught unexpected exception", e);
      Assert.fail();
    }
  }

  @Test
  public void testForRealExample() {
   
    try {
      String testStr = "pojo-agent-JVM:name=CPUTask.__<<TYPE[MACHINE] ID[10.0.1.132]>>";

      logger.debug("testing string expression is {}", testStr);
      PyMetricNameHelper<String> identifier = new PyMetricNameHelper<String>(String.class, testStr);
      logger.debug("indentifier is {}", identifier);

      Assert.assertEquals(
          new PyMetricNameHelper<String>("pojo-agent-JVM:name=CPUTask", ResourceType.MACHINE,
              "10.0.1.132"),
          identifier);
    } catch (Exception e) {
      logger.error("Caught an exception", e);
      Assert.fail();
    }
  }
}
