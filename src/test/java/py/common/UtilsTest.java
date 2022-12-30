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

package py.common;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.test.TestBase;

public class UtilsTest extends TestBase {
  private static final Logger logger = LoggerFactory.getLogger(UtilsTest.class);

  @Override
  public void init() throws Exception {
    super.init();
  }

  @Test
  public void testDeleteFileOrDirectory() throws Exception {
    final int breadth = 10;
    final int depth = 10;

    Queue<File> dirQueue;
    File dir;

    dir = new File("/tmp", getClass().getSimpleName());
    Assert.assertTrue(dir.mkdirs());

    dirQueue = new LinkedList<>();
    dirQueue.offer(dir);
    int depthIndex = 0;
    while (dirQueue.size() > 0 && depthIndex < depth) {
      dir = dirQueue.poll();
      for (int i = 0; i < breadth; i++) {
        File subDir;

        subDir = new File(dir, Integer.toString(i));
        Assert.assertTrue(subDir.mkdirs());

        dirQueue.offer(subDir);
      }

      ++depthIndex;
    }

    dir = new File("/tmp", getClass().getSimpleName());
    Utils.deleteDirectory(dir);

    Assert.assertFalse(dir.exists());
  }

  @Test
  public void testAnyExceptionOrAllDone() {
    CompletableFuture<Void> future1 = CompletableFuture.runAsync(() -> {
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    });
    CompletableFuture<Void> future2 = CompletableFuture.runAsync(() -> {
      try {
        Thread.sleep(1500);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    });
    Utils.anyExceptionOrAllDone(Arrays.asList(future1, future2), true).join();
  }
}
