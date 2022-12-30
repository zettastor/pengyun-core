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
