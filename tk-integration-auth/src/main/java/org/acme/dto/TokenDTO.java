package org.acme.dto;

import java.time.LocalDateTime;
import javax.validation.constraints.Future;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(name="Token DTO", description="JWT token to be passed for using application APIs")
public class TokenDTO {
	
	@NotBlank
	@Schema(title = "Generated Token", required = true,example = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJpc3MiOiJodHRwczovL3F1YXJrdXMuaW8vdXNpbmctand0LXJiYWMiLCJ1cG4iOiJzcGFubm96em9AYWNtZS5qd3QiLCJncm91cHMiOlsiQURNSU4iLCJVU0VSIl0sImlhdCI6MTYwMTU3MjcyNywiZXhwIjoxNjAxNTczMDI3LCJ6b25laW5mbyI6IkV1cm9wZS9CZXJsaW4iLCJqdGkiOiIzM0NYVDB0NS1wQjdrLVAtdEZtenB3In0.EPP4g5P78gf7YvokD_LQkZFjbDp4Icj2NT5zPcoFMAPTdFaXD3t_A5uApFx2stwb-yy143Qh3FUHp57Cawu5oQL31Q4OF1P6NZq_XmSvNhSMWn0qCZcLXIXWpID3ScPoPrH5i8-TfGLKdGguVfX9JDhADJ1unkr0LBC7FOyC0AH3r8QfYJ2DKOdgGpWCCrv7NqfGyzzbCqU2COAj4rMz7bAJmip3aw0GC7otO2uzeL7oCj4K8bF_I5i9Upo7nhC4vu9ekDtSUkzRUX7h49AuK6EUe_ZL1ECdduZPmx6UsVisH0FdgQC-gaux8X4sdmnj_48ElOn0xGT6CY4bTLzlqA")
	String token;
	
	@NotNull
	@PastOrPresent
	@Schema(title = "Issued Date", required = true,example = "2020-10-01T19:23:47")
	LocalDateTime createdAt;
	
	@NotNull
	@Future
	@Schema(title = "Expiration Date", required = true,example = "2020-10-01T19:28:47")
	LocalDateTime expireAt;
	
	public TokenDTO(@NotBlank String token, @NotNull @PastOrPresent LocalDateTime createdAt,
			@NotNull @Future LocalDateTime expireAt) {
		super();
		this.token = token;
		this.createdAt = createdAt;
		this.expireAt = expireAt;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public LocalDateTime getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
	public LocalDateTime getExpireAt() {
		return expireAt;
	}
	public void setExpireAt(LocalDateTime expireAt) {
		this.expireAt = expireAt;
	}
}
