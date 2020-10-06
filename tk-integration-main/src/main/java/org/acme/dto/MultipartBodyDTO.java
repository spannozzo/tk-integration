package org.acme.dto;

import java.io.InputStream;

import javax.ws.rs.FormParam;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.jboss.resteasy.annotations.providers.multipart.PartType;

/**
 * IMPORTANT - leave without a constructor
 * @author salva
 *
 */
@Schema(name="Multipart body DTO", description="Contains file stream and name being uploaded")
public class MultipartBodyDTO  {
	@Schema(title = "File stream", required = true,type = SchemaType.STRING)
	@FormParam("file")
    @PartType(MediaType.APPLICATION_OCTET_STREAM)
    InputStream file;

	@Schema(title = "File name", required = true,format = "binary")
    @FormParam("fileName")
    @PartType(MediaType.TEXT_PLAIN)
    String fileName;

	public InputStream getFile() {
		return file;
	}

	public void setFile(InputStream file) {
		this.file = file;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}   
	
    
}