package org.acme.exceptions;

import javax.validation.ValidationException;

public class RequestWhileBusyException extends ValidationException {

	private static final long serialVersionUID = 1L;

	public RequestWhileBusyException(String message, Throwable cause) {
		super(message, cause);
		
	}

	public RequestWhileBusyException(String message) {
		super(message);
	}

}
