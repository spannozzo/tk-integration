package org.acme.dto;

import javax.json.bind.annotation.JsonbTransient;
import javax.validation.constraints.NotBlank;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

//@Schema(name="Basic Auth Request DTO", description="credentials json to send for basic authentication",example = "user|xxx|password",type = SchemaType.STRING )

@Schema(name="Basic Auth Request DTO", description="credentials json to send for basic authentication")

public class BasicAuthDTO {
	
	@NotBlank
	@Schema(title = "User Name", required = true)
	String userName;
	
	@NotBlank
	@JsonbTransient
	@Schema(title = "Password", required = true)
	String password;

	
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public BasicAuthDTO(@NotBlank String userName, @NotBlank String password) {
		super();
		this.userName = userName;
		this.password = password;
	}

	public BasicAuthDTO() {
		
	}

	
	
}
