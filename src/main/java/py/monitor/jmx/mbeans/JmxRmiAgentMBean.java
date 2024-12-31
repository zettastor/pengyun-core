

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
