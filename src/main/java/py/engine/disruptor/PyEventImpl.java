
package py.engine.disruptor;

public class PyEventImpl<T> implements PyEvent<T> {
  private T data;

  @Override
  public T getData() {
    return data;
  }

  @Override
  public void setData(T data) {
    this.data = data;
  }
}
