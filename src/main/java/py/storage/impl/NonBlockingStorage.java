

package py.storage.impl;

import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;
import py.exception.StorageException;

public abstract class NonBlockingStorage extends AsyncStorage {
  public NonBlockingStorage(String identifier) {
    super(identifier);
  }

  public abstract int availablePermitsForWrite();

  public abstract int availablePermitsForRead();

  public abstract boolean preWrite(int permits);

  public abstract boolean preWrite(int permits, Boolean block);

  public abstract void discardWrite(int permits);

  public abstract boolean preRead(int permits);

  public abstract void discardRead(int permits);

  public abstract <A> void nonBlockingWrite(ByteBuffer buffer, long offset, A attachment,
      CompletionHandler<Integer, A> handler) throws StorageException;

  public abstract <A> void nonBlockingRead(ByteBuffer buffer, long offset, A attachment,
      CompletionHandler<Integer, A> handler) throws StorageException;
}
