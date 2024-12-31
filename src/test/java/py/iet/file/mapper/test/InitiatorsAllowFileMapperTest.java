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
