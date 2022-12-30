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

package py.db.sqlite.dialect;

import java.sql.Types;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.function.SQLFunctionTemplate;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.dialect.function.VarArgsSQLFunction;
import org.hibernate.type.IntegerType;
import org.hibernate.type.StringType;

@Deprecated
public class SqliteDialect extends Dialect {
  public SqliteDialect() {
    super();
    registerColumnType(Types.BIT, "integer");
    registerColumnType(Types.TINYINT, "tinyint");
    registerColumnType(Types.SMALLINT, "smallint");
    registerColumnType(Types.INTEGER, "integer");
    registerColumnType(Types.BIGINT, "bigint");
    registerColumnType(Types.FLOAT, "float");
    registerColumnType(Types.REAL, "real");
    registerColumnType(Types.DOUBLE, "double");
    registerColumnType(Types.NUMERIC, "numeric");
    registerColumnType(Types.DECIMAL, "decimal");
    registerColumnType(Types.CHAR, "char");
    registerColumnType(Types.VARCHAR, "varchar");
    registerColumnType(Types.LONGVARCHAR, "longvarchar");
    registerColumnType(Types.DATE, "date");
    registerColumnType(Types.TIME, "time");
    registerColumnType(Types.TIMESTAMP, "timestamp");
    registerColumnType(Types.BINARY, "blob");
    registerColumnType(Types.VARBINARY, "blob");
    registerColumnType(Types.LONGVARBINARY, "blob");

    registerColumnType(Types.BLOB, "bytea");
    registerColumnType(Types.CLOB, "clob");
    registerColumnType(Types.BOOLEAN, "integer");

    registerFunction("concat", new VarArgsSQLFunction(StringType.INSTANCE, "", "||", ""));
    registerFunction("mod", new SQLFunctionTemplate(IntegerType.INSTANCE, "?1 % ?2"));
    registerFunction("substr", new StandardSQLFunction("substr", StringType.INSTANCE));
    registerFunction("substring", new StandardSQLFunction("substr", StringType.INSTANCE));
  }

  public boolean supportsInsertSelectIdentity() {
    return true;
  }

  @Override
  public boolean supportsLimit() {
    return true;
  }

  @Override
  public boolean bindLimitParametersInReverseOrder() {
    return true;
  }

  @Override
  public String getLimitString(String query, boolean hasOffset) {
    return new StringBuffer(query.length() + 20).append(query)
        .append(hasOffset ? " limit ? offset ?" : " limit ?")
        .toString();
  }

  @Override
  public boolean supportsCurrentTimestampSelection() {
    return true;
  }

  @Override
  public boolean isCurrentTimestampSelectStringCallable() {
    return false;
  }

  @Override
  public String getCurrentTimestampSelectString() {
    return "select current_timestamp";
  }

  @Override
  public boolean supportsUnionAll() {
    return true;
  }

  @Override
  public boolean hasAlterTable() {
    return false;
  }

  @Override
  public boolean dropConstraints() {
    return false;
  }

  @Override
  public String getAddColumnString() {
    return "add column";
  }

  @Override
  public String getForUpdateString() {
    return "";
  }

  @Override
  public boolean supportsOuterJoinForUpdate() {
    return false;
  }

  @Override
  public String getDropForeignKeyString() {
    throw new UnsupportedOperationException(
        "No drop foreign key syntax supported by SQLiteDialect");
  }

  @Override
  public String getAddForeignKeyConstraintString(String constraintName, String[] foreignKey,
      String referencedTable,
      String[] primaryKey, boolean referencesPrimaryKey) {
    throw new UnsupportedOperationException("No add foreign key syntax supported by SQLiteDialect");
  }

  @Override
  public String getAddPrimaryKeyConstraintString(String constraintName) {
    throw new UnsupportedOperationException("No add primary key syntax supported by SQLiteDialect");
  }

  @Override
  public boolean supportsIfExistsBeforeTableName() {
    return true;
  }

  @Override
  public boolean supportsCascadeDelete() {
    return false;
  }
}