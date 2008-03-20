package org.obiba.core.validation.interceptor;

import java.util.ArrayList;
import java.util.List;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obiba.core.validation.exception.ValidationRuntimeException;
import org.springframework.validation.Errors;

public class MethodArgValidationInterceptor extends ObjectValidationInspector implements MethodInterceptor {
	
	private Log log = LogFactory.getLog(getClass());
  
	/**
	 * @see org.aopalliance.intercept.MethodInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
	 */
	public Object invoke(final MethodInvocation methodInvocation)
			throws Throwable {
    log.info("method=" + methodInvocation.getMethod().getName());
		final List<Errors> errors = new ArrayList<Errors>();
		for (int i = 0; i < methodInvocation.getArguments().length; i++) {
			final Object arg = methodInvocation.getArguments()[i];
      log.info("method=" + methodInvocation.getMethod().getName() + " object=" + arg.getClass().getName());
			// Inspect fields... of this arg.
			inspectObject(errors, arg);
		}

		if (errors.size() > 0) {
			log.warn("Validation error(s) found, throwing ValidationException.");
			throw new ValidationRuntimeException(errors);
		}

		return methodInvocation.proceed();
	}

}
