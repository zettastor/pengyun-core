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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DBOperator provide APIs to execute SQL statements on databases.
 *
 * <p>The way to use it is:
 * DBOperator dbOperator = new DBOperator(host, user, passwd); dbOperator.use(dbName);
 * dbOperator.executeSQL();
 */
public class DbOperator {
  private static String SYNC_OBJ = "sync";
  private static Hashtable hashDbNames = new Hashtable();
  private static Hashtable hashConnections = new Hashtable();
  private static Logger logger = Logger.getLogger(DbOperator.class.getName());

  static {
    try {
      Class.forName("com.mysql.jdbc.Driver");
    } catch (Exception e) {
      System.out.println("can't load com.mysql.jdbc.driver class, check your classpath");
      System.exit(1);
    }
  }

  private String encodingString = "useUnicode=true&characterEncoding=utf8";
  private String hostName = null;
  private String userName = null;
  private String password = null;
  private String userAndPasswd = null;
  private String prefixConnectionString = null;

  private Connection connection;

  public DbOperator(String host, String user, String passwd) {
    hostName = host;
    userName = user;
    password = passwd;
    userAndPasswd = "user=" + user + "&password=" + password;
    prefixConnectionString = "jdbc:mysql://" + hostName + "/";
  }

  public static void main(String[] args) {
    DbOperator db = new DbOperator("localhost", "root", "6729lc");
    db.use("universities");
    ResultSet srs;
    try {
      // srs = DbException.executeSQL("insert into stores values (4, \"?????????\",
      // \"?????????48\")");
      srs = db.executeSql("SELECT url FROM departments",
          SqlTypes.SELECT);

      if (srs != null) {
        while (srs.next()) {
          String url = srs.getString("url");
          System.out.println(url);
        }
      }

      srs.close();
      db.closeDb("universities");
    } catch (SQLException e) {
      return;
    }

  }

  public boolean use(String dbName) {
    if (dbName == null) {
      return false;
    }

    synchronized (SYNC_OBJ) {
      connection = (Connection) hashConnections.get(dbName);

      if (connection == null) {
        connection = getConnection(dbName);
        if (connection == null) {
          return false;
        } else {
          hashConnections.put(dbName, connection);
        }
      }
      return true;
    }
  }

  public void closeDb(String dbName) {
    synchronized (SYNC_OBJ) {
      connection = (Connection) hashConnections.get(dbName);
    }

    try {
      if (connection != null) {
        connection.close();
      }
    } catch (SQLException e) {
      logger.log(Level.WARNING, "can't close the connection to " + dbName);
      e.printStackTrace();
    }
  }

  public ResultSet executeSql(String sqlStatement, SqlTypes types) throws SQLException {
    return executeSql(sqlStatement, types, 0);
  }

  public ResultSet executeSql(String sqlStatement, SqlTypes types, int fetchSize)
      throws SQLException {
    if (connection == null) {
      return null;
    }

    try {
      Statement statement = connection.createStatement(
          ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);

      if (fetchSize > 0) {
        statement.setFetchSize(fetchSize);
      }
      switch (types) {
        case INSERT:
        case UPDATE:
          statement.executeUpdate(sqlStatement);
          statement.close();
          return null;
        case SELECT:
          ResultSet resultSet = statement.executeQuery(sqlStatement);
          return resultSet;
        default:
          return null;
      }
    } catch (SQLException ex) {
      // handle any errors
      System.out.println("something wrong with the sqlStatement string: " + sqlStatement);
      System.out.println("SQLException: " + ex.getMessage());
      System.out.println("SQLState: " + ex.getSQLState());
      System.out.println("VendorError: " + ex.getErrorCode());
      throw ex;
    }
  }

  private Connection getConnection(String dbName) {
    String connectionString = null;
    try {
      connectionString = prefixConnectionString + dbName + "?"
          + userAndPasswd + "&" + encodingString;
      logger.info("connection string is : " + connectionString);
      Connection conn = DriverManager.getConnection(connectionString);
      return conn;

    } catch (SQLException ex) {
      // handle any errors
      System.out.println("something wrong with the connection String"
          + connectionString);
      System.out.println("SQLException: " + ex.getMessage());
      System.out.println("SQLState: " + ex.getSQLState());
      System.out.println("VendorError: " + ex.getErrorCode());
      return null;
    }
  }

  public enum SqlTypes {
    INSERT, UPDATE, SELECT
  }
}
