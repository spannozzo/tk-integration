package org.acme.service;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Valid;
import org.acme.dto.BasicAuthDTO;
import org.acme.dto.TokenDTO;
import org.acme.dto.TokenValidationDTO;
import org.acme.exceptions.EncryptionCredentialException;
import org.acme.exceptions.InvalidTokenException;
import org.acme.exceptions.MalformedBasicAuthenticationException;
import org.acme.exceptions.WrongUserOrPasswordException;
import org.acme.util.EncryptPropertyUtil;
import org.acme.util.JWTGeneratorUtil;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jose4j.jwt.consumer.InvalidJwtException;

import io.smallrye.jwt.auth.principal.JWTCallerPrincipal;;

@Singleton
public class AuthService {

	@Inject
	JWTGeneratorUtil jWTGeneratorUtil;

	@Inject
	EncryptPropertyUtil encryptionUtil;

	@ConfigProperty(name = "acme.jwt.encrypted-user")
	String encryptedUser;

	@ConfigProperty(name = "acme.jwt.encrypted-password")
	String encryptedPassword;

	public @Valid TokenDTO getCreateToken(@Valid BasicAuthDTO credentials) {

		Boolean checkUserName = false;
		Boolean checkPassword = false;

		try {
			checkUserName = encryptionUtil.encrypt(credentials.getUserName()).equals(encryptedUser);

			checkPassword = encryptionUtil.encrypt(credentials.getPassword()).equals(encryptedPassword);

		} catch (InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException
				| BadPaddingException | NoSuchAlgorithmException | NoSuchPaddingException e) {

			throw new EncryptionCredentialException(e.getMessage(), e);
		}

		if (!checkUserName || !checkPassword) {
			throw new WrongUserOrPasswordException("Wrong user name or password");
		}

		String token = jWTGeneratorUtil.getToken();

		try {
			return this.getTokenDto(token);
		} catch (IndexOutOfBoundsException | IllegalArgumentException | InvalidJwtException e) {
			throw new InvalidTokenException(e.getMessage(),e);
		}
		
	}

	private TokenDTO getTokenDto(String token) throws IndexOutOfBoundsException, IllegalArgumentException, InvalidJwtException {

		JWTCallerPrincipal principal = jWTGeneratorUtil.getPrincipal(token);

		ZoneId tokenTimeZone = ZoneId.of(principal.getClaim("zoneinfo"));

		LocalDateTime issuedDate = Instant.ofEpochSecond(principal.getIssuedAtTime()).atZone(tokenTimeZone).toInstant()
				.atZone(ZoneId.systemDefault()).toLocalDateTime();
		LocalDateTime expirationDate = Instant.ofEpochSecond(principal.getExpirationTime()).atZone(tokenTimeZone)
				.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

		return new TokenDTO(token,issuedDate,expirationDate);
	}

	public @Valid TokenValidationDTO validateToken(String token)
			throws InvalidJwtException, IndexOutOfBoundsException, IllegalArgumentException {
		JWTCallerPrincipal principal = jWTGeneratorUtil.getPrincipal(token);

		if (principal == null) {
			return new TokenValidationDTO(false, token, "0").setReason("Malformed Token");
		}
		if (jWTGeneratorUtil.isTokenExpired(token)) {
			return new TokenValidationDTO(false, token, "0").setReason("Expired Token");
		}

		ZoneId tokenTimeZone = ZoneId.of(principal.getClaim("zoneinfo"));
		LocalDateTime expirationDate = Instant.ofEpochSecond(principal.getExpirationTime()).atZone(tokenTimeZone)
				.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

		LocalDateTime localNow = Instant.now().atZone(ZoneId.systemDefault()).toLocalDateTime();

		long minutes = localNow.until(expirationDate, ChronoUnit.MINUTES);

		return new TokenValidationDTO(true, token,
				new StringBuilder().append("Around ").append(minutes).append(" minutes").toString())
						.setReason("Not yet expired");

	}

	public BasicAuthDTO getCredentials(String authorizationValue) throws IndexOutOfBoundsException,MalformedBasicAuthenticationException {
		
		if (!authorizationValue.contains("Basic ")) {
			throw new MalformedBasicAuthenticationException("Malformed Basic Authentication");
		}
		
		String encodedCredentials = authorizationValue.replace("Basic ", "");

		byte[] decodedBytes = Base64.getDecoder().decode(encodedCredentials);
		String decodedString = new String(decodedBytes);

		String[] credentialValues = decodedString.split(":");

		return new BasicAuthDTO(credentialValues[0], credentialValues[1]);

	}

}
