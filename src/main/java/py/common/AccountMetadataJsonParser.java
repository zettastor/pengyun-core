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

package py.common;

import org.apache.log4j.Logger;

/**
 * A class to composite account id with account metadata json. The format of this composition is
 * "accountId:accountMetadataJSON". As we know parse json and encode json is a very high load job.
 * Sometimes we just need one element of the json, so we take the key element out of json, and
 * composite them.
 */
public class AccountMetadataJsonParser {
  private final Logger logger = Logger.getLogger(AccountMetadataJsonParser.class);
  private long accountId;
  private String accountMetadataJson;

  public AccountMetadataJsonParser(String compositedAccountMetadataJson) {
    int index = compositedAccountMetadataJson.indexOf(':');
    if (index == -1) {
      logger.warn("can not parse the volumeMetadataJSON:" + compositedAccountMetadataJson);
      return;
    }

    accountId = Long.valueOf(compositedAccountMetadataJson.substring(0, index));
    accountMetadataJson = compositedAccountMetadataJson.substring(index + 1);
  }

  public AccountMetadataJsonParser(long accountId, String accountMetadataJson) {
    this.accountId = accountId;
    this.accountMetadataJson = accountMetadataJson;
  }

  public long getAccountId() {
    return accountId;
  }

  public void setAccountId(long accountId) {
    this.accountId = accountId;
  }

  public String getAccountMetadataJson() {
    return accountMetadataJson;
  }

  public void setAccountMetadataJson(String accountMetadataJson) {
    this.accountMetadataJson = accountMetadataJson;
  }

  public String getCompositedAccountMetadataJson() {
    return accountId + ":" + accountMetadataJson;
  }
}
