package org.acme.dto;

import java.io.InputStream;

public class MultipartTKRequestDTO {

	InputStream file;

	String account;

	String username;

	String password;

	public InputStream getFile() {
		return file;
	}

	public String getAccount() {
		return account;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	
	public MultipartTKRequestDTO() {
	}
	public MultipartTKRequestDTO(InputStream f, String account, String username, String password) {
		super();
		this.file = f;
		this.account = account;
		this.username = username;
		this.password = password;
	}
	

}