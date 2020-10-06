package org.acme.exceptions;

import javax.validation.ValidationException;

public class ProcessIdNotFoundException extends ValidationException {

	static final long serialVersionUID = -1249817192558873358L;

	public ProcessIdNotFoundException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	

}
