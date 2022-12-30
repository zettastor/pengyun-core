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