
package py.store;

import py.monitor.exception.EmptyStoreException;

public interface IntLoader<PathT> {
  public void from(PathT path) throws EmptyStoreException, Exception;
}
