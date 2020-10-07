package org.acme.controller;

import java.security.NoSuchProviderException;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.acme.dto.BasicAuthDTO;
import org.acme.dto.TokenDTO;
import org.acme.dto.TokenValidationDTO;
import org.acme.exceptions.MalformedBasicAuthenticationException;
import org.acme.service.AuthService;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.jose4j.jwt.consumer.InvalidJwtException;


@RequestScoped
@Path("/accesstoken")
@SecurityScheme(securitySchemeName="basicAuth",type = SecuritySchemeType.HTTP,scheme = "Basic")
public class AuthController {

	@Inject
	AuthService authService;
	
	@GET
	@Path("/")
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
	public Response getToken(@HeaderParam("Authorization") @NotNull String authorizationValue) throws NoSuchProviderException {
		BasicAuthDTO credentials=new BasicAuthDTO();
		try {
			credentials=authService.getCredentials(authorizationValue);
		}catch (IndexOutOfBoundsException | MalformedBasicAuthenticationException e) {
			throw new MalformedBasicAuthenticationException(e.getMessage(),e);
		}
		
		TokenDTO returnValue=authService.getCreateToken(credentials);
				
		return Response.status(Response.Status.CREATED).entity(returnValue).build();

	}
	
	@POST
	@Path("/check")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.TEXT_HTML)
	@Operation(summary = "check if a token is valid and return remaing minutes before expiration")
	@APIResponses(value = { 
			@APIResponse(responseCode = "200", description = "Token status information", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = TokenValidationDTO.class)) 
			}),
			@APIResponse(responseCode = "400", description = "Invalid or malformed token", content = @Content)
	})
	public Response validateToken(@Valid @NotBlank String token) throws InvalidJwtException {

		TokenValidationDTO returnValue = null;
		
		returnValue = authService.validateToken(token);
		
		return Response.status(Response.Status.OK).entity(returnValue).build();

	}

}
