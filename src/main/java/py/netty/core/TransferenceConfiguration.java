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
