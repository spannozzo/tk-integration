package org.acme.config;

import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Optional;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.inject.Singleton;

import org.acme.dto.MultipartTKRequestDTO;
import org.acme.util.AESUtil;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Singleton
public class DecryptedMPRequestDtoConfig {

	@ConfigProperty(name = "encrypted.tk.account")
	String account;
	
	@ConfigProperty(name = "encrypted.tk.user")
	String username;
	
	@ConfigProperty(name = "encrypted.tk.password")
	String password;
	
	@ConfigProperty(name = "acme.aes.password")
	String aesPassword;

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
	 * @throws InvalidAlgorithmParameterException 
	 * @throws NoSuchProviderException 
	 */
	public Optional<MultipartTKRequestDTO> getDecryptedMultipartDTO(InputStream data) {
		
    	final String decryptedAccount=AESUtil.getInstance().decrypt(account, aesPassword);
    	final String decryptedUsername=AESUtil.getInstance().decrypt(username, aesPassword);
    	final String decryptedPassword=AESUtil.getInstance().decrypt(password, aesPassword);
    	
    	return Optional.of(new MultipartTKRequestDTO(data, decryptedAccount, decryptedUsername, decryptedPassword));
		
	}
	
	
}
