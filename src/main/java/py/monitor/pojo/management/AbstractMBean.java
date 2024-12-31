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

package py.monitor.pojo.management;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import py.monitor.pojo.management.annotation.Description;
import py.monitor.pojo.management.annotation.ManagedAttribute;
import py.monitor.pojo.management.annotation.ManagedOperation;
import py.monitor.pojo.management.annotation.ManagedOperation.Impact;
import py.monitor.pojo.management.exception.ManagementException;
import py.monitor.pojo.management.helper.MBeanRegistration;
import py.monitor.pojo.management.helper.ObjectNameBuilder;

public abstract class AbstractMBean {
  protected static final long NONE = Long.MIN_VALUE;
  protected final MBeanRegistration registration;
  private DatatypeFactory dtf;
  private AtomicLong started;

  public AbstractMBean() throws MalformedObjectNameException {
    ObjectName objectName = new ObjectNameBuilder().withObjectName(getClass()).build();
    registration = new MBeanRegistration(null, this, objectName);
    initialize();
  }

  public AbstractMBean(String mbeanName) throws MalformedObjectNameException {
    ObjectName objectName = new ObjectNameBuilder().withObjectName(getClass()).withName(mbeanName)
        .build();
    registration = new MBeanRegistration(null, this, objectName);
    initialize();
  }

  public AbstractMBean(ObjectName objectName) {
    registration = new MBeanRegistration(null, this, objectName);
    initialize();
  }

  protected void initialize() {
    try {
      dtf = DatatypeFactory.newInstance();
    } catch (DatatypeConfigurationException e) {
      throw new RuntimeException(e);
    }

    resetMbean();
  }

  @ManagedOperation(Impact.ACTION)
  @Description("Reset this MBean's metrics")
  public void resetMbean() {
  }

  public void start() throws ManagementException {
    started = new AtomicLong(now());
    registration.register();
  }

  public void stop() throws ManagementException {
    registration.unregister();
  }

  @ManagedAttribute
  @Description("The time at which the MBean was started")
  public XMLGregorianCalendar getStarted() {
    return date(noneAsNull(started));
  }

  protected Long noneAsNull(AtomicLong a) {
    long n = a.get();
    return n == NONE ? null : n;
  }

  protected Long age(Long millis, TimeUnit unit) {
    return millis == null ? null : unit.convert(now() - millis, MILLISECONDS);
  }

  protected String dateString(Long millis) {
    return (millis != null) ? date(millis).toXMLFormat() : null;
  }

  protected XMLGregorianCalendar date(Long millis) {
    if (millis == null) {
      return null;
    }
    GregorianCalendar gcal = new GregorianCalendar();
    gcal.setTimeInMillis(millis);
    return dtf.newXMLGregorianCalendar(gcal);
  }

  protected long now() {
    return System.currentTimeMillis();
  }

  protected AtomicLong zero() {
    return new AtomicLong();
  }

  protected AtomicLong none() {
    return new AtomicLong(NONE);
  }
}
