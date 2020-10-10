package org.acme.util;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyOrNullString;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
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

	static String encrypted;
	
	@ConfigProperty(name = "acme.jwt.value",defaultValue = "myuser")
	String value;

	
	@ConfigProperty(name = "acme.aes.password",defaultValue = "xxx")
	String encPassword;

	@Inject
	AESUtil aesUtils;
	
	@Test
	@Order(1)
	void shoudl_generate_key() throws InvalidKeyException, UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
		
		encrypted=aesUtils.encrypt(value, encPassword);
		
		assertThat(encrypted, is(not(emptyOrNullString())));
		assertThat(Base64.getDecoder().decode(encrypted), is(not(value)));
		
	}
	
	@Test
	@Order(2)
	void should_decrypt_into_original_password_value() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException  {
		String decrypted = aesUtils.decrypt(encrypted,encPassword);
		
		assertThat(decrypted, is(notNullValue()));
		assertThat(decrypted, is(value));
	}
	
	

}