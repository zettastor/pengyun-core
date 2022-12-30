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
