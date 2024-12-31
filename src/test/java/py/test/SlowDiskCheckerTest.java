/**
* Copyright (C) 2013-2024 Nanjing Pengyun Network Technology Co., Ltd.
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
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
