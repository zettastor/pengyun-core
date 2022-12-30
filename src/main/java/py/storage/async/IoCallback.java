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
