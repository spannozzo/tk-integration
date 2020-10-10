package org.acme.util;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Base64;

import javax.validation.ValidationException;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;


@TestMethodOrder(OrderAnnotation.class)
class AESUtilTest {

	static String encValue;
	
	
	String value="my_value";

	String encPassword="1234";

	AESUtil util=AESUtil.getInstance();
	
	@Test
	@Order(1)
	void shoudl_generate_key() {
		
		encValue=util.encrypt(value, encPassword);
		
		assertThat(encValue, is(notNullValue()));
		assertThat(Base64.getDecoder().decode(encValue), is(not(value)));
		
	}
	
	@Test
	@Order(2)
	void should_decrypt_into_original_password_value() {
		String decrypted = AESUtil.getInstance().decrypt(encValue,encPassword);
		
		assertThat(decrypted, is(notNullValue()));
		assertThat(decrypted, is(value));
	}
	
	@Test
	@Order(3)
	void fail_decrypt_malformed_encoded_string() {
		
		Exception exception = assertThrows(
				ValidationException.class,
				() -> util.decrypt("abc", "yyy"));
		
		assertThat(exception.getClass().getSimpleName(),is("EncryptionCredentialException"));
		
		
	}

}