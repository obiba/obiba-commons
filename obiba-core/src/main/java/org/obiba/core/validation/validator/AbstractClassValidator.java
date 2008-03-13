package org.obiba.core.validation.validator;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Validator class to extend for performing object validation on a specific class.
 * 
 * @author ymarcon
 *
 */
public abstract class AbstractClassValidator implements Validator {

	/**
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
  public boolean supports(final Class clazz) {
		return clazz.isAssignableFrom(getValidatorSupportClass());
	}

	/**
	 * @see org.springframework.validation.Validator#validate(java.lang.Object,
	 *      org.springframework.validation.Errors)
	 */
	public abstract void validate(final Object obj, final Errors errors);

	/**
	 * @return
	 */
	protected abstract Class getValidatorSupportClass();

}
