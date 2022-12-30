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
