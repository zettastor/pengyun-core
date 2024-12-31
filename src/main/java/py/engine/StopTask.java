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

package py.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StopTask extends DelayedTask {
  private static final Logger logger = LoggerFactory.getLogger(StopTask.class);

  public StopTask() {
    super(0);
  }

  @Override
  public void doWork() {
  }

  @Override
  public void destroy() {
  }

  @Override
  public void cancel() {
  }

  @Override
  public boolean isCancel() {
    return true;
  }

  @Override
  public int getToken() {
    return 1;
  }

  @Override
  public void setToken(int expectedToken) {
  }

  @Override
  public Result work() {
    logger.warn("this is a stop task");
    return null;
  }
}
