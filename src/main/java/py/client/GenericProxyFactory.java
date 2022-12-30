/*
 * Copyright (c) 2022-2022. PengYunNetWork
 *
 * This program is free software: you can use, redistribute, and/or modify it
 * under the terms of the GNU Affero General Public License, version 3 or later ("AGPL"),
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 *  You should have received a copy of the GNU Affero General Public License along with
 *  this program. If not, see <http://www.gnu.org/licenses/>.
 */

package py.client;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.annotation.Retry;

public class GenericProxyFactory<DelegateInterfaceT> {
  private static final Logger logger = LoggerFactory.getLogger(GenericProxyFactory.class);
  protected DelegateInterfaceT delegate;
  protected Class<DelegateInterfaceT> rootClass;

  public GenericProxyFactory(Class<DelegateInterfaceT> rootClass,
      DelegateInterfaceT delegate) {
    this.rootClass = rootClass;
    this.delegate = delegate;
  }

  @SuppressWarnings("unchecked")
  public DelegateInterfaceT createJavassistDynamicProxy(Class<?>[] types, Object[] proxyArgs)
      throws Exception {
    ProxyFactory proxyFactory = new ProxyFactory();
    proxyFactory.setSuperclass(delegate.getClass());
    DelegateInterfaceT javassistProxy = (DelegateInterfaceT) proxyFactory
        .create(types, proxyArgs);
    ((ProxyObject) javassistProxy).setHandler(new JavaAssitInterceptor(delegate));
    return javassistProxy;
  }

  @SuppressWarnings("unchecked")
  public DelegateInterfaceT createJdkDynamicProxy() throws Exception {
    DelegateInterfaceT jdkProxy = (DelegateInterfaceT) Proxy.newProxyInstance(
        ClassLoader.getSystemClassLoader(), new Class[]{rootClass}, new JdkHandler(delegate));
    return jdkProxy;
  }

  @SuppressWarnings("unchecked")
  public DelegateInterfaceT createCglibDynamicProxy() throws Exception {
    Enhancer enhancer = new Enhancer();
    enhancer.setCallback(new CglibInterceptor(delegate));
    enhancer.setInterfaces(new Class[]{rootClass});
    DelegateInterfaceT cglibProxy = (DelegateInterfaceT) enhancer.create();
    return cglibProxy;
  }

  public void throwException(Throwable t) throws Throwable {
    Throwable cause = t.getCause();
    if (cause == null) {
      throw t;
    } else {
      throw cause;
    }
  }

  public DelegateInterfaceT getDelegate() {
    return delegate;
  }

  public void setDelegate(DelegateInterfaceT delegate) {
    this.delegate = delegate;
  }

  public Class<DelegateInterfaceT> getRootClass() {
    return rootClass;
  }

  public void setRootClass(Class<DelegateInterfaceT> rootClass) {
    this.rootClass = rootClass;
  }

  private static class CglibInterceptor implements MethodInterceptor {
    final Object delegate;

    CglibInterceptor(Object delegate) {
      this.delegate = delegate;
    }

    @Override
    public Object intercept(Object object, Method method, Object[] objects, MethodProxy methodProxy)
        throws Throwable {
     
     
     
      return methodProxy.invoke(delegate, objects);
    }
  }

  private static class JdkHandler implements InvocationHandler {
    private Object delegate;

    JdkHandler(Object delegate) {
      this.delegate = delegate;
    }

    @Override
    public Object invoke(Object object, Method method, Object[] objects) throws Throwable {
      Annotation[] annotations = method.getDeclaredAnnotations();
      Object o = null;
      for (Annotation annotation : annotations) {
        if (annotation.getClass().getName().equals("Retry")) {
          Retry retry = (Retry) annotation;
          int retryTime = retry.times();
          int retryPeriod = retry.period();

          Validate.isTrue(retryTime >= 0);
          Validate.isTrue(retryPeriod >= 0);

          while (retryTime-- > 0) {
            try {
              logger.debug("Invoke {}", delegate.getClass().getName());
              if (objects != null) {
                for (Object arg : objects) {
                  logger.debug("Args : {}", arg);
                }
              }
              o = method.invoke(delegate, objects);
              break;
            } catch (Exception t) {
              logger.debug("Execute failure, try again. {} times remain", retryTime);
              if (retryTime > 0) {
                Thread.sleep(retryPeriod * 1000);
                continue;
              } else {
                Throwable cause = t.getCause();
                if (cause == null) {
                  throw t;
                } else {
                  throw cause;
                }
              }
            }
          }
        }
      }

      return o;
    }
  }

  public class JavaAssitInterceptor implements MethodHandler {
    private Object delegate;

    JavaAssitInterceptor(Object delegate) {
      this.delegate = delegate;
    }

    public Object invoke(Object self, Method m, Method proceed, Object[] args) throws Throwable {
      Object object = null;

      Annotation[] annotations = m.getDeclaredAnnotations();
      if (annotations.length == 0) {
       
        try {
          object = m.invoke(delegate, args);
        } catch (Throwable t) {
          throwException(t);
        }
      } else {
       
        Annotation annotation = null;
        boolean find = false;
        for (Annotation a : annotations) {
          logger.debug("Current annotation: {}", a.toString());
          if (a instanceof Retry) {
            annotation = a;
            find = true;
            break;
          }
        }

        if (!find) {
         
          try {
            object = m.invoke(delegate, args);
          } catch (Throwable t) {
            throwException(t);
          }
        } else {
          Retry retry = (Retry) annotation;
          int retryTimes = retry.times();
          int retryPeriod = retry.period();
          List<Class<? extends Throwable>> exceptions = Arrays.asList(retry.when());

          Validate.isTrue(retryTimes >= 0);
          Validate.isTrue(retryPeriod >= 0);
          while (retryTimes-- > 0) {
            try {
              logger.debug("Invoke {},{} times remain", delegate.getClass().getName(), retryTimes);
              object = m.invoke(delegate, args);
              break;
            } catch (Throwable t) {
              logger.debug("Execute failure, try again");
              if (exceptions.contains(t.getCause().getClass())) {
                if (retryTimes > 0) {
                  Thread.sleep(retryPeriod * 1000);
                  continue;
                } else {
                  throwException(t);
                }
              } else {
                logger.debug("Althougth the METHOD:[{}.{}] is annotated by 'Retry',but the "
                        + "exception threw out is not in the checking list of 'Retry' anntation",
                    delegate.getClass().getName(), m.getName());
                throwException(t);
              }
            }
          }
        }
      }
      return object;
    }

    public Object getDelegate() {
      return delegate;
    }

    public void setDelegate(Object delegate) {
      this.delegate = delegate;
    }
  }
}
