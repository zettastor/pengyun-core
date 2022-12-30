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

package py.monitor.jmx.mbeans;

import py.monitor.pojo.management.PeriodicPojo;
import py.monitor.pojo.management.annotation.Description;
import py.monitor.pojo.management.annotation.MBean;
import py.monitor.pojo.management.annotation.ManagedAttribute;

@MBean(objectName = "dummy-mbean:name=dummyMbean")
@Description("dummy mbean for test case")
public class DummyMBean extends PeriodicPojo {
  public static String OBJECT_NAME = "dummy-mbean:name=dummyMbean";

  private String name;

  private String description;

  @ManagedAttribute
  public String getName() {
    return name;
  }

  @ManagedAttribute
  public void setName(String name) {
    this.name = name;
  }

  @ManagedAttribute
  public String getDescription() {
    return description;
  }

  @ManagedAttribute
  public void setDescription(String description) {
    this.description = description;
  }

  @Override
  protected void modifyData() throws Exception {
  }

}
