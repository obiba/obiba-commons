/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.core.validation.interceptor;

import java.util.ArrayList;
import java.util.List;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.obiba.core.validation.exception.ValidationRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.Errors;

public class MethodArgValidationInterceptor extends ObjectValidationInspector implements MethodInterceptor {

  private final static Logger log = LoggerFactory.getLogger(MethodArgValidationInterceptor.class);

  /**
   * @see org.aopalliance.intercept.MethodInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
   */
  @Override
  public Object invoke(MethodInvocation methodInvocation) throws Throwable {
    log.info("method={}", methodInvocation.getMethod().getName());
    List<Errors> errors = new ArrayList<Errors>();
    for(int i = 0; i < methodInvocation.getArguments().length; i++) {
      Object arg = methodInvocation.getArguments()[i];
      log.info("method={} object={}", methodInvocation.getMethod().getName(), arg.getClass().getName());
      // Inspect fields... of this arg.
      inspectObject(errors, arg);
    }

    if(errors.size() > 0) {
      log.warn("Validation error(s) found, throwing ValidationException.");
      throw new ValidationRuntimeException(errors);
    }

    return methodInvocation.proceed();
  }

}
