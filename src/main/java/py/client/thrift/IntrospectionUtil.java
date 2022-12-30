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

package py.client.thrift;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IntrospectionUtil {
  public static Method findInheritedMethod(Package pack, Class<?> clazz, String methodName,
      Class<?>[] args,
      boolean strictArgs) throws NoSuchMethodException {
    if (clazz == null) {
      throw new NoSuchMethodException("No class");
    }
    if (methodName == null) {
      throw new NoSuchMethodException("No method name");
    }

    Method method = null;
    Method[] methods = clazz.getDeclaredMethods();
    for (int i = 0; i < methods.length && method == null; i++) {
      if (methods[i].getName().equals(methodName) && isInheritable(pack, methods[i])
          && checkParams(methods[i].getParameterTypes(), args, strictArgs)) {
        method = methods[i];
      }
    }
    if (method != null) {
      return method;
    } else {
      return findInheritedMethod(clazz.getPackage(), clazz.getSuperclass(), methodName, args,
          strictArgs);
    }
  }

  public static Field findInheritedField(Package pack, Class<?> clazz, String fieldName,
      Class<?> fieldType,
      boolean strictType) throws NoSuchFieldException {
    if (clazz == null) {
      throw new NoSuchFieldException("No class");
    }
    if (fieldName == null) {
      throw new NoSuchFieldException("No field name");
    }
    try {
      Field field = clazz.getDeclaredField(fieldName);
      if (isInheritable(pack, field) && isTypeCompatible(fieldType, field.getType(), strictType)) {
        return field;
      } else {
        return findInheritedField(clazz.getPackage(), clazz.getSuperclass(), fieldName, fieldType,
            strictType);
      }
    } catch (NoSuchFieldException e) {
      return findInheritedField(clazz.getPackage(), clazz.getSuperclass(), fieldName, fieldType,
          strictType);
    }
  }

  public static List<Field> getAllFields(Class<?> clazz) {
    List<Field> fields = new ArrayList<Field>();
    fields.addAll(Arrays.asList(clazz.getDeclaredFields()));

    Class<?> superClazz = clazz.getSuperclass();
    if (superClazz != null) {
      fields.addAll(getAllFields(superClazz));
    }

    return fields;
  }

  public static boolean isInheritable(Package pack, Member member) {
    if (pack == null) {
      return false;
    }
    if (member == null) {
      return false;
    }

    int modifiers = member.getModifiers();
    if (Modifier.isPublic(modifiers)) {
      return true;
    }
    if (Modifier.isProtected(modifiers)) {
      return true;
    }
    if (!Modifier.isPrivate(modifiers) && pack.equals(member.getDeclaringClass().getPackage())) {
      return true;
    }

    return false;
  }

  public static boolean checkParams(Class<?>[] formalParams, Class<?>[] actualParams,
      boolean strict) {
    if (formalParams == null && actualParams == null) {
      return true;
    }

    if (formalParams == null && actualParams != null) {
      return false;
    }

    if (formalParams != null && actualParams == null) {
      return false;
    }

    if (formalParams.length != actualParams.length) {
      return false;
    }

    if (formalParams.length == 0) {
      return true;
    }

    int j = 0;
    if (strict) {
      while (j < formalParams.length && formalParams[j].equals(actualParams[j])) {
        j++;
      }
    } else {
      while ((j < formalParams.length) && (formalParams[j].isAssignableFrom(actualParams[j]))) {
        j++;
      }
    }

    if (j != formalParams.length) {
      return false;
    }

    return true;
  }

  public static boolean isSameSignature(Method methodA, Method methodB) {
    if (methodA == null) {
      return false;
    }
    if (methodB == null) {
      return false;
    }

    List<?> parameterTypesA = Arrays.asList(methodA.getParameterTypes());
    List<?> parameterTypesB = Arrays.asList(methodB.getParameterTypes());

    if (methodA.getName().equals(methodB.getName()) && parameterTypesA
        .containsAll(parameterTypesB)) {
      return true;
    }

    return false;
  }

  public static boolean isTypeCompatible(Class<?> formalType, Class<?> actualType, boolean strict) {
    if (formalType == null && actualType != null) {
      return false;
    }
    if (formalType != null && actualType == null) {
      return false;
    }
    if (formalType == null && actualType == null) {
      return true;
    }

    if (strict) {
      return formalType.equals(actualType);
    } else {
      return formalType.isAssignableFrom(actualType);
    }
  }
}