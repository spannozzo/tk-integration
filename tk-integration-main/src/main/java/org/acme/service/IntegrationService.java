package org.acme.service;


import java.io.InputStream;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.core.Response;

import org.acme.config.DecryptedMPRequestDtoConfig;
import org.acme.dto.MultipartTKRequestDTO;
import org.acme.dto.ProfileDTO;
import org.acme.exceptions.EncryptionCredentialException;
import org.acme.exceptions.RequestWhileBusyException;
import org.acme.restclient.AuthRestClient;
import org.acme.restclient.FuRestClient;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

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
	
	
    static final Logger LOG = Logger.getLogger(IntegrationService.class);

	
	BlockingQueue<String> requestQueue=new ArrayBlockingQueue<>(50);
	
		
	public InputStream findFile(String token,String processId) {
		
		Response response=fuService.retrieveFile("Bearer "+token,processId);
			
		return (InputStream) response.getEntity();
		
	}

	public CompletionStage<Response> getTKServiceResponse(InputStream data,String processId) {
		return CompletableFuture.supplyAsync(() ->
			manageProcessId(processId)
		)
		.thenCompose(pId-> getAsyncGetDecryptedMultipartDTO(data,pId))
		.thenCompose(credentials->asyncExtract(credentials, processId))
		.thenCompose(profile ->{
			boolean removed=requestQueue.remove(processId);
			
			LOG.info("process id "+processId+" realeased: "+removed);
	
			return CompletableFuture.supplyAsync(()->Response.ok(profile));
		})
		.thenApply(Response.ResponseBuilder::build);
	}	
 
	private String manageProcessId(String processId) {
		if (!requestQueue.contains(processId)) {
			requestQueue.add(processId);
		}
		else {
			throw new RequestWhileBusyException("request is already running for this process id:"+processId);
		}
		return processId;
		
	}

	private CompletableFuture<MultipartTKRequestDTO> getAsyncGetDecryptedMultipartDTO(InputStream data,String processId){
		
		return CompletableFuture.supplyAsync(() -> 
			
			decryptedMPRequestDtoConfig.getDecryptedMultipartDTO(data).orElseThrow(()->{
				
				boolean removed=requestQueue.remove(processId);
				
				LOG.info("process id "+processId+" realeased: "+removed);
				
				throw new EncryptionCredentialException("Couln't authenticate service credentials");
			})
			
		);
		
	}
	private CompletableFuture<ProfileDTO> asyncExtract(MultipartTKRequestDTO requestDTO,String processId){
		
		return CompletableFuture.supplyAsync(() -> 
			tkService.extract(requestDTO, processId)
		);
		
	}	
}
