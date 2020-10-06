package org.acme.exceptions;

import javax.validation.ValidationException;

public class WrongUserOrPasswordException extends ValidationException {

	static final long serialVersionUID = -223974615852913689L;

	public WrongUserOrPasswordException(String message){
		super(message);
	}
}
