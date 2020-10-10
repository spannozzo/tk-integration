package org.acme.controller;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
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
import io.restassured.http.ContentType;

import static org.hamcrest.Matchers.hasItems;

@QuarkusTest
@TestMethodOrder(OrderAnnotation.class)
class TKIntegrationControllerTest {

	
	@ConfigProperty(name = "acme.basic.auth.user",defaultValue = "misterX")
	String username;
	
	@ConfigProperty(name = "acme.basic.auth.password",defaultValue = "xxx")
	String password;
	
	@ConfigProperty(name = "test-file1")
	String testFileUrl;
	
	@ConfigProperty(name = "test-file2")
	String txtFileUrl;

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
    void should_retrieve_token() throws InterruptedException {
    			
    	String encodedString=Base64.getEncoder().encodeToString((username+":"+password).getBytes());
		
    	jwtToken=given()
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
		
    	
    }
    
    @Test
	@Order(2)
	void should_upload_a_CV() throws URISyntaxException, IOException {
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
	void should_retrieve_400_when_multipart_is_without_file_name() throws URISyntaxException {
		
    	given()
		.header("content-type", "multipart/form-data")
		.header("Authorization", "Bearer "+jwtToken)
		.multiPart("file", file)
		.when().post("/submit")
		.then()
			.statusCode(HttpStatus.SC_BAD_REQUEST)
				
				;
		
	}
    
    @Test
	@Order(4)
	void should_retrieve_401_with_wrong_token() throws URISyntaxException {
		
		given()
			.header("Authorization", "Bearer "+jwtToken+"xyz")
			.pathParam("processId", processId)
			.when().get("/retrieve/{processId}")
			.then()
				.statusCode(HttpStatus.SC_UNAUTHORIZED)
				
				;
		
	}
    @Test
	@Order(4)
	void should_retrieve_400_with_empty_process_id() throws URISyntaxException {
		
		given()
			.header("Authorization", "Bearer "+jwtToken)
			.pathParam("processId", " ")
			.when().get("/retrieve/{processId}")
			.then()
				.statusCode(HttpStatus.SC_BAD_REQUEST)
				
				;
		
	}
    
    @Test
   	@Order(4)
   	void should_retrieve_404_with_unexistant_process_id() throws URISyntaxException {
   		
   		given()
   			.header("Authorization", "Bearer "+jwtToken)
   			.pathParam("processId", "1234_xyz.extension")
   			.when().get("/retrieve/{processId}")
   			.then()
   				.statusCode(HttpStatus.SC_NOT_FOUND)
   				
   				;
   		
   	}
    
    @Test
	@Order(4)
	void should_return_500_when_try_to_pass_txt_file_to_tk_service() throws URISyntaxException {
    	
    	ClassLoader classLoader = getClass().getClassLoader();

		file = new File(classLoader.getResource(txtFileUrl).toURI());
    	    	
    	String txtFileId=
				given()
					.header("content-type", "multipart/form-data")
					.header("Authorization", "Bearer "+jwtToken)
					.multiPart("file",file ).formParam("fileName", file.getName())
					.when().post("/submit")
					.then()
						.statusCode(HttpStatus.SC_CREATED)
						.extract().asString();
						;
			assertThat(txtFileId, is(not(emptyOrNullString())));
    	
		given()
			.header("Authorization", "Bearer "+jwtToken)
			.pathParam("processId", txtFileId)
			.when().get("/retrieve/{processId}")
			.then()
				.statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR)
				
				;
	}
    
    @Test
	@Order(5)
	void should_be_able_to_retrieve_the_uploaded_file_and_passing_to_the_TK_service() throws URISyntaxException {
		given()
			.header("Authorization", "Bearer "+jwtToken)
			.pathParam("processId", processId)
			.when().get("/retrieve/{processId}")
			.then()
				.statusCode(HttpStatus.SC_OK)
				
				;
	}
    
    @Test
	@Order(6)
	void should__retrieve_in_progress_when_multiple_call() throws URISyntaxException, IOException, InterruptedException, ExecutionException {
    	
    	List<Integer> statusCodes=new ArrayList<>();
    		
    	Runnable r=(()->{
    		int status=given()
    				.header("Authorization", "Bearer "+jwtToken)
    				.pathParam("processId", processId)
    				.when().get("/retrieve/{processId}")
    				.then()
    					.extract()
    					.response().statusCode()
    					;
    		statusCodes.add(status);
    		
    	});
    	
    	List<Thread> threads=
	    	Stream.generate(()->new Thread(r))
	    	.limit(5)
	    	.collect(Collectors.toList())
    	;

    	threads.parallelStream().forEach(Thread::start);
    	
    	threads.stream().forEach(t -> {
			try {
				t.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
    	
    	
    	assertThat(statusCodes, hasItems(202,200));
    	
    	
	}
    
    
    
}