package org.acme.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.validation.constraints.NotBlank;
import javax.ws.rs.core.Response;

import org.acme.dto.MultipartBodyDTO;
import org.acme.exceptions.InvalidJWTException;
import org.acme.restclient.AuthRestClient;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Singleton
public class MultipartService {

	@Inject
    @RestClient
    AuthRestClient authService;
	
	
	@ConfigProperty(name = "cv-store-folder")
	String cvStoreFolder;
	
	public void checkJwt(JsonWebToken jwt) {
		Response validationTokenResp=authService.validateToken(jwt.getRawToken());
		
		String jsonBody=validationTokenResp.readEntity(String.class);
		JsonReader jsonReader = Json.createReader(new StringReader(jsonBody));
		JsonObject jsonObject = jsonReader.readObject();
		
		Boolean isValid=jsonObject.getBoolean("valid");
		
		if (!isValid) {
			throw new InvalidJWTException("Invalid token provided");
		}
	}

	public String retrieveToken(String username, String password) {
		String encodedString="Basic "+Base64.getEncoder().encodeToString((username+":"+password).getBytes());

		Response tokenResp=authService.getToken(encodedString);
		
		String jsonBody=tokenResp.readEntity(String.class);
		JsonReader jsonReader = Json.createReader(new StringReader(jsonBody));
		JsonObject jsonObject = jsonReader.readObject();
		
		return jsonObject.getString("token");

	}

	public String storeFile(MultipartBodyDTO data) throws IOException {
		String pid= UUID.randomUUID().toString();
		
		StringBuilder fileNameSB=new StringBuilder(pid).append("_").append(data.getFileName());
		
				
		Path filePath = Path.of(cvStoreFolder, fileNameSB.toString());
		
		if (!Files.exists(filePath.getParent())) {
			Files.createDirectories(filePath.getParent());
		};
		
		Files.copy(data.getFile(), filePath, StandardCopyOption.REPLACE_EXISTING);
		
		
		return pid;
	}

	public InputStream getFileInputStream(@NotBlank String processId) throws IOException,IndexOutOfBoundsException {
		StringBuilder startingFileNameSB=new StringBuilder(processId).append("_");
		
		
		List<Path> fileNames = Files.list(Paths.get(cvStoreFolder)).parallel()
		        				.filter(path -> 
		        					
		        					path.getFileName().toString().startsWith(startingFileNameSB.toString())
		        					
		        				)
		        .collect(Collectors.toList());
		
		return Files.newInputStream(fileNames.get(0));
	}

}
