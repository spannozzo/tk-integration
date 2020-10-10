package org.acme.controller;

import java.io.InputStream;
import java.util.concurrent.CompletionStage;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.acme.dto.MultipartBodyDTO;
import org.acme.dto.ProfileDTO;
import org.acme.dto.TokenDTO;
import org.acme.restclient.AuthRestClient;
import org.acme.restclient.FuRestClient;
import org.acme.service.IntegrationService;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.security.SecuritySchemes;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;

@RequestScoped
@Path("/")

@SecuritySchemes(value= {
		@SecurityScheme(securitySchemeName="basicAuth",type = SecuritySchemeType.HTTP,scheme = "Basic"),
		@SecurityScheme(securitySchemeName = "jwt", type = SecuritySchemeType.HTTP, scheme = "Bearer")
})

public class TKIntegrationController {

	@Inject
	@NotNull
	JsonWebToken jwt;
	
	@Inject
	IntegrationService integrationService;
	
	@Inject
    @RestClient
    FuRestClient fuService;
	
	@Inject
    @RestClient
    AuthRestClient authService;
	
    @GET
    @RolesAllowed({ "ADMIN" })
    @Path("/retrieve/{processId}")
	@Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_XML)
	@Operation(summary = "use process id to retrieve additional information on a cv")
	@SecurityRequirement(name = "jwt")
    @APIResponses(value = { 
			@APIResponse(responseCode = "200", description = "Content retrieved from TK Service", content = {
					@Content(mediaType = MediaType.TEXT_XML, schema = @Schema(implementation = ProfileDTO.class)) 
					
			}),
			@APIResponse(responseCode = "401", description = "Invalid credentials", content = @Content),
			@APIResponse(responseCode = "400", description = "Bad request", content = @Content),
			@APIResponse(responseCode = "500", description = "Error parsing the retrieved content", content = @Content),
			@APIResponse(responseCode = "202", description = "Accepted: The request has been accepted for processing, "
					+ "but there is a previous process still in progress. It will respond with 'PROGRESS' ", content = @Content)

	})
    public CompletionStage<Response> retrieveData(@PathParam("processId") @NotBlank String processId)  {
    	    	
    	InputStream fileIS=integrationService.findFile(jwt.getRawToken(),processId);
    	    	
    	return integrationService.getTKServiceResponse(fileIS, processId);
    		
    }
  
    /**
     * required for Kubernetes gold application, 
     * on local the auth and file upload services are available either with ssl or through tcp
     * @param authorizationValue
     * @return
     */
    @GET
    @PermitAll
	@Path("/accesstoken")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Operation(summary = "create a token and return it with information about created and expired local datetime information")
	@SecurityRequirement(name = "basicAuth")
		
	@APIResponses(value = { 
			@APIResponse(responseCode = "201", description = "Token created", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = TokenDTO.class)) 
					
			}),
			@APIResponse(responseCode = "401", description = "Invalid credentials", content = @Content),
			@APIResponse(responseCode = "400", description = "Bad request", content = @Content)
			
	})
	public Response getToken(@HeaderParam("Authorization") @NotNull String authorizationValue) {
    	    	
    	return authService.getToken(authorizationValue);

	}
    
    /**
     * required for Kubernetes gold application
     * on local the auth and file upload services are available either with ssl or through tcp,
     * but with kubernetes they are declared as clusterIp so they are available only from the main
     * service
     * @param authorizationValue
     * @param data
     * @return
     */
    @POST
	@RolesAllowed({ "USER", "ADMIN" })
	@Path("/submit")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.TEXT_PLAIN)
	@Operation(summary = "upload a file and retrieve a process id")
	@SecurityRequirement(name = "jwt")
	@APIResponses(value = { 
			@APIResponse(responseCode = "201", description = "process id created", content = {
					@Content(mediaType = "application/json",schema = @Schema(type=SchemaType.STRING)) 
					
			}),
			@APIResponse(responseCode = "401", description = "Invalid credentials", content = @Content),
			@APIResponse(responseCode = "400", description = "Bad file submission", content = @Content),
			@APIResponse(responseCode = "500", description = "Error saving the file", content = @Content)
			
	})
	public Response uploadFile(
			@HeaderParam("Authorization") @NotNull String authorizationValue,
				@RequestBody(content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA, schema = @Schema(type = SchemaType.OBJECT))) 
				@MultipartForm MultipartBodyDTO data) {
				
		return fuService.uploadFile(authorizationValue, data);

	}
}