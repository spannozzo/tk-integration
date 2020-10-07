package org.acme.util;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.util.Base64;

import javax.annotation.PostConstruct;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.enterprise.context.ApplicationScoped;
import javax.xml.bind.DatatypeConverter;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class EncryptPropertyUtil {
	static final String ALGORITHM_TYPE = "AES/GCM/NoPadding";
	static final String PROVIDER = "BC";

	@PostConstruct
	public void init() throws NoSuchAlgorithmException, NoSuchProviderException,
			NoSuchPaddingException {
		Security.addProvider(new BouncyCastleProvider());

		cipher = Cipher.getInstance(ALGORITHM_TYPE, PROVIDER);

	}

	@ConfigProperty(name = "acme.jwt.enctypted-key")
	String encodedKey;

	@ConfigProperty(name = "acme.jwt.password.keylength", defaultValue = "128")
	Integer keylength;

	Cipher cipher;

	public Cipher getCipher() {
		return cipher;
	}

	byte[] ivBytes = new byte[] { 0x00, 0x00, 0x00, 0x01, 0x04, 0x05, 0x06, 0x07, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x00, 0x01 };

	String getSecretKey()
			throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException {

		cipher = Cipher.getInstance(ALGORITHM_TYPE, PROVIDER);
		KeyGenerator keyGenerator = KeyGenerator.getInstance("AES", PROVIDER);

		keyGenerator.init((keylength));

		SecretKey key=keyGenerator.generateKey(); 
				
		
		return DatatypeConverter.printHexBinary(key.getEncoded());
	}

	static EncryptPropertyUtil instance;

	public String encrypt(String value,String key)
			throws InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException,
			BadPaddingException {

		
		SecretKeySpec sks = new SecretKeySpec(DatatypeConverter.parseHexBinary(key), "AES");

	    cipher.init(Cipher.ENCRYPT_MODE, sks, new IvParameterSpec(ivBytes));
	   
	    byte[] encrypted = cipher.doFinal(value.getBytes());
	   
		return Base64.getEncoder().encodeToString(encrypted);

	}

	public String encrypt(String value)
			throws InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException,
			BadPaddingException {
		
		return encrypt(value, encodedKey);
	}
	
	public String decrypt(String encryptedValue,String key)
			throws NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException,
			InvalidKeyException, NoSuchProviderException, InvalidAlgorithmParameterException {

		SecretKeySpec sks = new SecretKeySpec(DatatypeConverter.parseHexBinary(key), "AES");
		
	    cipher.init(Cipher.DECRYPT_MODE, sks, new IvParameterSpec(ivBytes));
	    
		byte[] encryptedBytes = Base64.getDecoder().decode(encryptedValue);
		byte[] decrypted = cipher.doFinal(encryptedBytes);
		
		return new String(decrypted);
	   
	}
	
	public String decrypt(String encryptedValue)
			throws NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException,
			InvalidKeyException, NoSuchProviderException, InvalidAlgorithmParameterException {
		
		return decrypt(encryptedValue, encodedKey);
	}
	

}
