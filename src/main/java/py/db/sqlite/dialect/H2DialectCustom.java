/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package py.db.sqlite.dialect;

import java.sql.Types;
import org.hibernate.dialect.H2Dialect;
import org.hibernate.type.descriptor.sql.BinaryTypeDescriptor;
import org.hibernate.type.descriptor.sql.SqlTypeDescriptor;

/**
 * this class used for junit-test.
 *
 */
public class H2DialectCustom extends H2Dialect {
  public H2DialectCustom() {
    super();
    registerColumnType(Types.BLOB, "bytea");
  }

  @Override
  public SqlTypeDescriptor remapSqlTypeDescriptor(SqlTypeDescriptor sqlTypeDescriptor) {
    if (sqlTypeDescriptor.getSqlType() == Types.BLOB) {
      return BinaryTypeDescriptor.INSTANCE;
    }
    return super.remapSqlTypeDescriptor(sqlTypeDescriptor);
  }
}
