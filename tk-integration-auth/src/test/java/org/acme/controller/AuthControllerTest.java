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
class AuthControllerTest {

	
	@ConfigProperty(name = "acme.basic.auth.user",defaultValue = "misterX")
	String username;
	
	@ConfigProperty(name = "acme.basic.auth.password",defaultValue = "xxx")
	String password;

	static TokenDTO tokenDTO;
		
	
	String expiredToken="eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJpc3MiOiJod"
						+ "HRwczovL3F1YXJrdXMuaW8vdXNpbmctand0LXJiYWMiLCJ1cG4iOiJzcGFub"
						+ "m96em9AYWNtZS5qd3QiLCJncm91cHMiOlsiQURNSU4iLCJVU0VSIl0sImlhd"
						+ "CI6MTYwMTU3MjcyNywiZXhwIjoxNjAxNTczMDI3LCJ6b25laW5mbyI6IkV1c"
						+ "m9wZS9CZXJsaW4iLCJqdGkiOiIzM0NYVDB0NS1wQjdrLVAtdEZtenB3In0."
						+ "EPP4g5P78gf7YvokD_LQkZFjbDp4Icj2NT5zPcoFMAPTdFaXD3t_A5uApFx2"
						+ "stwb-yy143Qh3FUHp57Cawu5oQL31Q4OF1P6NZq_XmSvNhSMWn0qCZcLXIXWp"
						+ "ID3ScPoPrH5i8-TfGLKdGguVfX9JDhADJ1unkr0LBC7FOyC0AH3r8QfYJ2DKO"
						+ "dgGpWCCrv7NqfGyzzbCqU2COAj4rMz7bAJmip3aw0GC7otO2uzeL7oCj4K8bF"
						+ "_I5i9Upo7nhC4vu9ekDtSUkzRUX7h49AuK6EUe_ZL1ECdduZPmx6UsVisH0Fd"
						+ "gQC-gaux8X4sdmnj_48ElOn0xGT6CY4bTLzlqA"
	;
	
	@Test
	@Order(1)
	void should_give_401_when_password_are_wrong() {
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
	void should_give_400_when_basic_auth_is_malformed() {
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
	void should_give_400_when_authorization_header_is_missing() {
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
	void should_give_400_when_authorization_Basic_is_missing() {
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
	void should_authenticate_and_create_a_token() {
		
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
	void should_return_400_on_empty_token() {
				
			given()
				.contentType("text/html")
				.param("")
                .when()
                .post("/accesstoken/check")               
                .then()
                	.statusCode(HttpStatus.SC_BAD_REQUEST)
                ;
			
	}
	@Test
	@Order(3)
	void should_return_200_on_corrupted_token_with_reason_malformed_token() {
		// token created from first and last parts of current token +
		// some random string in the middle
		
			String malformedToken=tokenDTO.getToken().substring(0,tokenDTO.getToken().indexOf("."))
					+ ".eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ"
					+ tokenDTO.getToken().substring(tokenDTO.getToken().lastIndexOf("."));
			
			TokenValidationDTO tokenValidation=
				given()
					.contentType("text/html")
					.param(malformedToken)
	                .when()
	                .post("/accesstoken/check")               
	                .then()
	                	.statusCode(HttpStatus.SC_OK)
	                	
	                	.extract().as(TokenValidationDTO.class)
	                	;
				assertThat(tokenValidation, is(notNullValue()));
				assertThat(tokenValidation.getValid(), is(false));
				assertThat(tokenValidation.getReason(), is("Malformed Token"));
				assertThat(tokenValidation.getDuration(), is("0"));
	}
	@Test
	@Order(3)
	void should_return_200_on_expired_token_with_reason_expired_token() {
				
		TokenValidationDTO tokenValidation=
			given()
				.contentType("text/html")
				.param(expiredToken)
                .when()
                .post("/accesstoken/check")               
                .then()
                	.statusCode(HttpStatus.SC_OK)
                	
                	.extract().as(TokenValidationDTO.class)
                	;
			assertThat(tokenValidation, is(notNullValue()));
			assertThat(tokenValidation.getValid(), is(false));
			assertThat(tokenValidation.getReason(), is("Expired Token"));
			assertThat(tokenValidation.getDuration(), is("0"));
	}
	@Test
	@Order(3)
	void should_return_200_on_wrong_token_with_reason_malformed_token() {
				
		TokenValidationDTO tokenValidation=
			given()
				.contentType("text/html")
				.param("hello word")
                .when()
                .post("/accesstoken/check")               
                .then()
                	.statusCode(HttpStatus.SC_OK)
                	
                	.extract().as(TokenValidationDTO.class)
                	;
			assertThat(tokenValidation, is(notNullValue()));
			assertThat(tokenValidation.getValid(), is(false));
			assertThat(tokenValidation.getReason(), is("Malformed Token"));
			assertThat(tokenValidation.getDuration(), is("0"));
	}
	@Test
	@Order(4)
	void should_be_able_to_validate_token() {
				
			TokenValidationDTO tokenValidation=
			given()
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