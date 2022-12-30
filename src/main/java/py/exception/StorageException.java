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

package py.exception;

public class StorageException extends Exception {
  private static final long serialVersionUID = 1L;
  protected boolean ioException = false;
  private long offset;
  private long length;

  public StorageException() {
    super();
  }

  public StorageException(String s) {
    super(s);
  }

  public StorageException(Throwable ex1) {
    super(ex1);
  }

  public StorageException(String s, Throwable ex1) {
    super(s, ex1);
  }

  public StorageException(long offset, long length) {
    super();
    this.offset = offset;
    this.length = length;
  }

  public StorageException setOffsetAndLength(long offset, long length) {
    this.offset = offset;
    this.length = length;
    return this;
  }

  public boolean isIoException() {
    return ioException;
  }

  public StorageException setIoException(boolean ioException) {
    this.ioException = ioException;
    return this;
  }

  public long getOffset() {
    return offset;
  }

  public void setOffset(long offset) {
    this.offset = offset;
  }

  public long getLength() {
    return length;
  }

  public void setLength(long length) {
    this.length = length;
  }

  @Override
  public String toString() {
    return "StorageException{super=" + super.toString() + ", offset=" + offset + ", length="
        + length
        + ", ioException=" + ioException + '}';
  }
}
