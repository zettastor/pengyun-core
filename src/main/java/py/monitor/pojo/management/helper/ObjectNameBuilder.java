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

package py.monitor.pojo.management.helper;

import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.common.struct.EndPoint;
import py.metrics.PyMetricRegistry.PyMetricNameBuilder;
import py.monitor.pojo.management.annotation.MBean;
import py.monitor.pojo.util.Preconditions;

public class ObjectNameBuilder {
  private static final Logger logger = LoggerFactory.getLogger(ObjectNameBuilder.class);
  private static final String KEY_NAME = "name";
  private static final String KEY_TYPE = "type";
  private static final String KEY_APPLICATION = "application";

  private ObjectName objectName;
  private String domain;
  private Map<String, String> properties = new LinkedHashMap<String, String>();
  private EndPoint endpoint;

  public ObjectNameBuilder() {
  }

  private void normalize() {
    if (objectName != null) {
      this.domain = objectName.getDomain();
      this.properties.putAll(objectName.getKeyPropertyList());
      this.objectName = null;
    }
  }

  public ObjectNameBuilder withObjectName(Class<?> mbeanClass) throws MalformedObjectNameException {
    MBean annotation = mbeanClass.getAnnotation(MBean.class);
    Preconditions.notNull(annotation, mbeanClass + " is not annotated with " + MBean.class);
    return withObjectName(annotation);
  }

  public ObjectNameBuilder withObjectName(MBean annotation) throws MalformedObjectNameException {
    String annotatedObjectName = annotation.objectName();
    String finalObjectName = null;
    try {
      logger.debug("endpoint:{}", endpoint);
      String resourceId = null;
      switch (annotation.resourceType()) {
        case MACHINE:
          resourceId = this.endpoint.getHostName();
          break;
        case JVM:
          resourceId = String.format("%s[%d]", endpoint.getHostName(), endpoint.getPort());
          break;
        default:
          resourceId = PyMetricNameBuilder.EMPTY_ID;
          break;
      }
      finalObjectName = new PyMetricNameBuilder(annotatedObjectName).type(annotation.resourceType())
          .id(resourceId).build();
    } catch (Exception e) {
      logger.error("Caught an exception", e);
      throw new MalformedObjectNameException();
    }

    Preconditions.notEmpty(finalObjectName,
        String.format("@{} annotation does not specify objectName", MBean.class.getName()));
    return withObjectName(ObjectName.getInstance(finalObjectName));
  }

  public ObjectNameBuilder withObjectName(ObjectName objectName) {
    this.objectName = objectName;
    return this;
  }

  public ObjectNameBuilder withDomainAndType(Class<?> mbeanClass)
      throws MalformedObjectNameException {
    return withDomain(mbeanClass.getPackage().getName()).withType(mbeanClass.getName());
  }

  public ObjectNameBuilder withDomain(String domain) {
    normalize();
    this.domain = domain;
    return this;
  }

  public ObjectNameBuilder withName(String name) {
    return withProperty(KEY_NAME, name);
  }

  public ObjectNameBuilder withType(String type) {
    return withProperty(KEY_TYPE, type);
  }

  public ObjectNameBuilder withEndpoint(EndPoint endpoint) {
    this.endpoint = endpoint;
    return this;
  }

  public ObjectNameBuilder withApplication(String app) {
    return withProperty(KEY_APPLICATION, app);
  }

  public ObjectNameBuilder withProperty(String key, String value) {
    normalize();
    properties.put(key, value);
    return this;
  }

  public ObjectName build() throws MalformedObjectNameException {
    return (objectName != null) ? objectName
        : ObjectName.getInstance(domain, new Hashtable<String, String>(properties));
  }
}
