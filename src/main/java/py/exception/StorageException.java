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
