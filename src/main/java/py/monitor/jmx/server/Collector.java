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

import java.util.Timer;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.common.struct.EndPoint;
import py.monitor.pojo.management.PeriodicPojo;
import py.monitor.pojo.management.exception.ManagementException;
import py.monitor.pojo.management.helper.MBeanRegistration;
import py.monitor.pojo.management.helper.NotificableDynamicMBean;
import py.monitor.utils.Service;

public class Collector extends Service {
  private static final Logger logger = LoggerFactory.getLogger(Collector.class);
  private MBeanRegistration registration = null;
  private Timer timer;
  private long delay;
  private long period;
  private String filter;
  private PeriodicPojo pojo;
  private NotificableDynamicMBean mbean;

  private Collector(EndPoint endpoint, MBeanRegistration registration, final Timer timer,
      PeriodicPojo pojo,
      String filter, long delay, long period) {
    this.delay = delay;
    this.period = period;
    this.filter = filter;
    this.timer = timer;
    this.registration = registration;
    this.pojo = pojo;
  }

  public static Builder createBy(PeriodicPojo pojo) {
    return new Builder(pojo);
  }

  protected void onStart() throws Exception {
    mbean = registration.register();
    pojo.setRealBean(mbean);

    if (timer != null) {
      timer.schedule(pojo, delay, period);
    }
  }

  protected void onStop() throws Exception {
    logger.debug("Going to stop the collector");
    if (this.registration == null) {
      logger.warn("Timer task has not been started, no need to do 'stop' operation");
      return;
    }

    if (!pojo.cancel()) {
      throw new Exception("Failed to stop the task as a timer task");
    }

    if (timer != null) {
      timer.cancel();
    }

    try {
      logger.debug("Going to unregister the collector: {}", this.mbean);
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

  public static class Builder {
    private final PeriodicPojo pojo;
    private MBeanRegistration registration;
    private MBeanServer mbeanserver;
    private ObjectName objectName;
    private Timer timer;
    private String filter = new String("");
    private long delay = 0;
    private long period = 1000;
    private EndPoint endpoint;

    private Builder(PeriodicPojo pojo) {
      this.pojo = pojo;
    }

    public Builder forRegistration(MBeanRegistration registration) {
      this.registration = registration;
      return this;
    }

    public Builder bindMbeanServer(MBeanServer mbeanServer) {
      this.mbeanserver = mbeanServer;
      return this;
    }

    public Builder objectName(ObjectName objectName) {
      this.objectName = objectName;
      return this;
    }

    public Builder bindTimer(Timer timer) {
      this.timer = timer;
      return this;
    }

    public Builder filter(String filter) {
      this.filter = filter;
      return this;
    }

    public Builder delay(long delay) {
      this.delay = delay;
      return this;
    }

    public Builder period(long period) {
      this.period = period;
      return this;
    }

    public Builder endpoint(EndPoint endpoint) {
      this.endpoint = endpoint;
      return this;
    }

    public Collector build() throws Exception {
      checkRegistration();
      checkTimer();

      return new Collector(endpoint, registration, timer, pojo, filter, delay, period);
    }

    private void checkRegistration() throws Exception {
      if (registration == null) {
        if (pojo == null) {
          throw new Exception("puppetOfMBean can't be null");
        } else {
          if (objectName == null) {
            if (mbeanserver != null) {
              registration = new MBeanRegistration(endpoint, pojo, mbeanserver);
            } else {
              registration = new MBeanRegistration(endpoint, pojo);
            }
          } else {
            if (mbeanserver == null) {
              registration = new MBeanRegistration(endpoint, pojo, objectName);
            } else {
              registration = new MBeanRegistration(endpoint, pojo, objectName, mbeanserver);
            }
          }
        }
      } else {
        // nothing
      }
    }

    private void checkTimer() throws Exception {
      if (timer == null) {
        logger.warn("It is NOT good enough to create a timer just used by one single MBean");
        timer = new Timer();
      } else {
        // nothing
      }
    }
  }

}
