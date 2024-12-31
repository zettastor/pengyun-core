
package py.common.lock;

public interface HashLock<T> {
  void lock(T val) throws InterruptedException;

  boolean tryLock(T val);

  void unlock(T val);

}
