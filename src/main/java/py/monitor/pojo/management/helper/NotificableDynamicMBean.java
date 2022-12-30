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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import javax.management.DynamicMBean;
import javax.management.MBeanRegistration;
import javax.management.NotificationBroadcasterSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.SerializationUtils;
import py.common.struct.EndPoint;
import py.metrics.PyMetricRegistry.PyMetricNameBuilder;
import py.monitor.alarmbak.AlarmMessageData;
import py.monitor.customizable.repository.AttributeMetadata;
import py.monitor.dtd.Field;
import py.monitor.dtd.FieldId;
import py.monitor.jmx.repoter.notification.AlarmNotification;
import py.monitor.jmx.repoter.notification.PerformanceNotification;
import py.monitor.jmx.server.ResourceRelatedAttribute;

public abstract class NotificableDynamicMBean extends NotificationBroadcasterSupport
    implements DynamicMBean, MBeanRegistration {
  private static final Logger logger = LoggerFactory.getLogger(NotificableDynamicMBean.class);
  protected AtomicLong sequenceNumber = new AtomicLong(1);
  protected List<ResourceRelatedAttribute> resourceRelatedAttributes;
  protected EndPoint endpoint;
  protected long parentTaskId;

  public EndPoint getEndpoint() {
    return endpoint;
  }

  public void setEndpoint(EndPoint endpoint) {
    this.endpoint = endpoint;
  }

  public void sendPerformanceNotify() throws Exception {
    List<Field> notificationDatas = new ArrayList<Field>();
    try {
      logger.debug("resourceRelatedAttribute are : {}", resourceRelatedAttributes);
      for (ResourceRelatedAttribute resourceRelatedAttribute : resourceRelatedAttributes) {
        logger.debug("{}", resourceRelatedAttribute);

        AttributeMetadata attributeMetadata = resourceRelatedAttribute.getAttributeMetadata();
        try {
          Field data = new Field();
          data.setId(new FieldId(parentTaskId, attributeMetadata.getId(), UUID.randomUUID()));
          data.setName(attributeMetadata.getAttributeName());
          data.setType(attributeMetadata.getAttributeType());

          String resourceRelatedName =
              new PyMetricNameBuilder(attributeMetadata.getMbeanObjectName())
                  .type(attributeMetadata.getResourceType())
                  .id(String.valueOf(resourceRelatedAttribute.getResourceId())).build() + "."
                  + attributeMetadata.getAttributeName();
          logger.debug("resource id: {}, resource related name: {}",
              String.valueOf(resourceRelatedAttribute.getResourceId()), resourceRelatedName);
          data.setData(SerializationUtils.serialize(this.getAttribute(resourceRelatedName)));
          data.setDataSourceProvider(attributeMetadata.getMbeanObjectName());
          data.setTimeStamp(System.currentTimeMillis());

          data.setResourceType(attributeMetadata.getResourceType());
          data.setResourceId(resourceRelatedAttribute.getResourceId());

          notificationDatas.add(data);
        } catch (Exception e) {
          logger.warn("Caught an exception", e);
        }
      }

      logger.debug("Going to send performance data to jmx client, Sender:{},the data is {}",
          this.getClass().toString(), notificationDatas.size());
      PerformanceNotification notification = new PerformanceNotification(this,
          sequenceNumber.getAndIncrement(),
          System.currentTimeMillis(), PerformanceNotification.LIST_ATTRIBUTE_FOR_PERFORMANCE,
          notificationDatas);
      logger.debug("Notification data is : {}", notificationDatas);
      this.sendNotification(notification);

      logger.debug("[{}] sent a performance notification [{}]", this.getClass().getName(),
          notification);
    } catch (Exception e) {
      logger.error("Caught an exception", e);
      throw e;
    }
  }

  public void sendAlarmNotify(AlarmMessageData data) throws Exception {
    logger.debug("Going to send alarm data to monitorcenter");
    AlarmNotification notification = new AlarmNotification(this,
        UUID.randomUUID().getLeastSignificantBits(),
        System.currentTimeMillis(), AlarmNotification.LIST_ATTRIBUTE_FOR_ALARM, data);
    this.sendNotification(notification);
  }
}
