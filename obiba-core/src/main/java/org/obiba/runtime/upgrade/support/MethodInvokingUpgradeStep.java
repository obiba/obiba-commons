/***********************************************************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 **********************************************************************************************************************/
package org.obiba.runtime.upgrade.support;

import java.lang.reflect.Method;

import org.obiba.runtime.Version;
import org.obiba.runtime.upgrade.AbstractUpgradeStep;

public class MethodInvokingUpgradeStep extends AbstractUpgradeStep {

  private Object methodOwner;

  private String methodName;

  @Override
  public void execute(Version currentVersion) {
    Class<?> clazz = methodOwner.getClass();

    try {
      Method[] methods = clazz.getMethods();
      for(Method method : methods) {
        if(method.getName().equals(methodName)) {
          method.invoke(methodOwner);
          break;
        }
      }
    } catch(Exception ex) {
      throw new RuntimeException(
          "Could not invoke method '" + methodName + "' of object of type " + clazz.getSimpleName(), ex);
    }
  }

  public void setMethodOwner(Object methodOwner) {
    this.methodOwner = methodOwner;
  }

  public void setMethodName(String methodName) {
    this.methodName = methodName;
  }
}
