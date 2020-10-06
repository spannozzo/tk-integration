package org.acme.exceptions;

import javax.validation.ValidationException;

public class TKUnparsableResponse extends ValidationException {

	private static final long serialVersionUID = -7135827143640061021L;

	public TKUnparsableResponse(String message, Throwable cause) {
		super(message, cause);
	}

}
