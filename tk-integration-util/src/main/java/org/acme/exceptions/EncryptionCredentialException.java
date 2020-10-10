package org.acme.exceptions;

import javax.validation.ValidationException;

public class EncryptionCredentialException extends ValidationException {

	static final long serialVersionUID = 7725461212291421920L;

	public EncryptionCredentialException(String message,Throwable t) {
		super(message,t);
	}
	
	public EncryptionCredentialException(String message) {
		super(message);
	}
}
