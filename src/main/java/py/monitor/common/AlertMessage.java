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

package py.monitor.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.common.RequestIdBuilder;
import py.monitor.utils.MonitorServerUtils;

public class AlertMessage {
  private static final Logger logger = LoggerFactory.getLogger(AlertMessage.class);

  private String id;
  private String sourceId;
  private String sourceName;
  private String alertDescription;
  private String alertLevel;

  private String alertType;
  private String alertRuleName;
  private String counterKey;
  private boolean alertAcknowledge;
  private long alertAcknowledgeTime;
  private long firstAlertTime;
  private long lastAlertTime;
  private int alertFrequency;
  private boolean alertClear;
  private String alertClearType;
  private long clearTime;
  private boolean readFlag;
  private boolean deleteFlag;
  private long deleteTime;
  private String lastActualValue;

  private String ip;
  private String hostname;
  private String alertItem;
  private String serialNum;
  private String slotNo;
  private String serverNodeRackNo;
  private String serverNodeChildFramNo;
  private String serverNodeSlotNo;

  private EventDataInfo latestEventDataInfo;
  private Set<EventLogInfo> eventLogInfoSet;

  public AlertMessage() {
    this.id = String.valueOf(RequestIdBuilder.get());
    this.alertFrequency = 1;
    this.eventLogInfoSet = new TreeSet<>();
  }

  public static boolean isNetSubHealthAlert(String counterKey) {
    boolean isNetSubHealth = false;

    List<String> counterKeyList = getCounterNameList(counterKey);

    for (String counterKeyTemp : counterKeyList) {
      try {
        isNetSubHealth = isNetSubHealthAlert(CounterName.valueOf(counterKeyTemp));
        if (isNetSubHealth) {
          break;
        }
      } catch (Exception e) {
        logger.debug("cannot found CounterName:{} or its the mix counterName", counterKeyTemp);
      }
    }
    return isNetSubHealth;
  }

  public static boolean isNetSubHealthAlert(CounterName counterName) {
    switch (counterName) {
      case NETCARD_DELAY:
      case NETWORK_DROPPED:
      case NETWORK_CONGESTION:
        return true;
      default:
        break;
    }
    return false;
  }

  public boolean isNetSubHealthAlert() {
    return isNetSubHealthAlert(counterKey);
  }

  public static List<String> getCounterNameList(String counterKey) {
    List<String> counterKeyList = new ArrayList<>();
    if (counterKey.contains(MonitorServerUtils.MIX_ALERT_RULE_DELIMITER)) {
      counterKeyList = Arrays.asList(counterKey.split(MonitorServerUtils.MIX_ALERT_RULE_DELIMITER));
    } else {
      counterKeyList.add(counterKey);
    }
    return counterKeyList;
  }

  public List<String> getCounterNameList() {
    return getCounterNameList(counterKey);
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getSourceId() {
    return sourceId;
  }

  public void setSourceId(String sourceId) {
    this.sourceId = sourceId;
  }

  public String getSourceName() {
    return sourceName;
  }

  public void setSourceName(String sourceName) {
    this.sourceName = sourceName;
  }

  public String getAlertDescription() {
    return alertDescription;
  }

  public void setAlertDescription(String alertDescription) {
    this.alertDescription = alertDescription;
  }

  public String getAlertLevel() {
    return alertLevel;
  }

  public void setAlertLevel(String alertLevel) {
    this.alertLevel = alertLevel;
  }

  public String getAlertType() {
    return alertType;
  }

  public void setAlertType(String alertType) {
    this.alertType = alertType;
  }

  public String getAlertRuleName() {
    return alertRuleName;
  }

  public void setAlertRuleName(String alertRuleName) {
    this.alertRuleName = alertRuleName;
  }

  public String getCounterKey() {
    return counterKey;
  }

  public void setCounterKey(String counterKey) {
    this.counterKey = counterKey;
  }

  public boolean isAlertAcknowledge() {
    return alertAcknowledge;
  }

  public void setAlertAcknowledge(boolean alertAcknowledge) {
    this.alertAcknowledge = alertAcknowledge;
  }

  public long getAlertAcknowledgeTime() {
    return alertAcknowledgeTime;
  }

  public void setAlertAcknowledgeTime(long alertAcknowledgeTime) {
    this.alertAcknowledgeTime = alertAcknowledgeTime;
  }

  public long getFirstAlertTime() {
    return firstAlertTime;
  }

  public void setFirstAlertTime(long firstAlertTime) {
    this.firstAlertTime = firstAlertTime;
  }

  public long getLastAlertTime() {
    return lastAlertTime;
  }

  public void setLastAlertTime(long lastAlertTime) {
    this.lastAlertTime = lastAlertTime;
  }

  public int getAlertFrequency() {
    return alertFrequency;
  }

  public void setAlertFrequency(int alertFrequency) {
    this.alertFrequency = alertFrequency;
  }

  public void incAlertFrequency() {
    this.alertFrequency++;
  }

  public boolean isAlertClear() {
    return alertClear;
  }

  public void setAlertClear(boolean alertClear) {
    this.alertClear = alertClear;
  }

  public String getIp() {
    return ip;
  }

  public void setIp(String ip) {
    this.ip = ip;
  }

  public String getHostname() {
    return hostname;
  }

  public void setHostname(String hostname) {
    this.hostname = hostname;
  }

  public String getAlertItem() {
    return alertItem;
  }

  public void setAlertItem(String alertItem) {
    this.alertItem = alertItem;
  }

  public String getSerialNum() {
    return serialNum;
  }

  public void setSerialNum(String serialNum) {
    this.serialNum = serialNum;
  }

  public String getSlotNo() {
    return slotNo;
  }

  public void setSlotNo(String slotNo) {
    this.slotNo = slotNo;
  }

  public String getServerNodeRackNo() {
    return serverNodeRackNo;
  }

  public void setServerNodeRackNo(String serverNodeRackNo) {
    this.serverNodeRackNo = serverNodeRackNo;
  }

  public String getServerNodeChildFramNo() {
    return serverNodeChildFramNo;
  }

  public void setServerNodeChildFramNo(String serverNodeChildFramNo) {
    this.serverNodeChildFramNo = serverNodeChildFramNo;
  }

  public String getServerNodeSlotNo() {
    return serverNodeSlotNo;
  }

  public void setServerNodeSlotNo(String serverNodeSlotNo) {
    this.serverNodeSlotNo = serverNodeSlotNo;
  }

  public String getAlertClearType() {
    return alertClearType;
  }

  public void setAlertClearType(String alertClearType) {
    this.alertClearType = alertClearType;
  }

  public long getClearTime() {
    return clearTime;
  }

  public void setClearTime(long clearTime) {
    this.clearTime = clearTime;
  }

  public boolean isReadFlag() {
    return readFlag;
  }

  public void setReadFlag(boolean readFlag) {
    this.readFlag = readFlag;
  }

  public boolean isDeleteFlag() {
    return deleteFlag;
  }

  public void setDeleteFlag(boolean deleteFlag) {
    this.deleteFlag = deleteFlag;
  }

  public long getDeleteTime() {
    return deleteTime;
  }

  public void setDeleteTime(long deleteTime) {
    this.deleteTime = deleteTime;
  }

  public String getLastActualValue() {
    return lastActualValue;
  }

  public void setLastActualValue(String lastActualValue) {
    this.lastActualValue = lastActualValue;
  }

  public EventDataInfo getLatestEventDataInfo() {
    return latestEventDataInfo;
  }

  public void setLatestEventDataInfo(EventDataInfo latestEventDataInfo) {
    this.latestEventDataInfo = latestEventDataInfo;
  }

  public Set<EventLogInfo> getEventLogInfoSet() {
    return eventLogInfoSet;
  }

  public void setEventLogInfoSet(Set<EventLogInfo> eventLogInfoSet) {
    this.eventLogInfoSet = eventLogInfoSet;
  }

  public void addEventLogInfo(EventLogInfo eventLogInfo) {
    if (eventLogInfo == null) {
      return;
    }
    this.eventLogInfoSet.add(eventLogInfo);
  }

  public AlertMessageKey getAlertMessageKey() {
    return new AlertMessageKey(sourceId, counterKey,
        alertLevel == null ? null : AlertLevel.valueOf(alertLevel));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof AlertMessage)) {
      return false;
    }

    AlertMessage that = (AlertMessage) o;

    if (alertAcknowledge != that.alertAcknowledge) {
      return false;
    }
    if (alertAcknowledgeTime != that.alertAcknowledgeTime) {
      return false;
    }
    if (firstAlertTime != that.firstAlertTime) {
      return false;
    }
    if (lastAlertTime != that.lastAlertTime) {
      return false;
    }
    if (alertFrequency != that.alertFrequency) {
      return false;
    }
    if (alertClear != that.alertClear) {
      return false;
    }
    if (clearTime != that.clearTime) {
      return false;
    }
    if (readFlag != that.readFlag) {
      return false;
    }
    if (deleteFlag != that.deleteFlag) {
      return false;
    }
    if (deleteTime != that.deleteTime) {
      return false;
    }
    if (id != null ? !id.equals(that.id) : that.id != null) {
      return false;
    }
    if (sourceId != null ? !sourceId.equals(that.sourceId) : that.sourceId != null) {
      return false;
    }
    if (sourceName != null ? !sourceName.equals(that.sourceName) : that.sourceName != null) {
      return false;
    }
    if (alertDescription != null ? !alertDescription.equals(that.alertDescription)
        : that.alertDescription != null) {
      return false;
    }
    if (alertLevel != null ? !alertLevel.equals(that.alertLevel) : that.alertLevel != null) {
      return false;
    }
    if (alertType != null ? !alertType.equals(that.alertType) : that.alertType != null) {
      return false;
    }
    if (alertRuleName != null ? !alertRuleName.equals(that.alertRuleName)
        : that.alertRuleName != null) {
      return false;
    }
    if (counterKey != null ? !counterKey.equals(that.counterKey) : that.counterKey != null) {
      return false;
    }
    if (alertClearType != null ? !alertClearType.equals(that.alertClearType)
        : that.alertClearType != null) {
      return false;
    }
    if (lastActualValue != null ? !lastActualValue.equals(that.lastActualValue)
        : that.lastActualValue != null) {
      return false;
    }
    if (ip != null ? !ip.equals(that.ip) : that.ip != null) {
      return false;
    }
    if (hostname != null ? !hostname.equals(that.hostname) : that.hostname != null) {
      return false;
    }
    if (alertItem != null ? !alertItem.equals(that.alertItem) : that.alertItem != null) {
      return false;
    }
    if (serialNum != null ? !serialNum.equals(that.serialNum) : that.serialNum != null) {
      return false;
    }
    if (slotNo != null ? !slotNo.equals(that.slotNo) : that.slotNo != null) {
      return false;
    }
    if (serverNodeRackNo != null ? !serverNodeRackNo.equals(that.serverNodeRackNo)
        : that.serverNodeRackNo != null) {
      return false;
    }
    if (serverNodeChildFramNo != null ? !serverNodeChildFramNo.equals(that.serverNodeChildFramNo)
        : that.serverNodeChildFramNo != null) {
      return false;
    }
    if (serverNodeSlotNo != null ? !serverNodeSlotNo.equals(that.serverNodeSlotNo)
        : that.serverNodeSlotNo != null) {
      return false;
    }
    if (latestEventDataInfo != null ? !latestEventDataInfo.equals(that.latestEventDataInfo)
        : that.latestEventDataInfo != null) {
      return false;
    }
    return eventLogInfoSet != null ? eventLogInfoSet.equals(that.eventLogInfoSet)
        : that.eventLogInfoSet == null;
  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + (sourceId != null ? sourceId.hashCode() : 0);
    result = 31 * result + (sourceName != null ? sourceName.hashCode() : 0);
    result = 31 * result + (alertDescription != null ? alertDescription.hashCode() : 0);
    result = 31 * result + (alertLevel != null ? alertLevel.hashCode() : 0);
    result = 31 * result + (alertType != null ? alertType.hashCode() : 0);
    result = 31 * result + (alertRuleName != null ? alertRuleName.hashCode() : 0);
    result = 31 * result + (counterKey != null ? counterKey.hashCode() : 0);
    result = 31 * result + (alertAcknowledge ? 1 : 0);
    result = 31 * result + (int) (alertAcknowledgeTime ^ (alertAcknowledgeTime >>> 32));
    result = 31 * result + (int) (firstAlertTime ^ (firstAlertTime >>> 32));
    result = 31 * result + (int) (lastAlertTime ^ (lastAlertTime >>> 32));
    result = 31 * result + alertFrequency;
    result = 31 * result + (alertClear ? 1 : 0);
    result = 31 * result + (alertClearType != null ? alertClearType.hashCode() : 0);
    result = 31 * result + (int) (clearTime ^ (clearTime >>> 32));
    result = 31 * result + (readFlag ? 1 : 0);
    result = 31 * result + (deleteFlag ? 1 : 0);
    result = 31 * result + (int) (deleteTime ^ (deleteTime >>> 32));
    result = 31 * result + (lastActualValue != null ? lastActualValue.hashCode() : 0);
    result = 31 * result + (ip != null ? ip.hashCode() : 0);
    result = 31 * result + (hostname != null ? hostname.hashCode() : 0);
    result = 31 * result + (alertItem != null ? alertItem.hashCode() : 0);
    result = 31 * result + (serialNum != null ? serialNum.hashCode() : 0);
    result = 31 * result + (slotNo != null ? slotNo.hashCode() : 0);
    result = 31 * result + (serverNodeRackNo != null ? serverNodeRackNo.hashCode() : 0);
    result = 31 * result + (serverNodeChildFramNo != null ? serverNodeChildFramNo.hashCode() : 0);
    result = 31 * result + (serverNodeSlotNo != null ? serverNodeSlotNo.hashCode() : 0);
    result = 31 * result + (latestEventDataInfo != null ? latestEventDataInfo.hashCode() : 0);
    result = 31 * result + (eventLogInfoSet != null ? eventLogInfoSet.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "AlertMessage{"
        + "id='" + id + '\''
        + ", sourceId='" + sourceId + '\''
        + ", sourceName='" + sourceName + '\''
        + ", alertDescription='" + alertDescription + '\''
        + ", alertLevel='" + alertLevel + '\''
        + ", alertType='" + alertType + '\''
        + ", alertRuleName='" + alertRuleName + '\''
        + ", counterKey='" + counterKey + '\''
        + ", alertAcknowledge=" + alertAcknowledge
        + ", alertAcknowledgeTime=" + alertAcknowledgeTime
        + ", firstAlertTime=" + firstAlertTime
        + ", lastAlertTime=" + lastAlertTime
        + ", alertFrequency=" + alertFrequency
        + ", alertClear=" + alertClear
        + ", alertClearType='" + alertClearType + '\''
        + ", clearTime='" + clearTime + '\''
        + ", readFlag=" + readFlag
        + ", deleteFlag=" + deleteFlag
        + ", deleteTime=" + deleteTime
        + ", lastActualValue='" + lastActualValue + '\''
        + ", ip='" + ip + '\''
        + ", hostname='" + hostname + '\''
        + ", alertItem='" + alertItem + '\''
        + ", serialNum='" + serialNum + '\''
        + ", slotNo='" + slotNo + '\''
        + ", serverNodeRackNo='" + serverNodeRackNo + '\''
        + ", serverNodeChildFramNo='" + serverNodeChildFramNo + '\''
        + ", serverNodeSlotNo='" + serverNodeSlotNo + '\''
        + ", latestEventDataInfo=" + latestEventDataInfo
        + '}';
  }
}
