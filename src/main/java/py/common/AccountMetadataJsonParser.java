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
