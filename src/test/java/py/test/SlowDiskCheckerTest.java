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

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.storage.performance.IoStatElement;
import py.storage.performance.PyIoStat;
import py.storage.performance.SlowDiskChecker;
import py.storage.performance.SlowDiskCheckerImpl;

public class SlowDiskCheckerTest extends TestBase {
  private static final Logger logger = LoggerFactory.getLogger(SlowDiskCheckerTest.class);

  @Test
  public void testBasic() throws Exception {
    PyIoStat pyioStat = Mockito.mock(PyIoStat.class);
    IoStatElement element = new IoStatElement();
    SlowDiskChecker slowDiskChecker = new SlowDiskCheckerImpl(pyioStat);

    slowDiskChecker.incomingIo();

    Mockito.when(pyioStat
        .readDiskStat(ArgumentMatchers.any(String.class), ArgumentMatchers.any(String.class)))
        .thenReturn(element);

    Mockito.when(pyioStat.getIoAwait(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(901L);

    String fileName = "/proc/diskstats";
    boolean isSlowDisk = slowDiskChecker.checking(fileName, "sda", 900);
    assertTrue(isSlowDisk == false);

    slowDiskChecker.incomingIo();
    isSlowDisk = slowDiskChecker.checking(fileName, "sda", 900);
    assertTrue(isSlowDisk == false);

    slowDiskChecker.incomingIo();
    isSlowDisk = slowDiskChecker.checking(fileName, "sda", 900);
    assertTrue(isSlowDisk == true);

  }

  @Test
  public void testNoIoIncoming() throws Exception {
    PyIoStat pyioStat = Mockito.mock(PyIoStat.class);
    IoStatElement element = new IoStatElement();
    SlowDiskChecker slowDiskChecker = new SlowDiskCheckerImpl(pyioStat);

    slowDiskChecker.incomingIo();

    Mockito.when(pyioStat
        .readDiskStat(ArgumentMatchers.any(String.class), ArgumentMatchers.any(String.class)))
        .thenReturn(element);

    Mockito.when(pyioStat.getIoAwait(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(901L);

    String fileName = "/proc/diskstats";
    boolean isSlowDisk = slowDiskChecker.checking(fileName, "sda", 900);
    assertTrue(isSlowDisk == false);

    isSlowDisk = slowDiskChecker.checking(fileName, "sda", 900);
    assertTrue(isSlowDisk == false);

    slowDiskChecker.incomingIo();
    isSlowDisk = slowDiskChecker.checking(fileName, "sda", 900);
    assertTrue(isSlowDisk == false);

    slowDiskChecker.incomingIo();
    isSlowDisk = slowDiskChecker.checking(fileName, "sda", 900);
    assertTrue(isSlowDisk == true);

  }

  @Test
  public void awaitLessThanThreshold() throws Exception {
    PyIoStat pyioStat = Mockito.mock(PyIoStat.class);
    IoStatElement element = new IoStatElement();
    SlowDiskChecker slowDiskChecker = new SlowDiskCheckerImpl(pyioStat);

    slowDiskChecker.incomingIo();

    Mockito.when(pyioStat
        .readDiskStat(ArgumentMatchers.any(String.class), ArgumentMatchers.any(String.class)))
        .thenReturn(element);

    Mockito.when(pyioStat.getIoAwait(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(901L);
    String fileName = "/proc/diskstats";
    boolean isSlowDisk = slowDiskChecker.checking(fileName, "sda", 900);
    assertTrue(isSlowDisk == false);

    logger.warn("io await return 900");
    Mockito.when(pyioStat.getIoAwait(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(900L);
    slowDiskChecker.incomingIo();
    isSlowDisk = slowDiskChecker.checking(fileName, "sda", 900);
    assertTrue(isSlowDisk == false);

    logger.warn("io await return 901");
    Mockito.when(pyioStat.getIoAwait(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(901L);

    slowDiskChecker.incomingIo();
    isSlowDisk = slowDiskChecker.checking(fileName, "sda", 900);
    assertTrue(isSlowDisk == false);

    slowDiskChecker.incomingIo();
    isSlowDisk = slowDiskChecker.checking(fileName, "sda", 900);
    assertTrue(isSlowDisk == false);

    slowDiskChecker.incomingIo();
    isSlowDisk = slowDiskChecker.checking(fileName, "sda", 900);
    assertTrue(isSlowDisk == true);
  }

}
