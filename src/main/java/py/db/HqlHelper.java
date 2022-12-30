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

import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public class HqlHelper {
  private static Logger log = Logger.getLogger(HqlHelper.class);

  // building a condition. replace " and " with " where"
  private static String getCondition(String condition) {
    StringBuilder conditionBuilder = new StringBuilder(" ");
    if (!StringUtils.isBlank(condition)) {
      // Compile with case-insensitivity
      Pattern pattern = Pattern.compile("and", Pattern.CASE_INSENSITIVE);
      conditionBuilder.append(pattern.matcher(condition).replaceFirst("where"));
    }

    return conditionBuilder.toString();
  }

  public static String buildSelectHql(TableQuery query) {
    return buildSelectHql(query.getSelectedItems(), query.getTable(), query.getCondition(),
        query.getOrder());
  }

  public static String buildSelectHql(String items, String table, String condition, String order) {
    StringBuilder builder = new StringBuilder();

    builder.append(StringUtils.isBlank(items) ? "" : "select " + items);
    builder.append(" from ").append(table);
    builder.append(" ").append(getCondition(condition));
    if (!StringUtils.isBlank(order)) {
      builder.append(" order by ").append(order);
    }

    log.debug(builder.toString());
    return builder.toString();
  }

  public static String buildCountHql(TableQuery query) {
    return buildCountHql(query.getTable(), query.getCondition());
  }

  public static String buildCountHql(String table, String condition) {
    StringBuilder builder = new StringBuilder("select count(*) from ");

    builder.append(table);
    builder.append(" ").append(getCondition(condition));
    log.debug(builder.toString());
    return builder.toString();
  }

  public static String buildDeleteHql(TableQuery query) {
    return buildCountHql(query.getTable(), query.getCondition());
  }

  public static String buildDeleteHql(String table, String condition) {
    StringBuilder builder = new StringBuilder("delete from ");

    builder.append(table);
    builder.append(" ").append(getCondition(condition));
    log.debug(builder.toString());
    return builder.toString();
  }
}