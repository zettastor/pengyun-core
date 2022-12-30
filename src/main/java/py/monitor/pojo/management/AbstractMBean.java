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
