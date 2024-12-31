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

package py.netty.exception;

import io.netty.buffer.ByteBuf;

public class SnapshotMismatchException extends AbstractNettyException {
  private static final long serialVersionUID = 8898558822099233894L;

  private byte[] snapshotManager;

  public SnapshotMismatchException(byte[] snapshotManager) {
    super(ExceptionType.SNAPSHOTMISMATCH);
    this.snapshotManager = snapshotManager;
  }

  public static SnapshotMismatchException fromBuffer(ByteBuf buffer) {
    byte[] snapshotManager = new byte[buffer.readableBytes()];
    buffer.readBytes(snapshotManager);
    return new SnapshotMismatchException(snapshotManager);
  }

  @Override
  public int getSize() {
    return Integer.BYTES + snapshotManager.length;
  }

  @Override
  public void toBuffer(ByteBuf buffer) {
    buffer.writeInt(getExceptionType().getValue());
    buffer.writeBytes(snapshotManager);
  }

  public byte[] getSnapshotManager() {
    return snapshotManager;
  }

  public void setSnapshotManager(byte[] bytes) {
    snapshotManager = bytes;
  }
}
