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

package py.db;

public class TableQuery {
  private StringBuilder selectedItems = new StringBuilder();
  private StringBuilder table = new StringBuilder();
  private StringBuilder condition = new StringBuilder();
  private StringBuilder order = new StringBuilder();

  public TableQuery appendToSelectedItems(String a) {
    selectedItems.append(a);
    return this;
  }

  public TableQuery appendToTable(String a) {
    table.append(a);
    return this;
  }

  public TableQuery appendToCondition(String a) {
    condition.append(a);
    return this;
  }

  public TableQuery appendToOrder(String a) {
    order.append(a);
    return this;
  }

  public String getTable() {
    return table.toString();
  }

  public String getCondition() {
    return condition.toString();
  }

  public String getOrder() {
    return order.toString();
  }

  public String getSelectedItems() {
    return selectedItems.toString();
  }

  /**
   * If table is null or empty, the query is not valid.
   */
  public boolean isValid() {
    if (table.toString().trim().length() == 0) {
      return false;
    } else {
      return true;
    }
  }
}