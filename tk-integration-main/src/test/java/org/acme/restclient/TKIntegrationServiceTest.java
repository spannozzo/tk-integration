package org.acme.restclient;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import javax.inject.Inject;
import org.acme.config.DecryptedMPRequestDtoConfig;
import org.acme.dto.MultipartTKRequestDTO;
import org.acme.dto.ProfileDTO;
import org.acme.service.TkService;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestMethodOrder(OrderAnnotation.class)
 class TKIntegrationServiceTest {

		
	String password;
	
	@ConfigProperty(name = "test-file1")
	String testFileUrl;
		
	static File file;
	
	@Inject
	DecryptedMPRequestDtoConfig decryptedMPRequestDtoConfig;
		
	@Inject
    TkService tkService;

	private static MultipartTKRequestDTO requestDTO;
	
    @Test
    @Order(1)
    void should_retrieve_xml_from_rest_client() throws URISyntaxException, IOException {
    	ClassLoader classLoader = getClass().getClassLoader();
    	file = new File(classLoader.getResource(testFileUrl).toURI());	
    	
    	InputStream data=Files.newInputStream(file.toPath());
    	
    	requestDTO=decryptedMPRequestDtoConfig.getDecryptedMultipartDTO(data).get();
    	
    	ProfileDTO xmlResult=tkService.extract(requestDTO,file.getName());	
    	
    	assertThat(xmlResult, is(notNullValue()));
    	assertThat(xmlResult.getAddress(), is(notNullValue()));
    	assertThat(xmlResult.getFirstName(), is(not(emptyOrNullString())));
    }
    
}