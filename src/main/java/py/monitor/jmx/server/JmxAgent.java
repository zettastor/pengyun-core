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

package py.monitor.jmx.server;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.UUID;
import javax.management.BadAttributeValueExpException;
import javax.management.BadBinaryOpValueExpException;
import javax.management.BadStringOperationException;
import javax.management.InvalidApplicationException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.QueryExp;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.app.context.AppContext;
import py.common.PyService;
import py.common.RequestIdBuilder;
import py.instance.PortType;
import py.metrics.PyMetricNameHelper;
import py.monitor.customizable.repository.AttributeMetadata;
import py.monitor.exception.NotExistedException;
import py.monitor.jmx.mbeans.CpuTaskPojo;
import py.monitor.jmx.mbeans.JmxRmiAgent;
import py.monitor.jmx.mbeans.JmxRmiAgentMBean;
import py.monitor.jmx.mbeans.MemoryTaskPojo;
import py.monitor.jmx.mbeans.RealMachineInfomation;
import py.monitor.pojo.management.PeriodicPojo;
import py.monitor.pojo.management.helper.DescriptionEnDecoder;
import py.monitor.task.TimeSpan;
import py.monitor.utils.Service;

public class JmxAgent extends Service {
  private static final Logger logger = LoggerFactory.getLogger(JmxAgent.class);
  private static final String perfomanceReporterPrefix = "PerformanceReporter:name=";
  private static final String CONNECTOR_ADDRESS
      = "com.sun.management.jmxremote.localConnectorAddress";
  private static final String registryName = "jmxrmi";

  private final List<PeriodicReporter> performanceReporters = new ArrayList<PeriodicReporter>();

  private final List<Collector> collectors = new ArrayList<Collector>();
  private AppContext appContext;

  private List<String> alertRules;

  private List<List<String>> performanceTaskContext;

  private Timer timer;

  private JMXConnectorServer jmxConnectorServer;

  private JmxRmiAgent mbeanCreator;

  private AlarmReporter alarmReporter;

  private MBeanServer mbeanServer;
  private Registry registry;
  private boolean hasBeenRegisted = false;

  private boolean jmxSwitcher = true;

  private JmxAgent() {
    super();
  }

  public static JmxAgent getInstance() {
    return LazyHolder.singletonInstance;
  }

  public String addPerformanceReporter(long parentTaskId, long period, List<TimeSpan> runTime,
      List<ResourceRelatedAttribute> attributes) throws Exception {
    logger.debug("period : {}, runTime : {}, attributes : {}", period, runTime, attributes);
    if (period < 1 || runTime.size() == 0 || attributes.size() == 0) {
      logger.error("Illegal parmeters. parameters are: {},{},{}", period, runTime, attributes);
      throw new Exception();
    }

    logger.debug("AppContext: {}", appContext);
    String objectName = perfomanceReporterPrefix + UUID.randomUUID().toString();
    PeriodicReporter performanceReporter = PeriodicReporter.createBy(parentTaskId, attributes)
        .endpoint(appContext.getMainEndPoint()).objectName(objectName).bindMbeanServer(mbeanServer)
        .bindTimer(timer).period(period).build();
    performanceReporter.start();
    performanceReporters.add(performanceReporter);

    return objectName;
  }

  public void removePerformanceReporter(String objectName) throws NotExistedException, Exception {
    boolean found = false;
    for (PeriodicReporter reporter : performanceReporters) {
      String objectNameInMbeanServer = reporter.getRegistration().getMbeanObjectName()
          .getCanonicalName();
      logger.debug("object name in mbean server is : {}", objectNameInMbeanServer);

      if (objectName.equals(objectNameInMbeanServer)) {
        reporter.stop();
        performanceReporters.remove(reporter);
        found = true;
        break;
      }
    }

    if (!found) {
      logger.warn("performance sub-task mbean not existed");
      throw new NotExistedException();
    }
  }

  public Set<AttributeMetadata> getMbeans(Set<String> domains) throws Exception {
    Set<AttributeMetadata> attributes = new HashSet<AttributeMetadata>();
    logger.debug("All observed domains are : {}. AppContext is {}", domains, appContext);
    for (String domain : domains) {
      logger.debug("Current domain is : {}", domain);
      Set<ObjectInstance> objectInstances = mbeanServer.queryMBeans(new ObjectName(domain + ":*"),
          new QueryExp() {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean apply(ObjectName name)
                throws BadStringOperationException, BadBinaryOpValueExpException,
                BadAttributeValueExpException, InvalidApplicationException {
              return true;
            }

            @Override
            public void setMBeanServer(MBeanServer s) {
            }
          });

      logger.debug("objectInstances size is {}", objectInstances.size());
      for (ObjectInstance objectInstance : objectInstances) {
        MBeanInfo mbeanInfo = mbeanServer.getMBeanInfo(objectInstance.getObjectName());
        MBeanAttributeInfo[] attributeInfo = mbeanInfo.getAttributes();
        for (int i = 0; i < attributeInfo.length; ++i) {
          PyMetricNameHelper<String> resourceIdentify = new PyMetricNameHelper<String>(String.class,
              objectInstance.getObjectName().toString());
          if (resourceIdentify.getType() == null || resourceIdentify.getType()
              .equals(ResourceType.NONE)) {
            continue;
          }

          AttributeMetadata attribute = new AttributeMetadata();

          attribute.setId(RequestIdBuilder.get());
          attribute.setMbeanObjectName(objectInstance.getObjectName().toString());
          attribute.setAttributeName(attributeInfo[i].getName());
          attribute.setAttributeType(attributeInfo[i].getType());

          DescriptionEnDecoder descriptionEnDecoder = new DescriptionEnDecoder();
          descriptionEnDecoder.formatBy(attributeInfo[i].getDescription());
          attribute.setRange(descriptionEnDecoder.getRange());
          attribute.setUnitOfMeasurement(descriptionEnDecoder.getUnitOfMeasurement());
          attribute.setDescription(descriptionEnDecoder.getDescription());

          Set<String> services = new HashSet<String>();
          services.add(appContext.getInstanceName());
          attribute.setServices(services);

          attributes.add(attribute);
        }
      }
    }
    return attributes;
  }

  protected void onStart() throws Exception {
    timer = new Timer();

    int port = appContext.getEndPointByServiceName(PortType.MONITOR).getPort();
    String hostName = appContext.getEndPointByServiceName(PortType.MONITOR).getHostName();

    System.setProperty("java.rmi.server.hostname", hostName);

    logger.debug("Has mbean-server been registed? : {}", hasBeenRegisted);

    if (!hasBeenRegisted) {
      registry = LocateRegistry.createRegistry(port);
      hasBeenRegisted = true;
    }
    mbeanServer = ManagementFactory.getPlatformMBeanServer();

    String surl = String
        .format("service:jmx:rmi:///jndi/rmi://%s:%d/%s", hostName, port, registryName);
    logger.debug("string: {}, hostName: {}, port: {}, registryName: {}", surl, hostName, port,
        registryName);
    JMXServiceURL url = new JMXServiceURL(surl);

    jmxConnectorServer = JMXConnectorServerFactory.newJMXConnectorServer(url, null, mbeanServer);
    jmxConnectorServer.start();

    ObjectName name = new ObjectName(JmxRmiAgentMBean.ReporterCreaterName);
    mbeanCreator = new JmxRmiAgent();
    mbeanCreator.setAgent(this);
    mbeanServer.registerMBean(mbeanCreator, name);

    logger.debug("AppContext: {}, DIH service name: {}", appContext, PyService.DIH.name());
    PeriodicPojo memoryTaskPojo = new MemoryTaskPojo();
    memoryTaskPojo.setAutoReport(false);
    Collector memoryCollector = Collector.createBy(memoryTaskPojo)
        .endpoint(appContext.getEndPointByServiceName(PortType.CONTROL))
        .bindMbeanServer(mbeanServer)
        .bindTimer(timer).period(3000).build();
    collectors.add(memoryCollector);
    memoryCollector.start();

    PeriodicPojo cpuTaskPojo = new CpuTaskPojo();
    cpuTaskPojo.setAutoReport(false);
    Collector cpuCollector = Collector.createBy(cpuTaskPojo)
        .endpoint(appContext.getEndPointByServiceName(PortType.CONTROL))
        .bindMbeanServer(mbeanServer)
        .bindTimer(timer).period(3000).build();
    collectors.add(cpuCollector);
    cpuCollector.start();

    if (appContext.getInstanceName().equals(PyService.DIH.name())) {
      RealMachineInfomation realMachinePojo = new RealMachineInfomation();
      cpuTaskPojo.setAutoReport(false);
      Collector realMachineInfoCollector = Collector.createBy(realMachinePojo)
          .endpoint(appContext.getEndPointByServiceName(PortType.CONTROL))
          .bindMbeanServer(mbeanServer)
          .bindTimer(timer).period(3000).build();
      collectors.add(realMachineInfoCollector);
      realMachineInfoCollector.start();
    }

    alarmReporter = AlarmReporter.create()
        .endpoint(appContext.getEndPointByServiceName(PortType.CONTROL))
        .bindMbeanServer(mbeanServer).build();
    alarmReporter.start();

  }

  @Override
  protected void onStop() throws Exception {
    for (PeriodicReporter performanceReporter : performanceReporters) {
      try {
        performanceReporter.stop();
      } catch (Exception e) {
        logger.error("Caught an exception", e);
        continue;
      }
    }

    for (Collector collector : collectors) {
      try {
        collector.stop();
      } catch (Exception e) {
        logger.error("Caught an exception", e);
      }
    }

    alarmReporter.stop();

    registry.unbind(registryName);
    timer.cancel();

    try {
      jmxConnectorServer.stop();
    } catch (IOException e) {
      logger.error("Caught an exception", e);
      throw e;
    }

    ObjectName name = new ObjectName(JmxRmiAgentMBean.ReporterCreaterName);
    mbeanServer.unregisterMBean(name);

  }

  @Override
  protected boolean isJmxAgentSwitcherOn() throws Exception {
    return isJmxSwitcher();
  }

  @Override
  protected void onPause() throws Exception {
  }

  @Override
  protected void onResume() throws Exception {
  }

  public List<String> getAlertRules() {
    return alertRules;
  }

  public void setAlertRules(List<String> alertRules) {
    this.alertRules = alertRules;
  }

  public List<List<String>> getPerformanceTaskContext() {
    return performanceTaskContext;
  }

  public void setPerformanceTaskContext(List<List<String>> performanceTaskContext) {
    this.performanceTaskContext = performanceTaskContext;
  }

  public List<PeriodicReporter> getPerformanceReporters() {
    return performanceReporters;
  }

  public Timer getTimer() {
    return timer;
  }

  public MBeanServer getMbeanServer() {
    return mbeanServer;
  }

  public void setMbeanServer(MBeanServer mbeanServer) {
    this.mbeanServer = mbeanServer;
  }

  public AppContext getAppContext() {
    return appContext;
  }

  public void setAppContext(AppContext appContext) {
    this.appContext = appContext;
  }

  public AlarmReporter getAlarmReporter() {
    return alarmReporter;
  }

  public void setAlarmReporter(AlarmReporter alarmReporter) {
    this.alarmReporter = alarmReporter;
  }

  public boolean isJmxSwitcher() {
    return jmxSwitcher;
  }

  public void setJmxSwitcher(boolean jmxSwitcher) {
    this.jmxSwitcher = jmxSwitcher;
  }

  private static class LazyHolder {
    private static final JmxAgent singletonInstance = new JmxAgent();
  }
}
