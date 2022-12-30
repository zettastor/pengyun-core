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

import java.util.ArrayList;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.common.struct.EndPoint;
import py.monitor.alarmbak.AlarmMessageData;
import py.monitor.customizable.CustomizableMBean;
import py.monitor.pojo.management.exception.ManagementException;
import py.monitor.pojo.management.helper.MBeanRegistration;
import py.monitor.pojo.management.helper.NotificableDynamicMBean;
import py.monitor.utils.Service;

public class AlarmReporter extends Service {
  public static final String AlarmReporterObjectName = "pojo-agent-alarm:name=AlarmReporter";
  private static final Logger logger = LoggerFactory.getLogger(AlarmReporter.class);
  private MBeanRegistration registration = null;
  private NotificableDynamicMBean mbean;

  private AlarmReporter(MBeanRegistration registration) {
    this.registration = registration;
  }

  public static Builder create() {
    return new Builder();
  }

  public MBeanRegistration getRegistration() {
    return registration;
  }

  public void setRegistration(MBeanRegistration registration) {
    this.registration = registration;
  }

  public NotificableDynamicMBean getMbean() {
    return mbean;
  }

  public void setMbean(NotificableDynamicMBean mbean) {
    this.mbean = mbean;
  }

  @Override
  protected void onStart() throws Exception {
    mbean = registration.register();
  }

  @Override
  protected void onStop() throws Exception {
    if (this.registration == null) {
      logger.warn("Timer task has not been started, no need to do 'stop' operation");
      return;
    }

    try {
      registration.unregister();
    } catch (ManagementException e) {
      logger.error("Faild to stop the task as a mbean task", e);
      throw e;
    }
  }

  @Override
  protected void onPause() throws Exception {
  }

  @Override
  protected void onResume() throws Exception {
  }

  public void report(AlarmMessageData data) throws Exception {
    try {
      this.mbean.sendAlarmNotify(data);
    } catch (Exception e) {
      logger.error("Caught an exception when sending alarm data to monitorcenter");
      throw e;
    }
  }

  public static class Builder {
    private MBeanRegistration registration;
    private MBeanServer mbeanserver;
    private ObjectName mbeanObjectName;
    private CustomizableMBean dataProvider;
    private EndPoint endpoint;

    private Builder() {
    }

    public Builder forRegistration(MBeanRegistration registration) {
      this.registration = registration;
      return this;
    }

    public Builder bindMbeanServer(MBeanServer mbeanserver) {
      this.mbeanserver = mbeanserver;
      return this;
    }

    public Builder objectName(ObjectName objectName) {
      this.mbeanObjectName = objectName;
      return this;
    }

    public Builder objectName(String mbeanObjectName) throws MalformedObjectNameException {
      this.mbeanObjectName = new ObjectName(mbeanObjectName);
      return this;
    }

    public Builder endpoint(EndPoint endpoint) {
      this.endpoint = endpoint;
      return this;
    }

    public AlarmReporter build() throws Exception {
      checkObjectName();
      checkDataProvider();
      checkRegistration();
      checkEndpoint();

      return new AlarmReporter(registration);
    }

    private void checkObjectName() throws Exception {
      if (mbeanObjectName == null) {
        this.mbeanObjectName = new ObjectName(AlarmReporterObjectName);
      } else {
        // nothing
      }
    }

    private void checkRegistration() throws Exception {
      if (registration == null) {
        if (mbeanserver == null) {
          registration = new MBeanRegistration(endpoint, dataProvider, mbeanObjectName);
        } else {
          registration = new MBeanRegistration(endpoint, dataProvider, mbeanObjectName,
              mbeanserver);
        }
      } else {
        // nothing
      }
    }

    private void checkDataProvider() throws Exception {
      if (dataProvider == null) {
        logger.warn("No data provider has been selected in, so we're going to create a new one");
        dataProvider = new CustomizableMBean(this.endpoint, this.mbeanserver, 0,
            new ArrayList<ResourceRelatedAttribute>());
      } else {
        // nothing
      }
    }

    private void checkEndpoint() throws Exception {
      if (this.endpoint == null) {
        throw new Exception();
      }
    }
  }
}
