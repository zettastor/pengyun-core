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

import com.mchange.v2.c3p0.PooledDataSource;
import java.sql.SQLException;
import org.apache.log4j.Logger;

/**
 * This bean simply blocks until there are active connections in the given pool.
 */
public class ConnectionPoolWaiter {
  private static final Logger logger = Logger.getLogger(ConnectionPoolWaiter.class);

  private static final int DEFAULT_TIMEOUT_WAIT_FOR_ACTIVE_CONNECTION_MS = 10 * 1000;

  public ConnectionPoolWaiter(PooledDataSource pool) throws SQLException, InterruptedException {
    logger.info("Waiting until the connection pool has active connections...");

    int amountOfConnections = 0;
    long startTime = System.currentTimeMillis();
    while ((amountOfConnections = pool.getNumConnectionsAllUsers()) == 0) {
      logger.info("No connection got, let's wait 1 seconds more ...");
      if (System.currentTimeMillis() - startTime < DEFAULT_TIMEOUT_WAIT_FOR_ACTIVE_CONNECTION_MS) {
        Thread.sleep(500);
      } else {
        throw new SQLException("can not build connection with db: {}" + pool.getDataSourceName());
      }
    }

    logger.info(amountOfConnections + " connection(s) established...");
  }
}