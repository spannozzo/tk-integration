package org.acme.exceptions;

import javax.validation.ValidationException;

public class TKResponseException extends ValidationException {
	
	public TKResponseException(String message, Throwable t) {
		super(message, t);
	}

	public TKResponseException(String message) {
		super(message);
	}

	private static final long serialVersionUID = 2906607155687430568L;
	
}
