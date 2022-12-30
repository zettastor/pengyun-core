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

package py.iet.file.mapper.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import py.iet.file.mapper.InitiatorsAllowFileMapper;
import py.test.TestBase;

public class InitiatorsAllowFileMapperTest extends TestBase {
  private InitiatorsAllowFileMapper fileMapper = new InitiatorsAllowFileMapper();

  @Before
  public void ini() {
    fileMapper.setFilePath("/tmp/InitiatorsAllowFileMapperTest");
  }

  @Test
  public void testInitiatorsAllowFileMapper() {
    Map<String, List<String>> initiatorTable = fileMapper.getInitiatorAllowTable();

    List<String> initiatorList = new ArrayList<String>();
    initiatorList.add("10.0.1.116");
    initiatorList.add("10.0.1.117");

    initiatorTable.put("iqn.test1", initiatorList);

    List<String> initiatorList1 = new ArrayList<String>();
    initiatorList1.add("10.0.1.118");
    initiatorTable.put("iqn.test2", initiatorList1);

    fileMapper.flush();

    initiatorTable.clear();

    fileMapper.load();
    initiatorTable = fileMapper.getInitiatorAllowTable();

    System.out.println(initiatorTable);
    Assert.assertTrue(initiatorTable.get("iqn.test1").size() == 2);
    Assert.assertTrue(initiatorTable.get("iqn.test1").contains("10.0.1.116"));
    Assert.assertTrue(initiatorTable.get("iqn.test1").contains("10.0.1.117"));

    Assert.assertTrue(initiatorTable.get("iqn.test2").size() == 1);
    Assert.assertTrue(initiatorTable.get("iqn.test2").contains("10.0.1.118"));

    initiatorTable.put("iqn.test1", null);
    initiatorTable.get("iqn.test2").clear();
    Assert.assertTrue(fileMapper.flush());
    fileMapper.load();
    initiatorTable = fileMapper.getInitiatorAllowTable();
    Assert.assertTrue(initiatorTable.isEmpty());
  }
}
