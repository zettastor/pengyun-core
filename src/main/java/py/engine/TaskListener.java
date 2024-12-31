

package py.engine;

public interface TaskListener<R extends Result> {
  public void response(R result);
}
