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

package py.app;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@Configuration
@PropertySource("classpath:config/network.properties")
public class NetworkConfiguration {
  @Value("${enable.data.depart.from.control:false}")
  private boolean enableDataDepartFromControl = false;

  @Value("${control.flow.subnet:10.0.1.0/24}")
  private String controlFlowSubnet = "10.0.1.0/24";

  @Value("${data.flow.subnet:10.0.1.0/24}")
  private String dataFlowSubnet = "10.0.1.0/24";

  @Value("${monitor.flow.subnet:10.0.1.0/24}")
  private String monitorFlowSubnet = "10.0.1.0/24";

  @Value("${outward.flow.subnet:10.0.1.0/24}")
  private String outwardFlowSubnet = "10.0.1.0/24";

  @Bean
  public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
    return new PropertySourcesPlaceholderConfigurer();
  }

  public String getControlFlowSubnet() {
    return controlFlowSubnet;
  }

  public void setControlFlowSubnet(String controlFlowSubnet) {
    this.controlFlowSubnet = controlFlowSubnet;
  }

  public boolean isEnableDataDepartFromControl() {
    return enableDataDepartFromControl;
  }

  public void setEnableDataDepartFromControl(boolean enableDataDepartFromControl) {
    this.enableDataDepartFromControl = enableDataDepartFromControl;
  }

  public String getDataFlowSubnet() {
    return dataFlowSubnet;
  }

  public void setDataFlowSubnet(String dataFlowSubnet) {
    this.dataFlowSubnet = dataFlowSubnet;
  }

  public String getMonitorFlowSubnet() {
    return monitorFlowSubnet;
  }

  public void setMonitorFlowSubnet(String monitorFlowSubnet) {
    this.monitorFlowSubnet = monitorFlowSubnet;
  }

  public String getOutwardFlowSubnet() {
    return outwardFlowSubnet;
  }

  public void setOutwardFlowSubnet(String outwardFlowSubnet) {
    this.outwardFlowSubnet = outwardFlowSubnet;
  }
}
