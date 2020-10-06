package org.acme.config;

import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.acme.dto.MultipartTKRequestDTO;
import org.acme.util.EncryptPropertyUtil;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Singleton
public class DecryptedMPRequestDtoConfig {

	@ConfigProperty(name = "encrypted.tk.account")
	String account;
	
	@ConfigProperty(name = "encrypted.tk.user")
	String username;
	
	@ConfigProperty(name = "encrypted.tk.password")
	String password;
	
	@Inject
	EncryptPropertyUtil encryptionUtil;

	/**
	 * use encryption util lib for decrypting credentials passed as properties
	 * @param data
	 * @return
	 * @throws InvalidKeyException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws NumberFormatException
	 */
	public MultipartTKRequestDTO getDecryptedMultipartDTO(InputStream data) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException,NumberFormatException {
		
    	final String decryptedAccount=encryptionUtil.decrypt(account);
    	final String decryptedUsername=encryptionUtil.decrypt(username);
    	final String decryptedPassword=encryptionUtil.decrypt(password);
    	
    	return new MultipartTKRequestDTO(data, decryptedAccount, decryptedUsername, decryptedPassword);
		
	}
	
	
}
