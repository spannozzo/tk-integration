package org.acme.exceptions;

import javax.validation.ValidationException;

public class MalformedBasicAuthenticationException extends ValidationException {
	static final long serialVersionUID = -4837584769589445073L;

	public MalformedBasicAuthenticationException(String message, Throwable t) {
		super(message,t);
	}

	public MalformedBasicAuthenticationException(String message) {
		super(message);
	}
	
}
