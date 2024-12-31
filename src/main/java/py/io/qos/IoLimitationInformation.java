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

package py.io.qos;

import java.sql.Blob;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.persistence.Lob;
import net.sf.json.JSONArray;
import org.hibernate.annotations.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.license.Utils;

public class IoLimitationInformation {
  private static final Logger logger = LoggerFactory.getLogger(IoLimitationInformation.class);

  private long ruleId;

  private String ioLimitationName;

  private String limitType;

  @Lob
  @Type(type = "org.hibernate.type.BlobType")
  private Blob ioLimitationEntriesJson;

  private String status;

  public IoLimitationInformation() {
  }

  public IoLimitationInformation(long ruleId, String ioLimitationName, String limitType,
      Blob ioLimitationEntriesJson, String status) {
    this.ruleId = ruleId;
    this.ioLimitationName = ioLimitationName;
    this.limitType = limitType;
    this.ioLimitationEntriesJson = ioLimitationEntriesJson;
    this.status = status;
  }

  public String getLimitType() {
    return limitType;
  }

  public void setLimitType(String limitType) {
    this.limitType = limitType;
  }

  public String getIoLimitationName() {
    return ioLimitationName;
  }

  public void setIoLimitationName(String ioLimitationName) {
    this.ioLimitationName = ioLimitationName;
  }

  @Lob
  public Blob getIoLimitationEntriesJson() {
    return ioLimitationEntriesJson;
  }

  public void setIoLimitationEntriesJson(Blob ioLimitationEntriesJson) {
    this.ioLimitationEntriesJson = ioLimitationEntriesJson;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public long getRuleId() {
    return ruleId;
  }

  public void setRuleId(long ruleId) {
    this.ruleId = ruleId;
  }

  @Override
  public int hashCode() {
    int result = (int) (ruleId ^ (ruleId >>> 32));
    result = 31 * result + (limitType != null ? limitType.hashCode() : 0);
    result = 31 * result + (ioLimitationName != null ? ioLimitationName.hashCode() : 0);
    result =
        31 * result + (ioLimitationEntriesJson != null ? ioLimitationEntriesJson.hashCode() : 0);
    result = 31 * result + ((status == null) ? 0 : status.hashCode());
    return result;
  }

  public IoLimitation toIoLimitation() {
    IoLimitation ioLimitation = new IoLimitation();
    ioLimitation.setId(ruleId);
    ioLimitation.setName(ioLimitationName);
    ioLimitation.setLimitType(IoLimitation.LimitType.valueOf(limitType));
    ioLimitation.setStatus(IoLimitationStatus.valueOf(status));
    try {
      ioLimitation.setEntries(
          parseIoLimitationEntries(new String(Utils.readFrom(getIoLimitationEntriesJson()))));
      logger.debug("toIOLimitation ioLimitation {}", ioLimitation);
    } catch (Exception e) {
      logger.error("caught an exception", e);
    }
    return ioLimitation;
  }

  public List<IoLimitationEntry> parseIoLimitationEntries(String entriesJson) {
    logger.debug("parseIOLimitationEntries entriesJson {}", entriesJson);
    List<IoLimitationEntry> ioLimitationEntries = new ArrayList<>();
    try {
      JSONArray jsonArray = JSONArray.fromObject(entriesJson);
      logger.debug("parseIOLimitationEntries {}", jsonArray);
      Iterator<Object> iterator = jsonArray.iterator();
      while (iterator.hasNext()) {
        String json = iterator.next().toString();
        ioLimitationEntries.add(IoLimitationEntry.fromJson(json));
        logger.debug("parseIOLimitationEntries {} json {}", ioLimitationEntries, json);
      }
    } catch (Exception e) {
      logger.error("cannot get TimeZone from db set it to empty", e);
    }
    return ioLimitationEntries;
  }

  @Override
  public String toString() {
    return "IOLimitation [id=" + ruleId
        + ", ioLimitationName=" + ioLimitationName
        + ", limitType=" + limitType
        + ", status=" + status
        + "]";
  }

}
