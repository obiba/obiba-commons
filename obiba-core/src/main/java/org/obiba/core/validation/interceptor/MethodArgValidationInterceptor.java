package org.obiba.core.validation.interceptor;

import java.beans.PropertyDescriptor;
import java.util.AbstractCollection;
import java.util.AbstractList;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;


import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obiba.core.validation.exception.ValidationException;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class MethodArgValidationInterceptor implements MethodInterceptor {

	private List<Validator> validators = new ArrayList<Validator>();
	private Log log = LogFactory.getLog(getClass());

	/**
	 * @see org.aopalliance.intercept.MethodInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
	 */
	public Object invoke(final MethodInvocation methodInvocation)
			throws Throwable {
		final List<Errors> errors = new ArrayList<Errors>();
		for (int i = 0; i < methodInvocation.getArguments().length; i++) {
			final Object arg = methodInvocation.getArguments()[i];
			// Inspect fields... of this arg.
			inspectObject(errors, arg);
		}

		if (errors.size() > 0) {
			log.warn("Validation error(s) found, throwing ValidationException.");
			throw new ValidationException(errors);
		}

		return methodInvocation.proceed();
	}

	/**
	 * @return Returns the validators.
	 */
	public List<Validator> getValidators() {
		return validators;
	}

	/**
	 * @param validators
	 *          The validators to set.
	 */
	public void setValidators(List<Validator> validators) {
		this.validators = validators;
	}

	private void inspectObjectProperties(final Object arg,
			final List<Errors> errors) throws Exception {
		// Inspect supported properties.
		final PropertyDescriptor[] propertyDescriptors = PropertyUtils
				.getPropertyDescriptors(arg.getClass());
		for (final PropertyDescriptor propertyDescriptor : propertyDescriptors) {
			final Object propertyValue = propertyDescriptor.getReadMethod().invoke(
					arg);
			if (propertyValue != null
					&& isArrayOrCollection(propertyValue.getClass())) {
				if (propertyValue.getClass().isArray()) {
					for (final Object propertyElementValue : ((Object[]) propertyValue)) {
						for (final Validator validator : getValidators()) {
							if (validator.supports(propertyElementValue.getClass())) {
								log.debug("Validator supported: "
										+ propertyElementValue.getClass());
								validateAndAddErrors(propertyElementValue, validator, errors);
								inspectObjectProperties(propertyElementValue, errors);
							}
						}
					}
				} else {
					for (final Object propertyElementValue : ((Collection) propertyValue)) {
						for (final Validator validator : getValidators()) {
							if (validator.supports(propertyElementValue.getClass())) {
								log.debug("Validator supported: "
										+ propertyElementValue.getClass());
								validateAndAddErrors(propertyElementValue, validator, errors);
								inspectObjectProperties(propertyElementValue, errors);
							}
						}
					}
				}
			} else if (propertyValue != null) {
				// Non-Scalar property
				for (final Validator validator : getValidators()) {
					if (validator.supports(propertyValue.getClass())) {
						log.debug("Validator supported: " + propertyValue.getClass());
						validateAndAddErrors(propertyValue, validator, errors);
						inspectObjectProperties(propertyValue, errors);
					}
				}
			}
		}
	}

	private Errors validateAndAddErrors(final Object arg,
			final Validator validator, final List<Errors> errors) {
		final BindException objErrors = new BindException(arg, arg.getClass()
				.getName());
		validator.validate(arg, objErrors);
		if (objErrors.hasErrors()) {
			errors.add(objErrors);
		}
		return objErrors;
	}

	private void inspectObject(final List<Errors> errors, final Object arg)
			throws Exception {
		for (final Validator validator : getValidators()) {
			if (validator.supports(arg.getClass())) {
				log.debug("Validator supported: " + arg.getClass());
				validateAndAddErrors(arg, validator, errors);
				inspectObjectProperties(arg, errors);
			}
		}
	}

	private boolean isArrayOrCollection(final Class clazz) {
		return (clazz.isArray() || clazz.isAssignableFrom(List.class)
				|| clazz.isAssignableFrom(ArrayList.class)
				|| clazz.isAssignableFrom(Set.class)
				|| clazz.isAssignableFrom(SortedSet.class)
				|| clazz.isAssignableFrom(AbstractCollection.class)
				|| clazz.isAssignableFrom(AbstractList.class)
				|| clazz.isAssignableFrom(AbstractSet.class)
				|| clazz.isAssignableFrom(HashSet.class)
				|| clazz.isAssignableFrom(LinkedHashSet.class)
				|| clazz.isAssignableFrom(LinkedList.class)
				|| clazz.isAssignableFrom(TreeSet.class) || clazz
				.isAssignableFrom(Vector.class));
	}

}
