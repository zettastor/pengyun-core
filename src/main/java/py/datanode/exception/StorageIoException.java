
package py.datanode.exception;

import py.exception.StorageException;

public class StorageIoException extends StorageException {
  public StorageIoException(long offset, long length) {
    setOffset(offset);
    setLength(length);
    ioException = true;
  }
}
