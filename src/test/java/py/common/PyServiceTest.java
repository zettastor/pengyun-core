
package py.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.test.TestBase;

public class PyServiceTest extends TestBase {
  private static final Logger logger = LoggerFactory.getLogger(PyServiceTest.class);

  @Test
  public void testChecker() throws Exception {
    assertTrue(PyService.is(PyService.CONSOLE.getServiceName()).legal());
    assertFalse(PyService.is("wrong service name").legal());
  }

  @Test
  public void testFindValueByName() {
    assertEquals(PyService.DIH, PyService.findValueByName("DIH"));
    assertEquals(PyService.CONSOLE, PyService.findValueByName("CONSOLE"));
    assertEquals(PyService.COORDINATOR, PyService.findValueByName("COORDINATOR"));
    assertEquals(PyService.DATANODE, PyService.findValueByName("DATANODE"));
    assertEquals(PyService.DRIVERCONTAINER, PyService.findValueByName("DRIVERCONTAINER"));
    assertEquals(PyService.INFOCENTER, PyService.findValueByName("INFOCENTER"));
    //assertEquals(PyService.SYSTEMDAEMON, PyService.findValueByName("SYSTEMDAEMON"));
    assertEquals(null, PyService.findValueByName("Not existed service name"));
  }

}
