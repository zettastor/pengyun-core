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

package py.storage.async;

import java.nio.channels.CompletionHandler;
import py.datanode.exception.StorageIoException;
import py.exception.StorageException;

class IoCallback<A> implements Callback {
  private final long offset;
  private final int length;
  private final A attachment;
  private final CompletionHandler<Integer, A> completionHandler;

  IoCallback(long offset, int length, A attachment,
      CompletionHandler<Integer, A> completionHandler) {
    this.offset = offset;
    this.length = length;
    this.attachment = attachment;
    this.completionHandler = completionHandler;
  }

  @Override
  public void done(int errCode) {
    switch (errCode) {
      case AsyncFileAccessor.ERR_SUCCESS:
        completionHandler.completed(length, attachment);
        break;
      case AsyncFileAccessor.ERR_FAIL:
        completionHandler.failed(new StorageIoException(offset, length), attachment);
        break;
      default:
        completionHandler.failed(new StorageException("unknown error code"), attachment);
        break;
    }
  }
}
