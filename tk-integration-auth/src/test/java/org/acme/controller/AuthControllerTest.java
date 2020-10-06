package org.acme.controller;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

import java.util.Base64;

import org.acme.dto.TokenDTO;
import org.acme.dto.TokenValidationDTO;
import org.apache.http.HttpStatus;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
@TestMethodOrder(OrderAnnotation.class)
public class AuthControllerTest {

	
	@ConfigProperty(name = "acme.basic.auth.user",defaultValue = "misterX")
	String username;
	
	@ConfigProperty(name = "acme.basic.auth.password",defaultValue = "xxx")
	String password;

	static TokenDTO tokenDTO;
		
	
	@Test
	@Order(1)
	public void should_give_401_when_password_are_wrong() {
		String encodedString=Base64.getEncoder().encodeToString(("mister_x"+":"+"1234").getBytes());
		
		given()
			.header("Authorization", "Basic "+encodedString)
            .contentType(ContentType.JSON)
            .when()
            .get("/accesstoken")
            .then()
            	.statusCode(HttpStatus.SC_UNAUTHORIZED)
            	
            	;
	}
	@Test
	@Order(1)
	public void should_give_400_when_basic_auth_is_malformed() {
		String encodedString=Base64.getEncoder().encodeToString((username+"xxx"+password).getBytes());
		
		given()
			.header("Authorization", "Basic "+encodedString)
            .contentType(ContentType.JSON)
            .when()
            .get("/accesstoken")
            .then()
            	.statusCode(HttpStatus.SC_BAD_REQUEST)
            	
            	;
	}
	@Test
	@Order(1)
	public void should_give_400_when_authorization_header_is_missing() {
		String encodedString=Base64.getEncoder().encodeToString((username+":"+password).getBytes());
		
		given()
			.header("another_header", "Basic "+encodedString)
            .contentType(ContentType.JSON)
            .when()
            .get("/accesstoken")
            .then()
            	.statusCode(HttpStatus.SC_BAD_REQUEST)
            	
            	;
	}
	@Test
	@Order(1)
	public void should_give_400_when_authorization_Basic_is_missing() {
		String encodedString=Base64.getEncoder().encodeToString((username+":"+password).getBytes());
		
		given()
			.header("Authorization", encodedString)
            .contentType(ContentType.JSON)
            .when()
            .get("/accesstoken")
            .then()
            	.statusCode(HttpStatus.SC_BAD_REQUEST)
            	
            	;
	}
	@Test
	@Order(2)
	public void should_authenticate_and_create_a_token() {
		
			String encodedString=Base64.getEncoder().encodeToString((username+":"+password).getBytes());
		
			tokenDTO=given()
				.header("Authorization", "Basic "+encodedString)
                .contentType(ContentType.JSON)
                .when()
                .get("/accesstoken")
                .then()
                	.statusCode(HttpStatus.SC_CREATED)
                	.assertThat().body("token", is(not(emptyString())))
                	.assertThat().body("createdAt", is(notNullValue()))
                	.assertThat().body("expireAt", is(notNullValue()))
                	.extract().as(TokenDTO.class)
                	;
			assertThat(tokenDTO, is(notNullValue()));
	}
	@Test
	@Order(3)
	public void should_be_able_to_validate_token() {
				
			TokenValidationDTO tokenValidation=given()
				.contentType("text/html")
				.param(tokenDTO.getToken())
                .when()
                .post("/accesstoken/check")               
                .then()
                	.statusCode(HttpStatus.SC_OK)
                	
                	.extract().as(TokenValidationDTO.class)
                	;
			assertThat(tokenValidation, is(notNullValue()));
			assertThat(tokenValidation.getValid(), is(true));
	}

}