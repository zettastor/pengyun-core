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

package py.processmanager.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.Before;
import org.junit.Test;
import py.processmanager.Pmdb;
import py.processmanager.exception.PmdbPathNotExist;

public class PmdbTest {
  private String testPath;

  @Before
  public void init() throws IOException {
    testPath = "/tmp/pmdb_testing";
    if (Files.exists(Paths.get(testPath))) {
      return;
    }
    Files.createDirectories(Paths.get(testPath));
  }

  @Test
  public void testSaveAndGet() throws PmdbPathNotExist {
    String testName = "Status";
    String testStr = "DIH 1.0.0-internal ACTIVE";
    Pmdb pmdb = Pmdb.build(Paths.get(testPath));
    pmdb.save(testName, testStr);
    String expectStr = pmdb.get(testName);
    assertEquals(expectStr, testStr);
  }

}
