package org.obiba.core.validation.exception;

import java.util.LinkedList;
import java.util.List;

import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

public class ValidationException extends RuntimeException {

	/**
	 * Generated serialVersion
	 */
	private static final long serialVersionUID = 2605997256235741510L;
	private List<Errors> errors;

	public ValidationException(final List<Errors> errors) {
		this.errors = errors;
	}
  
  public ValidationException() {
    this.errors = new LinkedList<Errors>();
  }
  
  public ValidationException(Object target, String errorCode, String defaultMessage) {
    this.errors = new LinkedList<Errors>();
    reject(target, errorCode, defaultMessage);
  }

  public ValidationException(Object target, String errorCode, Object[] errorArgs, String defaultMessage) {
    this.errors = new LinkedList<Errors>();
    reject(target, errorCode, errorArgs, defaultMessage);
  }
  
  public ValidationException(String errorCode, String defaultMessage) {
    this.errors = new LinkedList<Errors>();
    reject(null, errorCode, defaultMessage);
  }
  
  public ValidationException(String errorCode, Object[] errorArgs, String defaultMessage) {
    this.errors = new LinkedList<Errors>();
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
	public void setErrors(final List<Errors> errors) {
		this.errors = errors;
	}
  
  public void reject(Object target, String errorCode) {
    Errors error = getTargetErrors(target);
    error.reject(errorCode);
    errors.add(error);
  }

  public void reject(Object target, String errorCode, String defaultMessage) {
    Errors error = getTargetErrors(target);
    error.reject(errorCode, defaultMessage);
    errors.add(error);
  }

  public void reject(Object target, String errorCode, Object[] errorArgs, String defaultMessage) {
    Errors error = getTargetErrors(target);
    error.reject(errorCode, errorArgs, defaultMessage);
    errors.add(error);
  }
  
  public void reject(String errorCode) {
    reject(null, errorCode);
  }

  public void reject(String errorCode, String defaultMessage) {
    reject(null, errorCode, defaultMessage);
  }

  public void reject(String errorCode, Object[] errorArgs, String defaultMessage) {
    reject(null, errorCode, errorArgs, defaultMessage);
  }
  
  private Errors getTargetErrors(Object target) {
    for (Errors error : errors) {
      if (error instanceof BindException) {
        if (target != null && target.equals(((BindException)error).getTarget()))
          return error;
        else if (target == null && ((BindException)error).getTarget() == null)
          return error;
      }
    }
    
    return new BindException(target, target != null ? target.getClass().getName() : "null");
  }

}
