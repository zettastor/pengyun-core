
package py.client;

import java.lang.reflect.Method;

public interface ProxyStrategy<DelegateInterfaceT> {
  public Object doWork(Method m, Object[] args) throws Throwable;
}
