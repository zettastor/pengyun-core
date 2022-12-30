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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Level;
import org.junit.Ignore;
import org.junit.Test;
import py.storage.performance.IoStatElement;
import py.storage.performance.PyIoStat;
import py.storage.performance.PyIoStatImpl;

@Ignore
public class PyIoStatImlTest extends TestBase {
  @Test
  public void testBasic() throws Exception {
    PyIoStat pyioStat = new PyIoStatImpl();

    IoStatElement element = pyioStat.readDiskStat("/proc/diskstats", "sda");
    assertTrue(element.getReadIos() > 0);
    assertTrue(element.getReadTicks() > 0);

    assertTrue(element.getWriteIos() > 0);
    assertTrue(element.getWriteTicks() > 0);

    assertTrue(element.getTotalTicks() > 0);
    assertTrue(element.getRequestTicks() > 0);

    element = pyioStat.readDiskStat("/proc/diskstats", "sdx");
    assertTrue(element == null);
  }

  @Test
  public void testAwait() throws Exception {
    PyIoStat pyioStat = new PyIoStatImpl();

    IoStatElement elementPrevious = new IoStatElement();

    IoStatElement elementNow = new IoStatElement();

    long await = pyioStat.getIoAwait(elementPrevious, elementNow);
    assertEquals(await, 0);

    elementNow.setReadIos(2);
    elementNow.setReadSectors(2);
    elementNow.setReadTicks(20);
    elementNow.setWriteIos(3);
    elementNow.setWriteSectors(3);
    elementNow.setWriteTicks(30);

    await = pyioStat.getIoAwait(elementPrevious, elementNow);
    assertEquals(await, 0);

    elementPrevious.setReadIos(1);
    elementPrevious.setReadSectors(1);
    elementPrevious.setReadTicks(10);
    elementPrevious.setWriteIos(2);
    elementPrevious.setWriteSectors(2);
    elementPrevious.setWriteTicks(20);

    await = pyioStat.getIoAwait(elementPrevious, elementNow);
    assertEquals(await, 10);

  }

  @Test
  public void correctTest() throws Exception {
    String diskFile = "/tmp/diskstats";
    ProcessBuilder processBuilder = new ProcessBuilder();
    List<String> cmdList = new ArrayList<>();
    String[] commands = new String[4];
    commands[0] = "bash";
    commands[1] = "-c";
    commands[2] = "echo \"8 245 sxc  1  2 3 4 5 6 7 8 9 10 11\" > " + diskFile + "\n";
    commands[3] = "echo \"8 234 sda 11 22 33 44 55 66 77 88 99 100 111\" >>" + diskFile;

    Runtime.getRuntime().exec(commands);

    commands[2] = "echo \"8 234 sda 11 22 33 44 55 66 77 88 99 100 111\" >>" + diskFile;
    Runtime.getRuntime().exec(commands);

    PyIoStat pyioStat = new PyIoStatImpl();
    IoStatElement element = pyioStat.readDiskStat(diskFile, "sxc");
    assertTrue(element != null);

    assertEquals(element.getReadIos(), 1);
    assertEquals(element.getReadMerges(), 2);
    assertEquals(element.getReadSectors(), 3);
    assertEquals(element.getReadTicks(), 4);

  }

  @Test
  public void performanceTest() throws Exception {
    setLogLevel(Level.WARN);
    long timeBegin = System.currentTimeMillis();

    PyIoStat pyioStat = new PyIoStatImpl();
    for (int i = 0; i < 10000; i++) {
      IoStatElement element = pyioStat.readDiskStat("/proc/diskstats", "sda");
      assertTrue(element.getReadIos() > 0);
      assertTrue(element.getReadTicks() > 0);

      assertTrue(element.getWriteIos() > 0);
      assertTrue(element.getWriteTicks() > 0);

      assertTrue(element.getTotalTicks() > 0);
      assertTrue(element.getRequestTicks() > 0);

    }

    long timeStop = System.currentTimeMillis();

    logger.warn("cost:{}", timeStop - timeBegin);
  }
}
