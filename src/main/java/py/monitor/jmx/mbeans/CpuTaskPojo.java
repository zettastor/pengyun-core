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

import static py.monitor.pojo.management.annotation.MBean.AutomaticType.ATTRIBUTE;
import static py.monitor.pojo.management.annotation.MBean.AutomaticType.OPERATION;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import javax.management.Attribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.monitor.jmx.server.ResourceType;
import py.monitor.pojo.management.PeriodicPojo;
import py.monitor.pojo.management.annotation.Description;
import py.monitor.pojo.management.annotation.MBean;
import py.monitor.pojo.management.annotation.ManagedAttribute;

@MBean(objectName = "pojo-agent-JVM:name=CPUTask", resourceType = ResourceType.JVM, automatic = {
    ATTRIBUTE,
    OPERATION})
@Description("CPU MBean timer task")
public class CpuTaskPojo extends PeriodicPojo {
  private static final Logger logger = LoggerFactory.getLogger(CpuTaskPojo.class);

  private double cpuUsage;

  @Description(value = "Cpu usage")
  @ManagedAttribute(range = "[0,1]", unitOfMeasurement = "percentage(%)")
  public double getCpuUsage() {
    return cpuUsage;
  }

  @ManagedAttribute
  public void setCpuUsage(double cpuUsage) {
    this.cpuUsage = cpuUsage;
  }

  @Override
  protected void modifyData() throws Exception {
    OperatingSystemMXBean operatingSystemInfo = ManagementFactory.getOperatingSystemMXBean();
    this.getRealBean()
        .setAttribute(new Attribute("cpuUsage", operatingSystemInfo.getSystemLoadAverage()));
  }
}
