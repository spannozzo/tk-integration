package org.acme.util;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyOrNullString;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;



@QuarkusTest
public class EncryptPropertyUtilTest {

	@Inject
	EncryptPropertyUtil encryptionUtil;

	static String encrypted;
	
	@ConfigProperty(name = "acme.jwt.password",defaultValue = "1234")
	String testPassword;

	
	@Test
	public void should_encrypt_a_password() throws InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException {
		encrypted=encryptionUtil.encrypt(testPassword);
		
		assertThat(encrypted, is(not(emptyOrNullString())));
		assertThat(Base64.getDecoder().decode(encrypted), is(not(testPassword)));
	}
	@Test
	public void should_decrypt_into_original_password_value() throws InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException {
		String decrypted=encryptionUtil.decrypt(encrypted);
		
		assertThat(decrypted, is(notNullValue()));
		assertThat(decrypted, is(testPassword));
	}

}