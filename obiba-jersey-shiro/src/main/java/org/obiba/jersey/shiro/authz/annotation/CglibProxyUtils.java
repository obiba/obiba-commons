package org.obiba.jersey.shiro.authz.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

class CglibProxyUtils {

  private CglibProxyUtils() {}

  static boolean isSuperMethodAnnotated(Class<?> superClass, Method method,
      Class<? extends Annotation> annotationClass) {
    try {
      return superClass.getMethod(method.getName(), method.getParameterTypes()).isAnnotationPresent(annotationClass);
    } catch(NoSuchMethodException ignored) {
      return false;
    }
  }

  static <T extends Annotation> T getSuperMethodAnnotation(Class<?> superClass, Method method,
      Class<T> annotationClass) {
    try {
      return superClass.getMethod(method.getName(), method.getParameterTypes()).getAnnotation(annotationClass);
    } catch(NoSuchMethodException ignored) {
      return null;
    }
  }

}
