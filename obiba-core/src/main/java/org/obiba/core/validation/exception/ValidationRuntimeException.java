package org.obiba.core.validation.exception;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nullable;

import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;

@SuppressWarnings({ "UnusedDeclaration", "ClassWithTooManyConstructors" })
public class ValidationRuntimeException extends RuntimeException {

  private static final long serialVersionUID = 2605997256235741510L;

  private static final Object NULL_TARGET = new Object();

  private List<Errors> errors;

  public ValidationRuntimeException(List<Errors> errors) {
    this.errors = errors;
  }

  public ValidationRuntimeException() {
    errors = new LinkedList<Errors>();
  }

  public ValidationRuntimeException(Object target, String errorCode, String defaultMessage) {
    errors = new LinkedList<Errors>();
    reject(target, errorCode, defaultMessage);
  }

  public ValidationRuntimeException(Object target, String errorCode, Object[] errorArgs, String defaultMessage) {
    errors = new LinkedList<Errors>();
    reject(target, errorCode, errorArgs, defaultMessage);
  }

  public ValidationRuntimeException(String errorCode, String defaultMessage) {
    errors = new LinkedList<Errors>();
    reject(null, errorCode, defaultMessage);
  }

  public ValidationRuntimeException(String errorCode, Object[] errorArgs, String defaultMessage) {
    errors = new LinkedList<Errors>();
    reject(null, errorCode, errorArgs, defaultMessage);
  }

  /**
   * @return Returns the errors.
   */
  public List<Errors> getErrors() {
    return errors;
  }

  /**
   * @param errors The errors to set.
   */
  public void setErrors(List<Errors> errors) {
    this.errors = errors;
  }

  /**
   * Get the object errors in a flat list.
   *
   * @return
   */
  public List<ObjectError> getAllObjectErrors() {
    List<ObjectError> allErrors = new ArrayList<ObjectError>();
    if(errors != null) {
      for(Errors err : errors) {
        for(ObjectError objectError : err.getAllErrors()) {
          allErrors.add(objectError);
        }
      }
    }
    return allErrors;
  }

  public void reject(Object target, String errorCode) {
    Errors error = getTargetErrors(target);
    error.reject(errorCode);
    errors.add(error);
  }

  public void reject(@Nullable Object target, @Nullable String errorCode, String defaultMessage) {
    Errors error = getTargetErrors(target);
    error.reject(errorCode, defaultMessage);
    errors.add(error);
  }

  public void reject(@Nullable Object target, String errorCode, Object[] errorArgs, String defaultMessage) {
    Errors error = getTargetErrors(target);
    error.reject(errorCode, errorArgs, defaultMessage);
    errors.add(error);
  }

  public void reject(String errorCode) {
    reject(null, errorCode);
  }

  public void reject(@Nullable String errorCode, String defaultMessage) {
    reject(null, errorCode, defaultMessage);
  }

  public void reject(String errorCode, Object[] errorArgs, String defaultMessage) {
    reject(null, errorCode, errorArgs, defaultMessage);
  }

  private Errors getTargetErrors(@Nullable Object target) {
    Object nonNullTarget = target == null ? NULL_TARGET : target;
    for(Errors error : errors) {
      if(error instanceof BindException) {
        if(nonNullTarget.equals(((BindingResult) error).getTarget())) return error;
      }
    }
    return new BindException(nonNullTarget, nonNullTarget.getClass().getName());
  }

  @Override
  public String toString() {
    return errors.toString();
  }

}
