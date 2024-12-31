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

package py.monitor.exception;

public class NotExistedException extends Exception {
  private static final long serialVersionUID = 1L;

  public NotExistedException() {
    super();
  }

  public NotExistedException(String message) {
    super(message);
  }

  public NotExistedException(String message, Throwable cause) {
    super(message, cause);
  }

  public NotExistedException(Throwable cause) {
    super(cause);
  }
}
