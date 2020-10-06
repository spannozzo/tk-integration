package org.acme.controller;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;

import javax.inject.Inject;

import org.acme.dto.MultipartBodyDTO;
import org.acme.restclient.FuRestClient;
import org.acme.service.IntegrationService;
import org.apache.http.HttpStatus;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestMethodOrder(OrderAnnotation.class)
public class TKIntegrationControllerTest {

	
	@ConfigProperty(name = "acme.basic.auth.user",defaultValue = "misterX")
	String username;
	
	@ConfigProperty(name = "acme.basic.auth.password",defaultValue = "xxx")
	String password;
	
	@ConfigProperty(name = "test-file")
	String testFileUrl;

	static String jwtToken;
	
	static File file;
	
	static String processId;
	
	@Inject
	IntegrationService integrationService;
	
	@Inject
    @RestClient
    FuRestClient fuService;
	
    @Test
    @Order(1)
    public void should_retrieve_token() {
    			
    	jwtToken=integrationService.retrieveToken(username,password);
		
		assertThat(jwtToken, is(not(emptyOrNullString())));
    }
    
    @Test
	@Order(2)
	public void should_upload_a_CV() throws URISyntaxException, IOException {
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
	public void should_be_able_to_retrieve_the_uploaded_file_and_passing_to_the_TK_service() throws URISyntaxException {
		
		given()
			.header("Authorization", "Bearer "+jwtToken)
			.pathParam("processId", processId)
			.when().get("/retrieve/{processId}")
			.then()
				.statusCode(HttpStatus.SC_OK)
				
				;
		
	}
}