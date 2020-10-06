package org.acme.service;

import javax.validation.ValidationException;

public class EncryptionCredentialException extends ValidationException {

	private static final long serialVersionUID = -8625755907485796383L;

	public EncryptionCredentialException(String message, Throwable cause) {
		super(message, cause);
		
	}

}
