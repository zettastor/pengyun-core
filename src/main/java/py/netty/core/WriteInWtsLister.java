

package py.netty.core;

public interface WriteInWtsLister<T> {
  void complete(T object);
}
