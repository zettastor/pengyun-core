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

package py.netty.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import py.netty.client.TransferenceClientOption;
import py.netty.server.TransferenceServerOption;

public class TransferenceConfiguration {
  private Map<TransferenceOption<?>, Object> options
      = new ConcurrentHashMap<TransferenceOption<?>, Object>();

  public TransferenceConfiguration() {
    option(TransferenceOption.MAX_MESSAGE_LENGTH, 5 * 1024 * 1024);
  }

  public static TransferenceConfiguration defaultConfiguration() {
    TransferenceConfiguration clientConfiguartion = new TransferenceConfiguration();
    clientConfiguartion.option(TransferenceClientOption.IO_CONNECTION_TIMEOUT_MS, 3000);
    clientConfiguartion.option(TransferenceClientOption.IO_TIMEOUT_MS, 10000);
    clientConfiguartion.option(TransferenceClientOption.CONNECTION_COUNT_PER_ENDPOINT, 1);
    clientConfiguartion
        .option(TransferenceClientOption.MAX_LAST_TIME_FOR_CONNECTION_MS, 60 * 1000 * 15);

    clientConfiguartion.option(TransferenceOption.MAX_BYTES_ONCE_ALLOCATE, 16 * 1024);

    clientConfiguartion.option(TransferenceClientOption.CLIENT_IO_EVENT_GROUP_THREADS_MODE,
        IoEventThreadsMode.Fix_Threads_Mode);
    clientConfiguartion
        .option(TransferenceClientOption.CLIENT_IO_EVENT_GROUP_THREADS_PARAMETER, 2.0f);
    clientConfiguartion.option(TransferenceClientOption.CLIENT_IO_EVENT_HANDLE_THREADS_MODE,
        IoEventThreadsMode.Fix_Threads_Mode);
    clientConfiguartion
        .option(TransferenceClientOption.CLIENT_IO_EVENT_HANDLE_THREADS_PARAMETER, 4.0f);

    clientConfiguartion.option(TransferenceServerOption.IO_EVENT_GROUP_THREADS_MODE,
        IoEventThreadsMode.Fix_Threads_Mode);
    clientConfiguartion.option(TransferenceServerOption.IO_EVENT_GROUP_THREADS_PARAMETER, 2.0f);
    clientConfiguartion.option(TransferenceServerOption.IO_EVENT_HANDLE_THREADS_MODE,
        IoEventThreadsMode.Fix_Threads_Mode);
    clientConfiguartion.option(TransferenceServerOption.IO_EVENT_HANDLE_THREADS_PARAMETER, 4.0f);
    return clientConfiguartion;
  }

  public <T> TransferenceConfiguration option(TransferenceOption<T> option, T value) {
    options.put(option, value);
    return this;
  }

  public <T> Object valueOf(TransferenceOption<T> option) {
    return options.get(option);
  }
}
