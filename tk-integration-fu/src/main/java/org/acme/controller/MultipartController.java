package org.acme.controller;

import java.io.InputStream;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.acme.dto.MultipartBodyDTO;
import org.acme.service.MultipartService;
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
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;

@RequestScoped
@Path("/")
@SecurityScheme(securitySchemeName = "jwt", type = SecuritySchemeType.HTTP, scheme = "Bearer")
public class MultipartController {

	@Inject
	@NotNull
	JsonWebToken jwt;

	@Inject
	MultipartService mpService;

	
	
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
				@MultipartForm @Valid MultipartBodyDTO data) {
		
		String processId=mpService.storeFile(data);
		
		return Response.status(Response.Status.CREATED).entity(processId).build();

	}
	@POST
	@RolesAllowed({ "ADMIN" })
	@Path("/{processId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Operation(summary = "retrieve a stored file from a process id")
	@SecurityRequirement(name = "jwt")
	@APIResponses(value = { 
			@APIResponse(responseCode = "200", description = "Returns the requested file", content = {
					@Content(mediaType = "application/json", schema = @Schema(type = SchemaType.OBJECT)) 
			}),
			@APIResponse(responseCode = "401", description = "Invalid credentials", content = @Content),
			@APIResponse(responseCode = "400", description = "Bad request", content = @Content),
			@APIResponse(responseCode = "404", description = "No file could be retrieved for the requested process id", content = @Content)
	})
	public Response retrieveFile( 	@HeaderParam("Authorization") @NotNull String authorizationValue,
									@PathParam("processId") @NotBlank String processId) {
				
		InputStream retrievedFileIS = mpService.getFileInputStream(processId);
		
		return Response.status(Response.Status.OK).entity(retrievedFileIS).build();

	}

}
