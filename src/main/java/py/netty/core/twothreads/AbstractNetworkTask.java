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

package py.netty.core.twothreads;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.NotImplementedException;
import py.engine.Task;

public abstract class AbstractNetworkTask implements Task {
  @Override
  public void destroy() {
    throw new NotImplementedException("");
  }

  @Override
  public void cancel() {
    throw new NotImplementedException("");
  }

  @Override
  public boolean isCancel() {
    throw new NotImplementedException("");
  }

  @Override
  public int getToken() {
    throw new NotImplementedException("");
  }

  @Override
  public void setToken(int token) {
    throw new NotImplementedException("");
  }

  @Override
  public long getDelay(TimeUnit unit) {
    throw new NotImplementedException("");
  }

  @Override
  public int compareTo(Delayed o) {
    throw new NotImplementedException("");
  }
}
