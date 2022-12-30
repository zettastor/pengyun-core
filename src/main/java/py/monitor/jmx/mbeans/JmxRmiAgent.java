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

import java.rmi.server.RemoteServer;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.monitor.customizable.repository.AttributeMetadata;
import py.monitor.jmx.server.JmxAgent;
import py.monitor.jmx.server.ResourceRelatedAttribute;
import py.monitor.task.TimeSpan;

public class JmxRmiAgent extends RemoteServer implements JmxRmiAgentMBean {
  private static final long serialVersionUID = 5388357648827099514L;
  private static final Logger logger = LoggerFactory.getLogger(JmxRmiAgent.class);
  private JmxAgent agent;

  @Override
  public String createSubTask(long parentTaskId, long period, List<TimeSpan> runTime,
      List<ResourceRelatedAttribute> resourceReleatedAttribute) throws Exception {
    if (agent == null) {
      throw new Exception();
    }
    logger.debug("Going to create sub-task of performance task from monitor-center");

    return agent.addPerformanceReporter(parentTaskId, period, runTime, resourceReleatedAttribute);
  }

  @Override
  public void destroySubTask(String objectName) throws Exception {
    if (agent == null) {
      throw new Exception();
    }
    logger.debug("Going to destroy sub-task of performance task from monitor-center");
    agent.removePerformanceReporter(objectName);
  }

  @Override
  public Set<AttributeMetadata> getMbeans(Set<String> domains) throws Exception {
    if (agent == null) {
      throw new Exception();
    }
    logger.debug("Going to get all mbeans from domain : {}", domains);
    return agent.getMbeans(domains);
  }

  public JmxAgent getAgent() {
    return agent;
  }

  public void setAgent(JmxAgent agent) {
    this.agent = agent;
  }

}
