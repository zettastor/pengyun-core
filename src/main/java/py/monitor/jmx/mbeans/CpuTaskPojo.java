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
