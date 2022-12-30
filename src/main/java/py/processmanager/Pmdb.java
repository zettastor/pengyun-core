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

package py.processmanager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.processmanager.exception.PmdbPathNotExist;

public class Pmdb {
  public static final String SERVICE_STATUS_NAME = "Status";
  public static final String SERVICE_STATUS_NAME_BAK = "Status_bak";
  public static final String PM_PID_NAME = "PMPid";
  public static final String SERVICE_PID_NAME = "SPid";
  public static final String COORDINATOR_PIDS_DIR_NAME = String
      .format("%s_coordinator", SERVICE_PID_NAME);
  private static final Logger logger = LoggerFactory.getLogger(Pmdb.class);
  private Path dbPath;

  public Pmdb(Path dbPath) throws PmdbPathNotExist {
    if (!Files.exists(dbPath)) {
      logger.error("Unable to find path {}", dbPath);
      throw new PmdbPathNotExist();
    }

    this.dbPath = dbPath;
  }

  public static Pmdb build(Path dbPath) throws PmdbPathNotExist {
    Pmdb pmdb = new Pmdb(dbPath);
    return pmdb;
  }

  public boolean save(String fileDbName, String records) {
    logger.debug("Save {} to file {} in {}", records, fileDbName, dbPath.toString());

    BufferedWriter writer = null;
    try {
      writer = new BufferedWriter(new FileWriter(getFile(fileDbName)));
      writer.write(records);
      writer.flush();
    } catch (IOException e) {
      logger.error("Caught an exception when save records to file", e);
      return false;
    } finally {
      if (writer != null) {
        try {
          writer.close();
        } catch (IOException e) {
          logger.error("Caught an exception when close writer of file {}", fileDbName, e);
          return false;
        }
      }
    }

    return true;
  }

  public String get(String fileDbName) {
    logger.debug("Get records from file {} in {}", fileDbName, dbPath.toString());

    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new FileReader(getFile(fileDbName)));

      String line = reader.readLine();
      if (line != null) {
        return line;
      }
    } catch (FileNotFoundException e) {
      logger.error("No such file {} in path {}", fileDbName, dbPath.toString());
      return null;
    } catch (IOException e) {
      logger.error("Caught an io exception when get records from {}", fileDbName, e);
      return null;
    } finally {
      try {
        if (reader != null) {
          reader.close();
        }
      } catch (IOException e) {
        logger.error("caught exception", e);
      }
    }
    return null;
  }

  private File getFile(String fileDbName) {
    String fileDbPath = Paths.get(dbPath.toFile().getAbsolutePath(), fileDbName).toString();

    return new File(fileDbPath);
  }
}
