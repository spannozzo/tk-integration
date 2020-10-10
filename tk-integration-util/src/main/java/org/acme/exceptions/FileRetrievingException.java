package org.acme.exceptions;

import javax.validation.ValidationException;

public class FileRetrievingException extends ValidationException {

	static final long serialVersionUID = 4948696727169923250L;
	
	public FileRetrievingException(String message, Throwable cause) {
		super(message, cause);
		
	}

}
