package org.acme.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.constraints.NotBlank;

import org.acme.dto.MultipartBodyDTO;
import org.acme.exceptions.FileRetrievingException;
import org.acme.exceptions.InvalidFileStoringException;
import org.acme.exceptions.ProcessIdNotFoundException;
import org.acme.restclient.AuthRestClient;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import io.reactivex.annotations.NonNull;

@Singleton
public class MultipartService {

	@Inject
    @RestClient
    AuthRestClient authService;
		
	@ConfigProperty(name = "cv-store-folder")
	String cvStoreFolder;
	
	static final Logger LOG = Logger.getLogger(MultipartService.class);
	
	public String storeFile(@NonNull MultipartBodyDTO data)  {
		String pid= UUID.randomUUID().toString();
		
		StringBuilder fileNameSB=new StringBuilder(pid).append("_").append(data.getFileName());
	
		Path filePath = Path.of(cvStoreFolder, fileNameSB.toString());
			
		try {
			Files.write(filePath,data.getFile().readAllBytes(),StandardOpenOption.CREATE_NEW);
		} catch (IOException e) {
			throw new InvalidFileStoringException("Could not write in: "+filePath.toAbsolutePath().toString()+" "+e.getCause(),e);
		}
		
		LOG.info("File "+filePath.getFileName()+" stored in: "+filePath.toAbsolutePath().getParent().toString());
		
		return pid;
	}

	/**
	 * filter files from storing folder by using unique process id
	 * 
	 * @param processId
	 * @return
	 */
	public InputStream getFileInputStream(@NotBlank String processId){
		StringBuilder startingFileNameSB=new StringBuilder(processId).append("_");
		
		List<Path> fileNames;
		
		try (Stream<Path> files = Files.list(Paths.get(cvStoreFolder))){
			fileNames = files.parallel().filter(path -> 
		    	path.getFileName().toString().startsWith(startingFileNameSB.toString())
		    ).collect(Collectors.toList());
		    
			return Files.newInputStream(fileNames.get(0));
		    
		}
		catch (IndexOutOfBoundsException e) {
			throw new ProcessIdNotFoundException("No file associated with process id: "+processId);
		} 
		catch (IOException e) {
			throw new FileRetrievingException(e.getMessage(),e);
		}

	}

}
