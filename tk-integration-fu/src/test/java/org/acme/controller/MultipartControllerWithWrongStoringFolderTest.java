package org.acme.controller;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Base64;

import javax.inject.Inject;

import org.acme.profile.WrongFolderProfile;
import org.acme.service.MultipartService;
import org.apache.http.HttpStatus;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;

@QuarkusTest
@TestProfile(value = WrongFolderProfile.class)
@TestMethodOrder(OrderAnnotation.class)
class MultipartControllerWithWrongStoringFolderTest {

	@ConfigProperty(name = "acme.basic.auth.user",defaultValue = "misterX")
	String username;
	
	@ConfigProperty(name = "acme.basic.auth.password",defaultValue = "xxx")
	String password;

	static String jwtToken;
	
	@Inject
	MultipartService mpService;

	@ConfigProperty(name = "test-file")
	String testFileUrl;

	@ConfigProperty(name = "cv-store-folder")
	String cvStoreFolder;

	@ConfigProperty(name = "jwt.service-auth-port")
	Integer authPort;
	
	static File file;
	
	static String processId;
	
	
	@Test
	@Order(1)
	void should_authenticate_and_create_a_token() {
			
		String encodedString=Base64.getEncoder().encodeToString((username+":"+password).getBytes());
		
		jwtToken=given().port(authPort)
			.header("Authorization", "Basic "+encodedString)
            .contentType(ContentType.JSON)
            .when()
            .get("/accesstoken")
            .then()
            	.statusCode(HttpStatus.SC_CREATED)
            	.assertThat().body("token", is(not(emptyString())))
            	.assertThat().body("createdAt", is(notNullValue()))
            	.assertThat().body("expireAt", is(notNullValue()))
            	.extract().path("token")
            	;
		
		assertThat(jwtToken, is(not(emptyOrNullString())));
		
	}
	
	
	@Test
	@Order(2)
	void should_return_500_when_upload_a_file_on_unexistant_folder() throws URISyntaxException {
		ClassLoader classLoader = getClass().getClassLoader();

		file = new File(classLoader.getResource(testFileUrl).toURI());
		
		assertThat(file.exists(), is(true));
		
			given()
				.header("content-type", "multipart/form-data")
				.multiPart("file", file)
				.header("Authorization", "Bearer "+jwtToken)
				.formParam("fileName", file.getName())
				.when().post("/submit")
				.then()
					.statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR)
					
					;
	}
	

}