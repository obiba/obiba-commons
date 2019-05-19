/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
