

package py.storage.async;

import py.exception.StorageException;
import py.storage.Storage;
import py.storage.StorageExceptionHandlerChain;

public class SlowDiskCallback implements Callback {
  Storage storage;
  StorageExceptionHandlerChain handlerChain;

  public SlowDiskCallback(Storage storage, StorageExceptionHandlerChain handlerChain) {
    this.storage = storage;
    this.handlerChain = handlerChain;
  }

  @Override
  public void done(int errCode) {
    StorageException storageException = new StorageException();
    storageException.setIoException(true);
    handlerChain.processException(storage, storageException);
  }
}
