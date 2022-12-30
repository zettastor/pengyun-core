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
