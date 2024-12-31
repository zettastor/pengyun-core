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

package py.common;

public class Constants {
  /**
   * {@link PyService} is encouraged.
   */
  @Deprecated
  public static final String DIH_INSTANCE_NAME = "DIH";
  /**
   * {@link PyService} is encouraged.
   */
  @Deprecated
  public static final String DATANODE_INSTANCE_NAME = "DataNode";
  /**
   * {@link PyService} is encouraged.
   */
  @Deprecated
  public static final String CONSOLE_INSTANCE_NAME = "Console";
  /**
   * {@link PyService} is encouraged.
   */
  @Deprecated
  public static final String INFO_CENTER_INSTANCE_NAME = "InfoCenter";
  /**
   * {@link PyService} is encouraged.
   */
  @Deprecated
  public static final String DRIVER_CONTAINER_INSTANCE_NAME = "DriverContainer";
  /**
   * {@link PyService} is encouraged.
   */
  @Deprecated
  public static final String CONTROL_CENTER_INSTANCE_NAME = "ControlCenter";
  /**
   * {@link PyService} is encouraged.
   */
  @Deprecated
  public static final String SCRIPT_CONTAINER_INSTANCE_NAME = "ScriptContainer";
  /**
   * {@link PyService} is encouraged.
   */
  @Deprecated
  public static final String COORDINATOR_INSTANCE_NAME = "Coordinator";

  public static final long SUPERADMIN_ACCOUNT_ID = 1862755152385798543L;
  public static final String SUPERADMIN_DEFAULT_ACCOUNT_NAME = "admin";
  public static final String SUPERADMIN_DEFAULT_ACCOUNT_PASSWORD = "admin";
  public static final String SUPERADMIN_ACCOUNT_TYPE = "SuperAdmin";
  public static final int SECTOR_SIZE = 512;
  public static final String DEFAULT_PASSWORD = "admin";
  /**
   * nbd-client queue size is 2048, when nbd-client is disconnected, still has 2048 requests need to
   * be handle, consider requests are all write type, and each byte can be build to one log, one
   * requests is 128K at max, above this, we can calc max logs number.
   */
  public static final long INCREMENT_NUM_OF_LOGS_ONE_CONNECTION = 4096 * 128 * 1024 * 1024L;
  public static final int NOT_FORMAT = -1;
  public static int DEPLOYMENT_DAEMON_PORT = 10002;
  public static int CONTROL_CENTER_PORT = 8010;
  public static int INFO_CENTER_PORT = 8020;
  public static int DIH_PORT = 10000;
  public static int CONSOLE_PORT = 8080;
  public static int DATANODE_PORT = 10011;
  public static int MONITOR_CENTER_PORT = 11000;
  public static int DRIVER_CONTAINER_PORT = 9000;
  public static int MAX_storagePool = 9000;
  public static int MAX_STORAGE_POOL_LENGTH = 250;
  public static String SPECIAL_IO_LIMIT_NAME_FOR_CSI = "多路径校正时间窗";
}
