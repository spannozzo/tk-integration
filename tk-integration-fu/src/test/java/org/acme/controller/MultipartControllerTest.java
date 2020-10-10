package org.acme.controller;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.inject.Inject;

import org.acme.restclient.AuthRestClient;
import org.acme.service.MultipartService;
import org.apache.http.HttpStatus;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
@TestMethodOrder(OrderAnnotation.class)
class MultipartControllerTest {

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

	@Inject
	@RestClient
	AuthRestClient authRestClient;
	
	static File file;
	
	static String processId;
	
	String expiredToken="eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJpc3MiOiJodHRwczovL3F1YXJrdXMuaW8vdXNpbmctand0LXJiYWMiLCJ1cG4iOiJzcGFubm96em9AYWNtZS5qd3QiLCJncm91cHMiOlsiQURNSU4iLCJVU0VSIl0sImlhdCI6MTYwMjI5MDMzOCwiZXhwIjoxNjAyMjkzOTM4LCJ6b25laW5mbyI6IkdNVCIsImp0aSI6InNtSjhCbzhfZl8zWk1rVGhsTkRkQlEifQ.SovPcr5SNFHuMZl5HLcGc20kYHNuvT9gLe-Im_GqUDaaFR2XXszJ3IVVKbc1YypGZVfLB4QuKveMOk5d20QOURuwIAUySTVc0wQAo7ISG75RCdZXAlQ2er-PjLZsnRpmkdiRkNR2rC8uX4cKdG8EtHmE4zScUTMx0RrGjhpPgV2d6Zm4jFxmiNwraY3v8iGHl-XoPQp7YDjC8iIHP3kx029bGeZ0pEX9nkcE-VFslpiCJ9bJLkX3kIFmgnF0RKSy6IyeEPVek5wpFJ9HZ9gvsapzsaTY2lxYARiXzRSDq9Q3mUoDDAlXTA2T4Bfg5uqvJGD-3NybyehaQIPYnvfUnw"
			;
	
	
	@Test
	@Order(0)
	void should_return_401_when_upload_a_file_with_expired_token() throws URISyntaxException  {
		
		ClassLoader classLoader = getClass().getClassLoader();

		file = new File(classLoader.getResource(testFileUrl).toURI());
			given()
				.header("content-type", "multipart/form-data")
				.multiPart("file", file)
				.header("Authorization", "Bearer "+expiredToken)
				.formParam("fileName", file.getName())
				.when().post("/submit")
				.then()
					.statusCode(HttpStatus.SC_UNAUTHORIZED)
					
					;
	}
	
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
	void should_return_400_when_upload_a_file_without_file_name() throws URISyntaxException {
		ClassLoader classLoader = getClass().getClassLoader();

		file = new File(classLoader.getResource(testFileUrl).toURI());
			given()
				.header("content-type", "multipart/form-data")
				.multiPart("file", file)
				.header("Authorization", "Bearer "+jwtToken)
				.formParam("fileName", "")
				.when().post("/submit")
				.then()
					.statusCode(HttpStatus.SC_BAD_REQUEST)
					
					;
	}
	@Test
	@Order(2)
	void should_return_401_when_upload_a_file_without_bearer_string_before_token() throws URISyntaxException {
		ClassLoader classLoader = getClass().getClassLoader();

		file = new File(classLoader.getResource(testFileUrl).toURI());
			given()
				.header("content-type", "multipart/form-data")
				.multiPart("file", file)
				.header("Authorization", ""+jwtToken)
				.formParam("fileName", file.getName())
				.when().post("/submit")
				.then()
					.statusCode(HttpStatus.SC_UNAUTHORIZED)
					
					;
	}
	@Test
	@Order(2)
	void should_return_401_when_upload_a_file_with_wrong_token() throws URISyntaxException {
		ClassLoader classLoader = getClass().getClassLoader();

		file = new File(classLoader.getResource(testFileUrl).toURI());
			given()
				.header("content-type", "multipart/form-data")
				.multiPart("file", file)
				.header("Authorization", "Bearer "+jwtToken+"xyz")
				.formParam("fileName", file.getName())
				.when().post("/submit")
				.then()
					.statusCode(HttpStatus.SC_UNAUTHORIZED)
					
					;
	}
	
	
	@Test
	@Order(3)
	void should_upload_a_CV() throws URISyntaxException {
		

		processId=
			given()
				.header("content-type", "multipart/form-data")
				.header("Authorization", "Bearer "+jwtToken)
				.multiPart("file", file)
				.formParam("fileName", file.getName())
				.when().post("/submit")
				.then()
					.statusCode(HttpStatus.SC_CREATED)
					.extract().asString();
					;
		assertThat(processId, is(not(emptyOrNullString())));
	}
	

	
	@Test
	@Order(4)
	void should_download_a_CV_from_process_id() throws URISyntaxException, IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		
		InputStream retrievedFileIS=
			given()
				.contentType(ContentType.JSON)
				.header("Authorization", "Bearer "+jwtToken)
				.pathParam("processId", processId)
				.when().post("/{processId}")
				.then()
					.statusCode(HttpStatus.SC_OK)
					.extract().asInputStream()
					;
		assertThat(retrievedFileIS, is(notNullValue()));
		assertThat(retrievedFileIS, is(not(InputStream.nullInputStream())));
		
		
		String inputFileContent= new String(classLoader.getResourceAsStream(testFileUrl).readAllBytes(), StandardCharsets.UTF_8);
		String outputFileContent= new String(retrievedFileIS.readAllBytes(), StandardCharsets.UTF_8);
		
		StringBuilder filePathSB=new StringBuilder(cvStoreFolder).append(processId).append("_").append(file.getName());
		
		File outputFile=new File(filePathSB.toString());
			
		
		assertThat(outputFile.exists(), is(true));
		assertThat(outputFile.delete(), is(true));
		
				
		assertThat(outputFileContent, is(inputFileContent));
		
	}
	@Test
	@Order(5)
	void should_return_404_when_try_to_retrieve_file_from_unexistant_id(){
				
			given()
				.contentType(ContentType.JSON)
				.header("Authorization", "Bearer "+jwtToken)
				.pathParam("processId", "12345")
				.when().post("/{processId}")
				.then()
					.statusCode(HttpStatus.SC_NOT_FOUND)
					
					;
	
		
	}
}