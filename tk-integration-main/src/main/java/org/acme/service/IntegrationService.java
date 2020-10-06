package org.acme.service;


import java.io.InputStream;
import java.io.StringReader;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.ws.rs.core.Response;

import org.acme.config.DecryptedMPRequestDtoConfig;
import org.acme.dto.MultipartTKRequestDTO;
import org.acme.dto.ProfileDTO;
import org.acme.exceptions.InvalidJWTException;
import org.acme.exceptions.RequestWhileBusyException;
import org.acme.exceptions.TKResponseException;
import org.acme.restclient.AuthRestClient;
import org.acme.restclient.FuRestClient;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Singleton
public class IntegrationService {

	@Inject
    @RestClient
    AuthRestClient authService;
	
	@Inject
    @RestClient
    FuRestClient fuService;
	
	@Inject
	DecryptedMPRequestDtoConfig decryptedMPRequestDtoConfig;
	
	@Inject
    TkService tkService;
	
	BlockingQueue<String> requestQueue=new ArrayBlockingQueue<>(50);
	
	public String retrieveToken(String username, String password) {
		String encodedString="Basic "+Base64.getEncoder().encodeToString((username+":"+password).getBytes());
		Response tokenResp=authService.getToken(encodedString);
		
		String jsonBody=tokenResp.readEntity(String.class);
		JsonReader jsonReader = Json.createReader(new StringReader(jsonBody));
		JsonObject jsonObject = jsonReader.readObject();
		
		return jsonObject.getString("token");
		
	}

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

	public InputStream findFile(String token,String processId) {

		InputStream fileIS=InputStream.nullInputStream();
		
		Response response=fuService.retrieveFile("Bearer "+token,processId);
		
		fileIS=(InputStream) response.getEntity();
		
		return fileIS;
		
	}

	/**
	 * Convert the encrypted credentials configuration and pass it to the TK service.
	 * The apache client Api used in the extract method is expecting a filename, but we have the inputstream, not the file. 
	 * Fortunately every string value is ok, so I'm passing the processId itself
	 * 
	 * @param data
	 * @param processId
	 * @return xml result
	 * @throws Throwable 
	 * @throws ExecutionException 
	 * @throws InterruptedException 
	 */
	public ProfileDTO callTkService(InputStream data,String processId) throws Throwable {

		CompletableFuture<ProfileDTO> storePidRunRequestRemovePid=CompletableFuture.supplyAsync(()->{
			if (!requestQueue.contains(processId)) {
				requestQueue.add(processId);
			}else {
				throw new RequestWhileBusyException("request is already running for this process id:"+processId);
			}
			return processId;
		}).thenApply(storedProcessId->{
			MultipartTKRequestDTO requestDTO=null;
			try {
				requestDTO=decryptedMPRequestDtoConfig.getDecryptedMultipartDTO(data);
			} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException
					| BadPaddingException | NumberFormatException e) {
				requestQueue.remove(processId);
				throw new EncryptionCredentialException(e.getMessage(),e);
			}
			ProfileDTO retrievedProfile=null;
			try {
				retrievedProfile=tkService.extract(requestDTO, storedProcessId);
			} catch (Exception e) {
				requestQueue.remove(processId);
				throw new TKResponseException(e.getMessage(),e);
			}
			
			return retrievedProfile;
				
		}).thenApply(profile->{
			requestQueue.remove(processId);
			
			return profile;
		});
			
		try {
			return storePidRunRequestRemovePid.get();
		} catch (InterruptedException | ExecutionException e) {
			throw e.getCause();
		}
	}

	CompletableFuture<ProfileDTO> asyncReq(MultipartTKRequestDTO multipartTKRequestDTO, String fileName) {
		return CompletableFuture.supplyAsync(() -> {
			return tkService.extract(multipartTKRequestDTO, fileName);
		});
	}   
}
