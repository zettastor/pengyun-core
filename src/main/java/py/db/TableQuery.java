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