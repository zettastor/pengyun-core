

package py.engine.disruptor;

public interface PyEvent<T> {
  public T getData();

  public void setData(T data);
}
