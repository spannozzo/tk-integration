package org.acme.dto;

import java.io.InputStream;

import javax.validation.constraints.NotBlank;
import javax.ws.rs.FormParam;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.jboss.resteasy.annotations.providers.multipart.PartType;

import io.reactivex.annotations.NonNull;

@Schema(name="Multipart body DTO", description="Contains file stream and name being uploaded")
public class MultipartBodyDTO {

	@NonNull
	@Schema(title = "File stream", required = true,type = SchemaType.STRING)
	@FormParam("file")
    @PartType(MediaType.APPLICATION_OCTET_STREAM)
    InputStream file;

	@NotBlank
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