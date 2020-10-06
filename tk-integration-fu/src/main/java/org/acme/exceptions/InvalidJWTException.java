package org.acme.exceptions;

import javax.validation.ValidationException;

public class InvalidJWTException extends ValidationException {

	public InvalidJWTException(String message) {
		super(message);
	}

	static final long serialVersionUID = -6995256875045617942L;

	
}
