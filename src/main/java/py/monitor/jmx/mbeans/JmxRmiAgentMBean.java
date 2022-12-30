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

import java.util.List;
import java.util.Set;
import py.monitor.customizable.repository.AttributeMetadata;
import py.monitor.jmx.server.ResourceRelatedAttribute;
import py.monitor.task.TimeSpan;

public interface JmxRmiAgentMBean {
  public static final String performanceDomain = "PerformanceReporter";
  public static final String ReporterCreaterName = "real-mbean:name=MBeanCreator";

  public String createSubTask(long parentTaskId, long period, List<TimeSpan> runTime,
      List<ResourceRelatedAttribute> resourceRelatedAttribute) throws Exception;

  public void destroySubTask(String objectName) throws Exception;

  public Set<AttributeMetadata> getMbeans(Set<String> domains) throws Exception;
}
