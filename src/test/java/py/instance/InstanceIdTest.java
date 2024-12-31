

package py.instance;

import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import py.test.TestBase;

public class InstanceIdTest extends TestBase {
  @Test
  public void testMapJson() throws Exception {
    InstanceId ii = new InstanceId();
    ObjectMapper mapper = new ObjectMapper();
    String str = mapper.writeValueAsString(ii);
    logger.warn(str);

    InstanceId newInstanceId = mapper.readValue(str, InstanceId.class);
    assertTrue(ii.equals(newInstanceId));
  }
}
