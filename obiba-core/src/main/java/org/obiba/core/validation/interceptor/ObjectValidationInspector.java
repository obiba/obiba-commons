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

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obiba.core.service.EntityQueryService;
import org.obiba.core.validation.validator.AbstractPersistenceAwareClassValidator;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class ObjectValidationInspector {
  
  private List<Validator> validators = new ArrayList<Validator>();

  protected EntityQueryService entityQueryService;
  
  private Log log = LogFactory.getLog(getClass());
  
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
  
  public void setEntityQueryService(EntityQueryService entityQueryService) {
    this.entityQueryService = entityQueryService;
  }
  
  public EntityQueryService getEntityQueryService() {
    return entityQueryService;
  }

  private void inspectObjectProperties(final Object arg,
      final List<Errors> errors) {
    // Inspect supported properties.
    final PropertyDescriptor[] propertyDescriptors = PropertyUtils
        .getPropertyDescriptors(arg.getClass());
    for (final PropertyDescriptor propertyDescriptor : propertyDescriptors) {
      Object propertyValue;
      try {
        propertyValue = propertyDescriptor.getReadMethod().invoke(
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
      } catch (IllegalArgumentException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (IllegalAccessException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (InvocationTargetException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
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

  public void inspectObject(final List<Errors> errors, final Object arg) {
    for (final Validator validator : getValidators()) {
      if (validator.supports(arg.getClass())) {
        if (entityQueryService != null && validator instanceof AbstractPersistenceAwareClassValidator) {
          ((AbstractPersistenceAwareClassValidator)validator).setEntityQueryService(entityQueryService);
        }
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
