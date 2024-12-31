
package py.engine.disruptor;

import com.lmax.disruptor.EventFactory;

public class PyEventFactory implements EventFactory<PyEvent> {
  @Override
  public PyEvent newInstance() {
    return new PyEventImpl();
  }
}
