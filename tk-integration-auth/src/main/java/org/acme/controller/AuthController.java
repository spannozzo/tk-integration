package org.acme.controller;

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
import org.acme.service.AuthService;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;


@RequestScoped
@Path("/accesstoken")
//@SecuritySchemes(value= {
//		@SecurityScheme(securitySchemeName="basicAuth",type = SecuritySchemeType.HTTP,scheme = "Basic"),
//		@SecurityScheme(securitySchemeName = "jwt", type = SecuritySchemeType.HTTP, scheme = "Bearer")
//})
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
	public Response getToken(@Valid @HeaderParam("Authorization") @NotNull String authorizationValue) {
				
		BasicAuthDTO credentials=authService.getCredentials(authorizationValue);
		
		TokenDTO returnValue=authService.getCreateToken(credentials);
				
		return Response.status(Response.Status.CREATED).entity(returnValue).build();

	}
	
		
	@POST
	@Path("/check")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.TEXT_HTML)
	@Operation(summary = "check if a token is valid and return remaing minutes before expiration")
//	@SecurityRequirement(name = "jwt")
	@APIResponses(value = { 
			@APIResponse(responseCode = "200", description = "Token status information", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = TokenValidationDTO.class)) 
			}),
			@APIResponse(responseCode = "400", description = "Invalid or malformed token", content = @Content)
	})
	public Response validateToken(@NotBlank String token) {
		
		TokenValidationDTO validatedToken=authService.validateToken(token);
		
		return Response.status(Response.Status.OK).entity(validatedToken).build();

	}

}
