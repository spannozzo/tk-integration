package org.acme.exceptions;

import javax.validation.ValidationException;

public class InvalidFileStoringException extends ValidationException {

	public InvalidFileStoringException(String message, Throwable t) {
		super(message, t);
	}

	static final long serialVersionUID = -2582699779657140919L;

}
