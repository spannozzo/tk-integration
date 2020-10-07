package org.acme.util;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyOrNullString;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import io.quarkus.test.junit.QuarkusTest;



@QuarkusTest
@TestMethodOrder(OrderAnnotation.class)
class EncryptPropertyUtilTest {

	@Inject
	EncryptPropertyUtil encryptionUtil;

	static String encrypted;
	
	@ConfigProperty(name = "acme.jwt.password",defaultValue = "1234")
	String testPassword;

	@ConfigProperty(name = "acme.jwt.password.keylength",defaultValue = "128")
	Integer keylength;

	private static String decrypted;
	
	@Test
	@Order(1)
	void should_encrypt_a_password() throws InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, NoSuchProviderException {
		
		
		encrypted=encryptionUtil.encrypt(testPassword);
		
		assertThat(encrypted, is(not(emptyOrNullString())));
		assertThat(Base64.getDecoder().decode(encrypted), is(not(testPassword)));
	}
	
	@Test
	@Order(2)
	void should_decrypt_into_original_password_value() throws InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, NoSuchProviderException {
		decrypted=encryptionUtil.decrypt(encrypted);
		
		assertThat(decrypted, is(notNullValue()));
		assertThat(decrypted, is(testPassword));
	}
	
	@Test
	@Order(3)
	void check_encryptions_from_new_key() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, InvalidAlgorithmParameterException, ShortBufferException, IllegalBlockSizeException, BadPaddingException {
		String key =encryptionUtil.getSecretKey();
		
		String encrypted=encryptionUtil.encrypt(testPassword);
		
		assertThat(encrypted, is(not(emptyOrNullString())));
		assertThat(Base64.getDecoder().decode(encrypted), is(not(testPassword)));
		
		String decrypted=encryptionUtil.decrypt(encrypted);
		
		assertThat(decrypted, is(notNullValue()));
		assertThat(decrypted, is(testPassword));
		assertThat(decrypted, is(EncryptPropertyUtilTest.decrypted));
	}

	

}