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

package py.monitor.utils;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DeepCopyer {
  private static final Logger logger = LoggerFactory.getLogger(DeepCopyer.class);

  private static final ThreadLocal<Map<String, Object>> references = new ThreadLocal<>();

  private static final Map<Class<?>, Object> primitiveWrappersMap = new HashMap<>();

  private static final Set<Class<?>> uninstantiableClasses = new HashSet<>();

  static {
    primitiveWrappersMap.put(boolean.class, Boolean.FALSE);
    primitiveWrappersMap.put(byte.class, (byte) 0);
    primitiveWrappersMap.put(short.class, (short) 0);
    primitiveWrappersMap.put(char.class, (char) 0);
    primitiveWrappersMap.put(int.class, 0);
    primitiveWrappersMap.put(long.class, 0L);
    primitiveWrappersMap.put(float.class, 0f);
    primitiveWrappersMap.put(double.class, (double) 0);

    uninstantiableClasses.add(Class.class);
    uninstantiableClasses.add(Void.class);
  }

  private DeepCopyer() {
  }

  @SuppressWarnings("unchecked")
  public static <T> T clone(final T obj) throws ReflectiveOperationException {
    try {
     
      references.set(new HashMap<String, Object>());
      Class<T> clazz = (Class<T>) obj.getClass();
      return getClone(obj, clazz);
    } finally {
      references.get().clear();
      references.remove();
    }
  }

  @SuppressWarnings({"unchecked"})
  public static <T> T cloneByCommonWay(final T obj) throws ReflectiveOperationException {
    if (obj == null) {
      return null;
    }

    Class<?> clazz = obj.getClass();

    if (obj instanceof Cloneable) {
      try {
       
        final Method m = clazz.getDeclaredMethod("clone");
       
        if (m.getReturnType().isInstance(obj)) {
         
          SetMethodAccessible<T> accessible = new SetMethodAccessible<>(m, obj);
          T copy = AccessController.doPrivileged(accessible);
          logger.debug("Returns a copy by clone method");
          return copy;
        }
      } catch (PrivilegedActionException e) {
        logger.error("Attempt to use a clone method failed: {}", e);
      }
    }

    final Constructor<T> ctor;
    final Class<?>[] paramTypes = new Class[]{clazz};
    try {
      ctor = (Constructor<T>) clazz.getDeclaredConstructor(paramTypes);
     
      ctor.setAccessible(true);
      T copy = ctor.newInstance(obj);
      logger.debug("Returns a copy by copy constructor");
      return copy;
    } catch (ReflectiveOperationException e) {
      logger.error("Attempt to use a copy constructor failed: {}", e);
    }
    return clone(obj);
  }

  private static void addToReferencesMap(Object original, Object copy) {
    String key = getUniqueName(original);
    references.get().put(key, copy);
  }

  private static boolean isUninstantiable(Class<?> clazz) {
    return uninstantiableClasses.contains(clazz);
  }

  private static Object constractNewObject(Class<?> clazz) throws ReflectiveOperationException {
   
    try {
      return clazz.newInstance();
    } catch (ReflectiveOperationException e) {
      // do nothing
    }

    Constructor<?>[] constructors = clazz.getDeclaredConstructors();
    if (constructors.length > 0) {
      for (Constructor<?> constructor : constructors) {
        Class<?>[] parameters = constructor.getParameterTypes();
        Object[] args = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
         
          args[i] = parameters[i].isPrimitive() ? getPrimitiveDefault(parameters[i]) : null;
        }

        try {
         
          constructor.setAccessible(true);
          return constructor.newInstance(args);
        } catch (ReflectiveOperationException e) {
         
          continue;
        }
      }
    }
    return null;
  }

  private static Object copyArray(Object array) throws ReflectiveOperationException {
    Class<?> arrayType = array.getClass().getComponentType();
    int length = Array.getLength(array);
    Object arrayCopy = Array.newInstance(arrayType, length);

    addToReferencesMap(array, arrayCopy);

    for (int i = 0; i < length; i++) {
      Object value = Array.get(array, i);
      Object cloneValue = getClone(value, arrayType);
      Array.set(arrayCopy, i, cloneValue);
    }
    return arrayCopy;
  }

  private static <T> void copyFieldValues(Field[] fields, T fromObj, T toObj)
      throws ReflectiveOperationException {
    Object value;
    Object cloneValue;
    for (Field field : fields) {
     
      field.setAccessible(true);
      Class<?> fieldClazz = field.getType();

      value = field.get(fromObj);
      cloneValue = getClone(value, fieldClazz);
      logger.debug("{}", field);
      field.set(toObj, cloneValue);
    }
  }

  private static Object copyObject(Object obj, Class<?> clazz) throws ReflectiveOperationException {
    Object copy = constractNewObject(clazz);

    addToReferencesMap(obj, copy);

    Field[] fields = getFields(clazz);
    copyFieldValues(fields, obj, copy);
    return copy;
  }

  @SuppressWarnings("unchecked")
  private static <T> T getClone(T original, Class<?> clazz) throws ReflectiveOperationException {
    if (original == null) {
      return null;
    }

    Object cloneValue;
    boolean isPrimitive = clazz.isPrimitive();

    if (!isPrimitive) {
     
      if ((cloneValue = getFromReferencesMap(original)) != null) {
        return (T) cloneValue;
      }
    }

    Class<?> valueType = isPrimitive ? clazz : original.getClass();

    if (valueType.isArray()) {
      cloneValue = copyArray(original);
    } else if (isPrimitive || valueType.isEnum() || isUninstantiable(valueType)) {
      cloneValue = original;
    } else {
      cloneValue = copyObject(original, valueType);
    }
    return (T) cloneValue;
  }

  private static Field[] getFields(Class<?> clazz) {
    List<Field> result = new LinkedList<>();
    while (clazz != null) {
      Field[] fields = clazz.getDeclaredFields();
      for (Field field : fields) {
       
        if (!Modifier.isStatic(field.getModifiers())) {
          result.add(field);
        }
      }
     
      clazz = clazz.getSuperclass();
    }
    return result.toArray(new Field[result.size()]);
  }

  private static Object getFromReferencesMap(Object obj) {
    String key = getUniqueName(obj);
    Object result = references.get().get(key);

    return result;
  }

  private static Object getPrimitiveDefault(Class<?> primitiveClazz) {
    return primitiveWrappersMap.get(primitiveClazz);
  }

  private static String getUniqueName(Object obj) {
    return obj.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(obj));
  }

  private static final class SetMethodAccessible<T> implements PrivilegedExceptionAction<T> {
    private final Method method;
    private final Object args;

    SetMethodAccessible(Method method, Object args) {
      this.method = method;
      this.args = args;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T run() throws ReflectiveOperationException {
      method.setAccessible(true);
      return (T) method.invoke(args);
    }
  }
}
