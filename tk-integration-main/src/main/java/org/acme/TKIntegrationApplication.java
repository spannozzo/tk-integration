package org.acme;

import javax.ws.rs.core.Application;

import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.info.License;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
@OpenAPIDefinition(
	    tags = {
	            @Tag(name="Tk Integration - Main Integration microservice", 
	            		description="use upload service and auth service for authenticate, "
	            				+ "store files, and send this file for retrieving data from TK service"),
	    },
	    info = @Info(
	        title="TK Main - Integration API",
	        version = "3.1",
	        contact = @Contact(
	            name = "Spannozzo",
	            url = "http://acme.org/contacts",
	            email = "spannozzo@acme.org"),
	        license = @License(
	            name = "Apache 2.0",
	            url = "http://www.apache.org/licenses/LICENSE-2.0.html"))
	)
	public class TKIntegrationApplication extends Application {
	}
