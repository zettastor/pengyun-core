
package py.algorithm.policy;

import java.util.Iterator;

public interface ObjectKeyToArrayIndexMap<T> {
  int getEmptyValue();

  int get(T key);

  void put(T key, int index);

  void remove(T key);

  Iterator<T> keySetIterator();
}
