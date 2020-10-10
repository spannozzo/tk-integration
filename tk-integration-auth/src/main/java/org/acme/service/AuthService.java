package org.acme.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Valid;
import org.acme.dto.BasicAuthDTO;
import org.acme.dto.TokenDTO;
import org.acme.dto.TokenValidationDTO;
import org.acme.exceptions.InvalidTokenException;
import org.acme.exceptions.MalformedBasicAuthenticationException;
import org.acme.exceptions.WrongUserOrPasswordException;
import org.acme.util.AESUtil;
import org.acme.util.JWTGeneratorUtil;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import io.smallrye.jwt.auth.principal.JWTCallerPrincipal;

@Singleton
public class AuthService {

	@Inject
	JWTGeneratorUtil jWTGeneratorUtil;

	private static final String CLAIM_PROPERTY="zoneinfo";
	
	public static String getClaimproperty() {
		return CLAIM_PROPERTY;
	}

	@ConfigProperty(name = "acme.jwt.encrypted-user")
	String encryptedUser;

	@ConfigProperty(name = "acme.jwt.encrypted-password")
	String encryptedPassword;

	@ConfigProperty(name = "acme.aes.password")
	String aesPassword;
		
	public @Valid TokenDTO getCreateToken(@Valid BasicAuthDTO credentials) {

		Boolean checkUserName = false;
		Boolean checkPassword = false;

		checkUserName= AESUtil.getInstance().decrypt(encryptedUser, aesPassword).equals(credentials.getUserName());	
		checkPassword = AESUtil.getInstance().decrypt(encryptedPassword,aesPassword).equals(credentials.getPassword());
		
		if (Boolean.FALSE.equals(checkUserName) || Boolean.FALSE.equals(checkPassword)) {
			throw new WrongUserOrPasswordException("Wrong user name or password");
		}

		String token = jWTGeneratorUtil.getToken();

		return this.getTokenDto(token);
		 
		
	}

	private TokenDTO getTokenDto(String token) {

		JWTCallerPrincipal principal = jWTGeneratorUtil.getPrincipal(token);

		ZoneId tokenTimeZone = ZoneId.of(principal.getClaim(AuthService.CLAIM_PROPERTY));

		LocalDateTime issuedDate = Instant.ofEpochSecond(principal.getIssuedAtTime()).atZone(tokenTimeZone).toInstant()
				.atZone(ZoneId.systemDefault()).toLocalDateTime();
		LocalDateTime expirationDate = Instant.ofEpochSecond(principal.getExpirationTime()).atZone(tokenTimeZone)
				.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

		return new TokenDTO(token,issuedDate,expirationDate);
	}

	
	
	public @Valid TokenValidationDTO validateToken(String token){
		JWTCallerPrincipal principal=null;
		ZoneId tokenTimeZone = null;
		try {
			principal = jWTGeneratorUtil.getPrincipal(token);
			if (principal.getClaim(AuthService.CLAIM_PROPERTY)==null) {
				throw new InvalidTokenException("Malformed Token");
			}
			tokenTimeZone=ZoneId.of(principal.getClaim(AuthService.CLAIM_PROPERTY));
		} 
		catch (InvalidTokenException e) {
			return new TokenValidationDTO(false, token, "0").setReason("Malformed Token");
		}

		if (Boolean.TRUE.equals(jWTGeneratorUtil.isTokenExpired(principal))) {
			return new TokenValidationDTO(false, token, "0").setReason("Expired Token");
		}

		LocalDateTime expirationDate = Instant.ofEpochSecond(principal.getExpirationTime()).atZone(tokenTimeZone)
				.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

		LocalDateTime localNow = Instant.now().atZone(ZoneId.systemDefault()).toLocalDateTime();

		long minutes = localNow.until(expirationDate, ChronoUnit.MINUTES);

		return new TokenValidationDTO(true, token,
				new StringBuilder().append("Around ").append(minutes).append(" minutes").toString())
						.setReason("Not yet expired");

	}

	public BasicAuthDTO getCredentials(String authorizationValue){
		
		if (!authorizationValue.contains("Basic ")) {
			throw new MalformedBasicAuthenticationException("Malformed Basic Authentication");
		}
		
		String encodedCredentials = authorizationValue.replace("Basic ", "");

		byte[] decodedBytes = Base64.getDecoder().decode(encodedCredentials);
		String decodedString = new String(decodedBytes);

		try {
			String[] credentialValues = decodedString.split(":");
			return new BasicAuthDTO(credentialValues[0], credentialValues[1]);
		} catch (IndexOutOfBoundsException e) {
			throw new MalformedBasicAuthenticationException("Malformed Basic Authentication");
		}
		

		

	}

	

}
