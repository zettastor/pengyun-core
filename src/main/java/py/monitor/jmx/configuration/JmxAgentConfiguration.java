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

package py.monitor.jmx.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@Configuration
@PropertySource({"classpath:config/jmxagent.properties"})
public class JmxAgentConfiguration {
  @Value("${jmx.agent.port}")
  private int jmxAgentPort;

  @Value("${jmx.agent.switcher}")
  private String jmxAgentSwitcher;

  @Bean
  public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
    return new PropertySourcesPlaceholderConfigurer();
  }

  public int getJmxAgentPort() {
    return jmxAgentPort;
  }

  public void setJmxAgentPort(int jmxAgentPort) {
    this.jmxAgentPort = jmxAgentPort;
  }

  public String getJmxAgentSwitcher() {
    return jmxAgentSwitcher;
  }

  public void setJmxAgentSwitcher(String jmxAgentSwitcher) {
    this.jmxAgentSwitcher = jmxAgentSwitcher;
  }
}
