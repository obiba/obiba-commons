package org.obiba.core.validation.validator;

import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Validator class to extend for performing object validation on a specific class.
 *
 * @author ymarcon
 */
public abstract class AbstractClassValidator implements Validator {

  /**
   * @see org.springframework.validation.Validator#supports(java.lang.Class)
   */
  @SuppressWarnings("unchecked")
  public boolean supports(Class clazz) {
    return clazz.isAssignableFrom(getValidatorSupportClass());
  }

  /**
   * @see org.springframework.validation.Validator#validate(java.lang.Object,
   *      org.springframework.validation.Errors)
   */
  public abstract void validate(Object obj, Errors errors);

  /**
   * @return
   */
  protected abstract Class getValidatorSupportClass();

  /**
   * Validate given object.
   *
   * @param obj the object to validate.
   * @return null if object is null or object class is not supported by this validator.
   */
  public Errors validate(Object obj) {
    Errors errors = null;

    if(obj != null && supports(obj.getClass())) {
      errors = new BindException(obj, obj.getClass().getName());

      validate(obj, errors);
    }

    return errors;
  }
}
