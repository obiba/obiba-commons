package org.obiba.core.validation.exception;

import java.util.List;

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

}
