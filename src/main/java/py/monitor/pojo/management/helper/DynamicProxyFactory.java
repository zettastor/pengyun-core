/**
* Copyright (C) 2013-2024 Nanjing Pengyun Network Technology Co., Ltd.
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
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
