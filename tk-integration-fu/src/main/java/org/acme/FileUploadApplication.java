package org.acme;

import javax.ws.rs.core.Application;

import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.info.License;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
@OpenAPIDefinition(
	    tags = {
	            @Tag(name="Tk Integration - File upload microservice", 
	            		description="Use JWT microservice in order to authenticate for storing and retrieving files"),
	    },
	    info = @Info(
	        title="TK File Upload - Integration API",
	        version = "3.1",
	        contact = @Contact(
	            name = "Spannozzo",
	            url = "http://acme.org/contacts",
	            email = "spannozzo@acme.org"),
	        license = @License(
	            name = "Apache 2.0",
	            url = "http://www.apache.org/licenses/LICENSE-2.0.html"))
	)
	public class FileUploadApplication extends Application {
	}
