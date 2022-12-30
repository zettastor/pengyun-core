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

import static java.lang.String.format;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.MethodDescriptor;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.management.Attribute;
import javax.management.AttributeChangeNotification;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.IntrospectionException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.monitor.pojo.management.PeriodicPojo;
import py.monitor.pojo.management.annotation.Description;
import py.monitor.pojo.management.annotation.MBean;
import py.monitor.pojo.management.annotation.MBean.AutomaticType;
import py.monitor.pojo.management.annotation.ManagedAttribute;
import py.monitor.pojo.management.annotation.ManagedOperation;
import py.monitor.pojo.management.annotation.ManagedOperation.Impact;
import py.monitor.pojo.management.annotation.Parameter;
import py.monitor.pojo.management.exception.ManagementException;
import py.monitor.pojo.util.Objects;

public class IntrospectedDynamicMBean extends NotificableDynamicMBean {
  private static final Logger logger = LoggerFactory.getLogger(IntrospectedDynamicMBean.class);
  private final PeriodicPojo pojo;
  private final Class<?> mbeanClass;
  private final MBeanRegistration registrationDelegate;
  private final Map<String, PropertyDescriptor> propertyDescriptors;
  private final Map<String, Method> operationMethods;
  private final MBeanInfo mbeanInfo;

  public IntrospectedDynamicMBean(PeriodicPojo pojo) throws ManagementException {
    logger.debug("IntrospectedDynamicMBean constructor");
    this.pojo = pojo;
    this.mbeanClass = pojo.getClass();
    if (!mbeanClass.isAnnotationPresent(MBean.class)) {
      throw new IllegalArgumentException(
          format("MBean %s is not annotated with @%s", mbeanClass, MBean.class.getName()));
    }
    registrationDelegate = (MBeanRegistration) ((pojo instanceof MBeanRegistration) ? pojo
        : new MBeanRegistrationBase());
    try {
      BeanInfo beanInfo = Introspector.getBeanInfo(mbeanClass);
      propertyDescriptors = createPropertyDescriptors(beanInfo);
      operationMethods = createOperationMethods(beanInfo);
      mbeanInfo = createMbeanInfo(mbeanClass, propertyDescriptors, operationMethods);
    } catch (IntrospectionException e) {
      throw new ManagementException(e);
    } catch (java.beans.IntrospectionException e) {
      throw new ManagementException(e);
    }
  }

  private static MBeanInfo createMbeanInfo(Class<?> mbeanClass,
      Map<String, PropertyDescriptor> propertyDescriptors,
      Map<String, Method> operationMethods) throws IntrospectionException, ManagementException {
    String description = description(mbeanClass);
    final MBeanAttributeInfo[] attributeInfo = createAttributeInfo(propertyDescriptors);
    final MBeanConstructorInfo[] constructorInfo = createConstructorInfo();
    final MBeanOperationInfo[] operationInfo = createOperationInfo(operationMethods);
    final MBeanNotificationInfo[] notificationInfo = createNotificationInfo();
    return new MBeanInfo(mbeanClass.getName(), description, attributeInfo, constructorInfo,
        operationInfo,
        notificationInfo);
  }

  private static MBeanNotificationInfo[] createNotificationInfo() {
    String[] types = new String[]{AttributeChangeNotification.ATTRIBUTE_CHANGE};
    String name = AttributeChangeNotification.class.getName();
    String description = "An attribute of this MBean has changed";
    MBeanNotificationInfo info = new MBeanNotificationInfo(types, name, description);
    return new MBeanNotificationInfo[]{info};
  }

  private static Map<String, Method> createOperationMethods(BeanInfo beanInfo)
      throws ManagementException {
    Set<Method> allAccessors = allAccessors(beanInfo);
    Map<String, Method> operationMethods = new HashMap<String, Method>();
    for (MethodDescriptor descriptor : beanInfo.getMethodDescriptors()) {
      Method method = descriptor.getMethod();
      logger.debug("{}", method);
      ManagedOperation operationAnnotation = method.getAnnotation(ManagedOperation.class);
      if (operationAnnotation != null && allAccessors.contains(method)) {
        throw new ManagementException(
            String.format("Accessor method %s is annotated as an @%s", method,
                ManagedOperation.class.getName()));
      }

      boolean isAutomatic = isAutomatic(method.getDeclaringClass(), AutomaticType.OPERATION);
      boolean autoOperation = (isAutomatic && isPublicInstance(method) && !allAccessors
          .contains(method));
      if (operationAnnotation != null || autoOperation) {
        Method old = operationMethods.put(method.getName(), method);
        if (old != null) {
          throw new ManagementException(
              format("Multiple Operation annotations for operation %s of %s",
                  method.getName(), old.getDeclaringClass()));
        }
      }
    }

    logger.debug("--------------{}", operationMethods);
    return operationMethods;
  }

  private static boolean isPublicInstance(Method method) {
    int mod = method.getModifiers();
    return Modifier.isPublic(mod) && !Modifier.isStatic(mod);
  }

  private static Set<Method> allAccessors(BeanInfo beanInfo) {
    PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
    Set<Method> accessors = new HashSet<Method>(propertyDescriptors.length * 2);
    for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
      addNotNull(accessors, propertyDescriptor.getReadMethod());
      addNotNull(accessors, propertyDescriptor.getWriteMethod());
    }
    return accessors;
  }

  public static <T> void addNotNull(Collection<T> collection, T element) {
    if (element != null) {
      collection.add(element);
    }
  }

  private static MBeanOperationInfo[] createOperationInfo(Map<String, Method> operationMethods)
      throws ManagementException {
    MBeanOperationInfo[] operationInfos = new MBeanOperationInfo[operationMethods.size()];
    int operationIndex = 0;

    for (String methodName : sortedKeys(operationMethods)) {
      Method method = operationMethods.get(methodName);
      ManagedOperation annotation = method.getAnnotation(ManagedOperation.class);

      MBeanParameterInfo[] signature = createParameterInfo(method);

      Impact impact = annotation == null ? Impact.UNKNOWN : annotation.value();
      int impactValue = impact.impactValue;
      String description = description(method);
      MBeanOperationInfo opInfo = new MBeanOperationInfo(method.getName(), description, signature,
          method.getReturnType().getName(), impactValue);
      operationInfos[operationIndex++] = opInfo;
    }
    return operationInfos;
  }

  private static boolean isAutomatic(Class<?> clazz, AutomaticType autoType) {
    MBean annotation = clazz.getAnnotation(MBean.class);
    if (annotation == null) {
      return false;
    }
    AutomaticType[] values = annotation.automatic();

    for (AutomaticType value : values) {
      if (value == autoType) {
        return true;
      }
    }
    return false;
  }

  private static boolean isAutomatic(PropertyDescriptor property) {
    Method accessor = Objects.firstNotNull(property.getReadMethod(), property.getWriteMethod());
    boolean isAutomatic = isAutomatic(accessor.getDeclaringClass(), AutomaticType.ATTRIBUTE);
    return isAutomatic;
  }

  protected static MBeanParameterInfo[] createParameterInfo(Method method) {
    MBeanParameterInfo[] parameters = new MBeanParameterInfo[method.getParameterTypes().length];
    for (int parameterIndex = 0; parameterIndex < parameters.length; parameterIndex++) {
      final String pType = method.getParameterTypes()[parameterIndex].getName();

      Parameter parameter = getParameterAnnotation(method, parameterIndex, Parameter.class);
      Description description = getParameterAnnotation(method, parameterIndex, Description.class);
      final String pName =
          (parameter != null) ? parameter.value() : "p" + (parameterIndex + 1);
      final String pDesc = (description != null) ? description.value() : null;
      parameters[parameterIndex] = new MBeanParameterInfo(pName, pType, pDesc);
    }
    return parameters;
  }

  private static MBeanConstructorInfo[] createConstructorInfo() {
    return null;
  }

  private static Map<String, PropertyDescriptor> createPropertyDescriptors(BeanInfo beanInfo)
      throws ManagementException {
    Map<String, PropertyDescriptor> properties = new HashMap<String, PropertyDescriptor>();
    for (PropertyDescriptor property : beanInfo.getPropertyDescriptors()) {
      ManagedAttribute getterAnnotation = getAnnotation(property.getReadMethod(),
          ManagedAttribute.class);
      ManagedAttribute setterAnnotation = getAnnotation(property.getWriteMethod(),
          ManagedAttribute.class);
      if (isAutomatic(property) || getterAnnotation != null || setterAnnotation != null) {
        properties.put(property.getName(), property);
      }
    }
    return properties;
  }

  private static MBeanAttributeInfo[] createAttributeInfo(
      Map<String, PropertyDescriptor> propertyDescriptors)
      throws ManagementException, IntrospectionException {
    MBeanAttributeInfo[] infos = new MBeanAttributeInfo[propertyDescriptors.size()];
    int i = 0;

    for (String propertyName : sortedKeys(propertyDescriptors)) {
      PropertyDescriptor property = propertyDescriptors.get(propertyName);
      boolean isAutomatic = isAutomatic(property);
      Method readMethod = property.getReadMethod();
      Method writeMethod = property.getWriteMethod();

      Annotation getterAnnotation = getAnnotation(readMethod, ManagedAttribute.class);
      Annotation setterAnnotation = getAnnotation(writeMethod, ManagedAttribute.class);
      boolean readable = isAutomatic || (null != getterAnnotation);
      boolean writable = isAutomatic || (null != setterAnnotation);
      Description descriptionAnnotation = getSingleAnnotation(property, Description.class,
          readMethod,
          writeMethod);
      String description = (descriptionAnnotation != null) ? descriptionAnnotation.value() : "";

      ManagedAttribute attributeGetterAnno = (ManagedAttribute) getterAnnotation;
      if (getterAnnotation != null) {
        DescriptionEnDecoder descriptionEnDecoder = new DescriptionEnDecoder();
        descriptionEnDecoder.setRange(attributeGetterAnno.range());
        descriptionEnDecoder.setUnitOfMeasurement(attributeGetterAnno.unitOfMeasurement());
        descriptionEnDecoder.setDescription(description);

        description = descriptionEnDecoder.toString();
      }

      MBeanAttributeInfo info = new MBeanAttributeInfo(property.getName(), description,
          readable ? readMethod : null, writable ? writeMethod : null);
      infos[i++] = info;
    }
    return infos;
  }

  private static <T extends Annotation> T getSingleAnnotation(PropertyDescriptor property,
      Class<T> annotationClass,
      AccessibleObject... entities) throws ManagementException {
    T result = null;
    for (AccessibleObject entity : entities) {
      if (entity != null) {
        T annotation = entity.getAnnotation(annotationClass);
        if (annotation != null) {
          if (result != null) {
            throw new ManagementException(
                String.format("Multiple %s annotations found for property %s",
                    annotationClass.getName(), property.getName()));
          }
          result = annotation;
        }
      }
    }
    return result;
  }

  private static <A extends Annotation> A getParameterAnnotation(Method method, int index,
      Class<A> annotationClass) {
    for (Annotation a : method.getParameterAnnotations()[index]) {
      if (annotationClass.isInstance(a)) {
        return annotationClass.cast(a);
      }
    }
    return null;
  }

  private static <A extends Annotation> A getAnnotation(AnnotatedElement element,
      Class<A> annotationClass) {
    return (element != null) ? element.getAnnotation(annotationClass) : null;
  }

  private static String description(AnnotatedElement element) {
    Description annotation = element.getAnnotation(Description.class);
    String explicitValue = (annotation != null) ? annotation.value() : null;
    if (explicitValue != null && !explicitValue.isEmpty()) {
      return explicitValue;
    } else {
      return generatedDescription(element);
    }
  }

  private static String generatedDescription(AnnotatedElement element) {
    if (element instanceof Method) {
      Method method = (Method) element;
      return method.getName() + "() of " + method.getDeclaringClass().getSimpleName();
    } else if (element instanceof Class) {
      return "class " + ((Class<?>) element).getName();

    }
    return element.toString();
  }

  private static List<String> sortedKeys(Map<String, ?> map) {
    List<String> keys = new ArrayList<String>(map.keySet());
    Collections.sort(keys);
    return keys;
  }

  @Override
  public Object getAttribute(String attribute)
      throws AttributeNotFoundException, MBeanException, ReflectionException {
    logger.debug("attribute is : {}", attribute);
    PropertyDescriptor propertyDescriptor = propertyDescriptors.get(attribute);
    if (propertyDescriptor == null) {
      throw new AttributeNotFoundException(attribute);
    }
    Method getter = propertyDescriptor.getReadMethod();
    if (getter == null) {
      throw new AttributeNotFoundException(
          format("Getter method for attribute %s of %s", attribute, mbeanClass));
    }
    try {
      if (!getter.isAccessible()) {
        getter.setAccessible(true);
      }
      return getter.invoke(pojo);
    } catch (InvocationTargetException e) {
      throw new MBeanException((Exception) e.getCause());
    } catch (Exception e) {
      throw new RuntimeException(
          format("Unable to obtain value of attribute %s of %s", attribute, mbeanClass));
    }
  }

  @Override
  public AttributeList getAttributes(String[] attributeNames) {
    logger.debug("IntrospectedDynamicMBean getAttributes");
    AttributeList attributes = new AttributeList(attributeNames.length);
    for (String attributeName : attributeNames) {
      try {
        Attribute attribute = new Attribute(attributeName, getAttribute(attributeName));
        attributes.add(attribute);
      } catch (Exception e) {
        throw new IllegalArgumentException(e);
      }
    }
    return attributes;
  }

  @Override
  public void setAttribute(Attribute attribute)
      throws AttributeNotFoundException, InvalidAttributeValueException,
      MBeanException, ReflectionException {
    String name = attribute.getName();
    PropertyDescriptor propertyDescriptor = propertyDescriptors.get(name);
    if (propertyDescriptor == null) {
      throw new AttributeNotFoundException(name);
    }
    Method setter = propertyDescriptor.getWriteMethod();
    if (setter == null) {
      throw new AttributeNotFoundException(
          format("setter method for attribute %s of %s", name, mbeanClass));
    }
    Object value = attribute.getValue();
    try {
      if (!setter.isAccessible()) {
        setter.setAccessible(true);
      }
      setter.invoke(pojo, value);
    } catch (IllegalArgumentException e) {
      throw new InvalidAttributeValueException(
          String.format("attribute %s, value = (%s)%s, expected (%s)", name,
              value.getClass().getName(), value, setter.getParameterTypes()[0].getName()));
    } catch (IllegalAccessException e) {
      throw new ReflectionException(e,
          format("attribute %s of %s, value = (%s)%s", name, mbeanClass, value.getClass().getName(),
              value));
    } catch (InvocationTargetException e) {
      throw new MBeanException((Exception) e.getCause(),
          format("attribute %s of %s, value = (%s)%s", name, mbeanClass, value.getClass().getName(),
              value));
    }
  }

  @Override
  public AttributeList setAttributes(AttributeList attributes) {
    for (Object object : attributes) {
      Attribute attribute = (Attribute) object;
      try {
        setAttribute(attribute);
      } catch (Exception e) {
        throw new IllegalArgumentException(e);
      }
    }

    return attributes;
  }

  @Override
  public MBeanInfo getMBeanInfo() {
    return mbeanInfo;
  }

  @Override
  public Object invoke(String actionName, Object[] params, String[] signature)
      throws MBeanException, ReflectionException {
    Method method = operationMethods.get(actionName);

    if (method == null) {
      throw new IllegalArgumentException("No such operation: " + actionName);
    }
    try {
      if (!method.isAccessible()) {
        method.setAccessible(true);
      }
      return method.invoke(pojo, params);
    } catch (InvocationTargetException e) {
      throw new MBeanException((Exception) e.getCause());
    } catch (Exception e) {
      throw new IllegalArgumentException(e);
    }

  }

  public ObjectName preRegister(MBeanServer server, ObjectName name) throws Exception {
    return registrationDelegate.preRegister(server, name);
  }

  public void postRegister(Boolean registrationDone) {
    registrationDelegate.postRegister(registrationDone);
  }

  public void postDeregister() {
    registrationDelegate.postDeregister();
  }

  public void preDeregister() throws Exception {
    registrationDelegate.preDeregister();
  }

  @Override
  public String toString() {
    return "IntrospectedDynamicMBean [pojo=" + pojo + ", mbeanClass=" + mbeanClass
        + ", registrationDelegate="
        + registrationDelegate + ", propertyDescriptors=" + propertyDescriptors
        + ", operationMethods="
        + operationMethods + ", mbeanInfo=" + mbeanInfo + "]";
  }

}
