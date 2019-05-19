/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
  @Override
  @SuppressWarnings("unchecked")
  public boolean supports(Class clazz) {
    return clazz.isAssignableFrom(getValidatorSupportClass());
  }

  /**
   * @see org.springframework.validation.Validator#validate(java.lang.Object,
   *      org.springframework.validation.Errors)
   */
  @Override
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
