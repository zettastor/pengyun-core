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

package py.monitor.pojo.management.helper;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.MethodDescriptor;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import javax.management.Attribute;
import javax.management.DynamicMBean;
import py.monitor.pojo.util.Preconditions;

public class DynamicProxyFactory {
  private DynamicProxyFactory() {
  }

  @SuppressWarnings("unchecked")
  public static <T> T createDynamicProxy(DynamicMBean dynamicMbean, Class<T> type)
      throws IntrospectionException {
    return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class[]{type},
        new DynamicMBeanInvokationHandler(dynamicMbean, type));
  }

  private static class DynamicMBeanInvokationHandler implements InvocationHandler {
    final DynamicMBean dynamicMbean;
    private Map<Method, String[]> operationSignatures = new HashMap<Method, String[]>();
    private Map<Method, String> getters = new HashMap<Method, String>();
    private Map<Method, String> setters = new HashMap<Method, String>();

    public DynamicMBeanInvokationHandler(DynamicMBean dynamicMbean, Class<?> type) {
      Preconditions.notNull(dynamicMbean);
      Preconditions.notNull(type);

      this.dynamicMbean = dynamicMbean;

      initializeSignatures(type);
    }

    private void initializeSignatures(Class<?> type) {
      BeanInfo beanInfo;
      try {
        beanInfo = Introspector.getBeanInfo(type);
      } catch (IntrospectionException e) {
        throw new RuntimeException(e);
      }
     
      PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
      for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
        String property = propertyDescriptor.getName();
        addNotNull(getters, propertyDescriptor.getReadMethod(), property);
        addNotNull(setters, propertyDescriptor.getWriteMethod(), property);
      }

      MethodDescriptor[] methodDescriptors = beanInfo.getMethodDescriptors();
      for (MethodDescriptor methodDescriptor : methodDescriptors) {
        Method method = methodDescriptor.getMethod();
        operationSignatures.put(method, createSignature(method.getParameterTypes()));
      }
    }

    private <T> void addNotNull(Map<Method, String> properties, Method method, String name) {
      if (method != null) {
        properties.put(method, name);
      }
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      String getterAttribute = getters.get(method);
      if (getterAttribute != null) {
        return dynamicMbean.getAttribute(getterAttribute);
      }
      String setterAttribute = setters.get(method);
      if (setterAttribute != null) {
        if (args == null || args.length != 1) {
          throw new IllegalArgumentException(
              String.format("Setter method was called with != 1 arguments: %s", method));
        }
        dynamicMbean.setAttribute(new Attribute(setterAttribute, args[0]));
        return null;
      }

      String[] operationSignature = operationSignatures.get(method);
      if (operationSignature != null) {
        return dynamicMbean.invoke(method.getName(), args, operationSignature);
      }
      throw new IllegalArgumentException("Unknown method: " + method);
    }

    private String[] createSignature(Class<?>[] parameterTypes) {
     
      String[] signature = new String[parameterTypes.length];
      for (int i = 0; i < parameterTypes.length; i++) {
        signature[i] = parameterTypes[i].getName();
      }
      return signature;
    }

  }
}
