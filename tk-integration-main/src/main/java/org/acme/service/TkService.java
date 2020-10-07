package org.acme.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.annotation.PostConstruct;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.acme.dto.MultipartTKRequestDTO;
import org.acme.dto.ProfileDTO;
import org.acme.exceptions.TKResponseException;
import org.acme.exceptions.TKUnparsableResponse;
import org.acme.util.EncryptPropertyUtil;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.security.jpa.Password;

@Singleton
public class TkService {

	CloseableHttpClient client;

	
	@ConfigProperty(name = "tk.service.url")
	@Password
	String serviceUrl;

	
	JAXBContext context;

	Unmarshaller unmarshaller;
	
	@Inject
	EncryptPropertyUtil encryptionUtil;

	@PostConstruct
	void init() throws JAXBException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {

		context = JAXBContext.newInstance(ProfileDTO.class);
		unmarshaller = context.createUnmarshaller();
		
		serviceUrl=encryptionUtil.decrypt(serviceUrl);
		
	}

	
	public ProfileDTO extract(MultipartTKRequestDTO multipartTKRequestDTO, String fileName) {
		
		
		HttpPost httpPost = initPostCall(multipartTKRequestDTO, fileName);

		InputStream resultIS = getPostResult(httpPost);

		ProfileDTO xmlResult = getXMLFromResult(resultIS);

		return xmlResult;
	}

	private HttpPost initPostCall(MultipartTKRequestDTO multipartTKRequestDTO, String fileName) {
		client = HttpClients.createDefault();

		HttpEntity entity = MultipartEntityBuilder.create().addTextBody("account", multipartTKRequestDTO.getAccount())
				.addTextBody("username", multipartTKRequestDTO.getUsername())
				.addTextBody("password", multipartTKRequestDTO.getPassword()).addBinaryBody("uploaded_file",
						multipartTKRequestDTO.getFile(), ContentType.create("application/octet-stream"), fileName)
				.build();

		HttpPost httpPost = new HttpPost(serviceUrl);
		httpPost.setEntity(entity);
		return httpPost;
	}

	/**
	 * Takes the content as inputstream and close the connection
	 * 
	 * 
	 * @param httpPost
	 * @return
	 */
	private InputStream getPostResult(HttpPost httpPost) {
		CloseableHttpResponse response = null;
		InputStream resultIS = InputStream.nullInputStream();
		try {
			response = client.execute(httpPost);
			HttpEntity result = response.getEntity();

			resultIS = result.getContent();

			// I need to close the response call, but it will close me also the inputstream
			// that I want to return, so I need to create a copy of the inputstream

			resultIS = cloneIS(resultIS);

		} catch (IOException e) {
			throw new TKResponseException(e.getMessage(), e);
		} finally {
			try {
				if (response != null) {
					response.close();
				}
				client.close();
			} catch (IOException ignored) {

			}
		}

		return resultIS;

	}

	private InputStream cloneIS(InputStream resultIS) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		resultIS.transferTo(baos);
		resultIS = new ByteArrayInputStream(baos.toByteArray());
		return resultIS;
	}

	/**
	 * for some reason passing directly the input stream to the unmarshaller give
	 * unexpected exception, so the xml content need to be passed trough a string
	 * reader
	 * 
	 * @param resultIS
	 * @return
	 */
	private ProfileDTO getXMLFromResult(InputStream resultIS) {
		ProfileDTO xmlResult = new ProfileDTO();
		String xmlContent="";
		try {
			xmlContent = new String(resultIS.readAllBytes(), StandardCharsets.UTF_8);
			
			StringReader sr = new StringReader(xmlContent);

			xmlResult = (ProfileDTO) unmarshaller.unmarshal(sr);
		} catch (JAXBException | IOException e) {
			
			throw new TKUnparsableResponse(xmlContent, e);
		} finally {
			try {
				resultIS.close();
			} catch (IOException ignored) {
				
			}
		}
		return xmlResult;
	}
}
