

package py.storage;

import py.exception.StorageException;

public interface StorageExceptionHandler {
  void handle(Storage storage, StorageException exception);

}
