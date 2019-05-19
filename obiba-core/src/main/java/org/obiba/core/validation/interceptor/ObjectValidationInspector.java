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

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
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

import javax.annotation.Nullable;

import org.apache.commons.beanutils.PropertyUtils;
import org.obiba.core.service.EntityQueryService;
import org.obiba.core.validation.validator.AbstractPersistenceAwareClassValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@SuppressWarnings("UnusedDeclaration")
public class ObjectValidationInspector {

  private final static Logger log = LoggerFactory.getLogger(ObjectValidationInspector.class);

  private List<Validator> validators = new ArrayList<Validator>();

  protected EntityQueryService entityQueryService;

  /**
   * @return Returns the validators.
   */
  public List<Validator> getValidators() {
    return validators;
  }

  /**
   * @param validators The validators to set.
   */
  public void setValidators(List<Validator> validators) {
    this.validators = validators;
  }

  public void setEntityQueryService(EntityQueryService entityQueryService) {
    this.entityQueryService = entityQueryService;
  }

  public EntityQueryService getEntityQueryService() {
    return entityQueryService;
  }

  private void inspectObjectProperties(Object arg, List<Errors> errors) {
    // Inspect supported properties.
    PropertyDescriptor[] propertyDescriptors = PropertyUtils.getPropertyDescriptors(arg.getClass());
    for(PropertyDescriptor propertyDescriptor : propertyDescriptors) {
      try {
        inspectPropertyValue(errors, propertyDescriptor.getReadMethod().invoke(arg));
      } catch(IllegalArgumentException e) {
        log.error("IllegalArgumentException", e);
      } catch(IllegalAccessException e) {
        log.error("IllegalAccessException", e);
      } catch(InvocationTargetException e) {
        log.error("InvocationTargetException", e);
      }
    }
  }

  private void inspectPropertyValue(List<Errors> errors, Object propertyValue) {
    if(propertyValue != null && isArrayOrCollection(propertyValue.getClass())) {
      if(propertyValue.getClass().isArray()) {
        for(Object propertyElementValue : (Object[]) propertyValue) {
          validatePropertyValue(errors, propertyElementValue);
        }
      } else {
        for(Object propertyElementValue : (Collection<?>) propertyValue) {
          validatePropertyValue(errors, propertyElementValue);
        }
      }
    } else {
      // Non-Scalar property
      validatePropertyValue(errors, propertyValue);
    }
  }

  private void validatePropertyValue(List<Errors> errors, @Nullable Object propertyValue) {
    if(propertyValue == null) return;
    for(Validator validator : getValidators()) {
      if(validator.supports(propertyValue.getClass())) {
        log.debug("Validator supported: {}", propertyValue.getClass());
        validateAndAddErrors(propertyValue, validator, errors);
        inspectObjectProperties(propertyValue, errors);
      }
    }
  }

  private Errors validateAndAddErrors(Object arg, Validator validator, Collection<Errors> errors) {
    Errors objErrors = new BindException(arg, arg.getClass().getName());
    validator.validate(arg, objErrors);
    if(objErrors.hasErrors()) {
      errors.add(objErrors);
    }
    return objErrors;
  }

  public void inspectObject(List<Errors> errors, Object arg) {
    for(Validator validator : getValidators()) {
      if(validator.supports(arg.getClass())) {
        if(entityQueryService != null && validator instanceof AbstractPersistenceAwareClassValidator) {
          ((AbstractPersistenceAwareClassValidator) validator).setEntityQueryService(entityQueryService);
        }
        log.debug("Validator supported: {}", arg.getClass());
        validateAndAddErrors(arg, validator, errors);
        inspectObjectProperties(arg, errors);
      }
    }
  }

  private boolean isArrayOrCollection(Class<?> clazz) {
    return clazz.isArray() || clazz.isAssignableFrom(List.class) || clazz.isAssignableFrom(ArrayList.class) ||
        clazz.isAssignableFrom(Set.class) || clazz.isAssignableFrom(SortedSet.class) ||
        clazz.isAssignableFrom(AbstractCollection.class) || clazz.isAssignableFrom(AbstractList.class) ||
        clazz.isAssignableFrom(AbstractSet.class) || clazz.isAssignableFrom(HashSet.class) ||
        clazz.isAssignableFrom(LinkedHashSet.class) || clazz.isAssignableFrom(LinkedList.class) ||
        clazz.isAssignableFrom(TreeSet.class) || clazz.isAssignableFrom(Vector.class);
  }
}
