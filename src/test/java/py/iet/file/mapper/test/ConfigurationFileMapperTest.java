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

import static junit.framework.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import py.iet.file.mapper.ConfigurationFileMapper;
import py.iet.file.mapper.ConfigurationFileMapper.Lun;
import py.iet.file.mapper.ConfigurationFileMapper.Target;
import py.test.TestBase;

public class ConfigurationFileMapperTest extends TestBase {
  private ConfigurationFileMapper mapper = new ConfigurationFileMapper();

  @Override
  @Before
  public void init() {
    mapper.setFilePath("/tmp/ConfigurationFileMapperTest");
  }

  @Test
  public void testConfigurationFileMapper() {
    Lun lun = new Lun();
    lun.setIndex(0);
    lun.setPath("/tmp/test");
    lun.setType("fileio");

    Target target = new Target();
    target.setTargetName("iqn.test");
    target.addToLunList(lun);

    mapper.addToTargetList(target);
    mapper.flush();

    mapper.load();

    assertTrue(mapper.getTargetList().size() == 1);
    assertTrue(mapper.getTargetList().get(0).getTargetName().equals(target.getTargetName()));
    assertTrue(mapper.getTargetList().get(0).getLunList().size() == 1);
    Lun lunAfterLoad = mapper.getTargetList().get(0).getLunList().get(0);
    assertTrue(lunAfterLoad.getIndex() == lunAfterLoad.getIndex());
    assertTrue(lunAfterLoad.getPath().equals(lunAfterLoad.getPath()));
    assertTrue(lunAfterLoad.getType().equals(lunAfterLoad.getType()));
  }
}
