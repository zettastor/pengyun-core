
package py.test;

import java.util.Random;
import java.util.UUID;
import org.junit.Test;

public class UuidTest extends TestBase {
  @Test
  public void uuidPerformance() throws Exception {
    Thread.sleep(5000);
    long t0 = System.currentTimeMillis();
    for (int i = 0; i < 1000000; i++) {
      UUID.randomUUID();
    }
    System.out.println("uuid=" + (System.currentTimeMillis() - t0));
    Thread.sleep(5000);
  }

  @Test
  public void randomPerformance() throws Exception {
    Thread.sleep(5000);
    long t0 = System.currentTimeMillis();
    Random r = new Random();
    for (int i = 0; i < 1000000; i++) {
      new UUID(r.nextLong(), r.nextLong());
    }
    System.out.println("random=" + (System.currentTimeMillis() - t0));
    Thread.sleep(5000);
  }
}
