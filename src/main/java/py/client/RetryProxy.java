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
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.annotation.Retry;

public class RetryProxy<DelegateInterfaceT> implements ProxyStrategy<DelegateInterfaceT> {
  private static final Logger logger = LoggerFactory.getLogger(RetryProxy.class);
  private DelegateInterfaceT delegate;

  public RetryProxy(DelegateInterfaceT delegate) {
    super();
    this.delegate = delegate;
  }

  @Override
  public Object doWork(Method m, Object[] args) throws Throwable {
    Object object = null;

    Annotation[] annotations = m.getDeclaredAnnotations();
    if (annotations.length == 0) {
      try {
        object = m.invoke(delegate, args);
      } catch (Throwable t) {
        throw t.getCause().getClass().newInstance();
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
          throw t.getCause().getClass().newInstance();
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
                throw t.getCause().getClass().newInstance();
              }
            } else {
              logger.debug("Althougth the METHOD:[{}.{}] is annotated by 'Retry',but the "
                      + "exception threw out is not in the checking list of 'Retry' anntation",
                  delegate.getClass().getName(), m.getName());
              throw t.getCause().getClass().newInstance();
            }
          }
        }
      }
    }
    return object;
  }

}
