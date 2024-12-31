
package py.consumer;

import java.util.Collection;

public interface ConsumerService<E> {
  void start();

  void stop();

  boolean submit(E element);

  default int submit(Collection<E> elements) {
    int successCount = 0;
    for (E element : elements) {
      successCount += submit(element) ? 1 : 0;
    }
    return successCount;
  }

}
