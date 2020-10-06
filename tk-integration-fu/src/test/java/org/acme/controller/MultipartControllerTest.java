package org.acme.controller;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import javax.inject.Inject;

import org.acme.service.MultipartService;
import org.apache.http.HttpStatus;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
@TestMethodOrder(OrderAnnotation.class)
public class MultipartControllerTest {

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

	static File file;
	
	static String processId;
	
	@Test
	@Order(1)
	public void should_authenticate_and_create_a_token() {
			
		jwtToken=mpService.retrieveToken(username,password);
		
		assertThat(jwtToken, is(not(emptyOrNullString())));
		
	}
	
	@Test
	@Order(2)
	public void should_upload_a_CV() throws URISyntaxException {
		ClassLoader classLoader = getClass().getClassLoader();

		file = new File(classLoader.getResource(testFileUrl).toURI());

		processId=
			given()
				.header("content-type", "multipart/form-data")
				.header("Authorization", "Bearer "+jwtToken)
				.multiPart("file", file).formParam("fileName", file.getName())
				.when().post("/submit")
				.then()
					.statusCode(HttpStatus.SC_CREATED)
					.extract().asString();
					;
		assertThat(processId, is(not(emptyOrNullString())));
	}
	@Test
	@Order(3)
	public void should_download_a_CV_from_process_id() throws URISyntaxException, IOException {
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

}