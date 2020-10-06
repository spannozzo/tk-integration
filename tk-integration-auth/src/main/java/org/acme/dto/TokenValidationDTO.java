package org.acme.dto;

import javax.validation.constraints.NotBlank;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(name="Token Validation DTO", description="Return information about validation of a token")
public class TokenValidationDTO {
	
	
	@Schema(title = "Valid Token?", required = true, example = "true")
	Boolean valid;
	
	@NotBlank
	@Schema(title = "Checked Token", required = true, example = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJpc3MiOiJodHRwczovL3F1YXJrdXMuaW8vdXNpbmctand0LXJiYWMiLCJ1cG4iOiJzcGFubm96em9AYWNtZS5qd3QiLCJncm91cHMiOlsiQURNSU4iLCJVU0VSIl0sImlhdCI6MTYwMTU3MjcyNywiZXhwIjoxNjAxNTczMDI3LCJ6b25laW5mbyI6IkV1cm9wZS9CZXJsaW4iLCJqdGkiOiIzM0NYVDB0NS1wQjdrLVAtdEZtenB3In0.EPP4g5P78gf7YvokD_LQkZFjbDp4Icj2NT5zPcoFMAPTdFaXD3t_A5uApFx2stwb-yy143Qh3FUHp57Cawu5oQL31Q4OF1P6NZq_XmSvNhSMWn0qCZcLXIXWpID3ScPoPrH5i8-TfGLKdGguVfX9JDhADJ1unkr0LBC7FOyC0AH3r8QfYJ2DKOdgGpWCCrv7NqfGyzzbCqU2COAj4rMz7bAJmip3aw0GC7otO2uzeL7oCj4K8bF_I5i9Upo7nhC4vu9ekDtSUkzRUX7h49AuK6EUe_ZL1ECdduZPmx6UsVisH0FdgQC-gaux8X4sdmnj_48ElOn0xGT6CY4bTLzlqA")
	String token;
	
	@NotBlank
	@Schema(title = "Information about the duration of the token", required = true, defaultValue = "0" ,example = "Around 4 minutes")
	String duration;
	
	
	@Schema(title = "Reason",example = "Not yet expired")
	String reason;
	
	public Boolean getValid() {
		return valid;
	}
	public void setValid(Boolean valid) {
		this.valid = valid;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public String getDuration() {
		return duration;
	}
	public void setDuration(String duration) {
		this.duration = duration;
	}
	
	public TokenValidationDTO() {
	}
	
	public TokenValidationDTO(@NotBlank Boolean valid, @NotBlank String token, String duration) {
		super();
		this.valid = valid;
		this.token = token;
		this.duration = duration;
	}
	public String getReason() {
		return reason;
	}
	public TokenValidationDTO setReason(String reason) {
		this.reason = reason;
		
		return this;
	}

	
}
