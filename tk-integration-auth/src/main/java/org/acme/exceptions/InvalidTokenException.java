package org.acme.exceptions;

import javax.validation.ValidationException;

public class InvalidTokenException extends ValidationException {

	static final long serialVersionUID = -3592419341009270990L;

	public InvalidTokenException(String message,Throwable t) {
		super(message,t);
	}

	public InvalidTokenException(String message) {
		super(message);
	}
}
