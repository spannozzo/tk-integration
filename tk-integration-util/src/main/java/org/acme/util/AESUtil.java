package org.acme.util;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.acme.exceptions.EncryptionCredentialException;

public class AESUtil {

	static final String KEY_ALGORITHM = "AES";
	static final String DEFAULT_CIPHER_ALGORITHM = "AES/GCM/NoPadding";// The default encryption algorithm

	private AESUtil() {
	}

	private static AESUtil instance;

	public static synchronized AESUtil getInstance() {
		if (instance == null) {
			instance= new AESUtil();
		}
		return instance;
	}

	public String encrypt(String content, String encryptPass) {

		try {
			byte[] iv = new byte[12];
			SecureRandom secureRandom = new SecureRandom();
			secureRandom.nextBytes(iv);
			byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);
			Cipher cipher = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);
			GCMParameterSpec params = new GCMParameterSpec(128, iv);
			
			cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(encryptPass), params);
			byte[] encryptData = cipher.doFinal(contentBytes);
			byte[] message = new byte[12 + contentBytes.length + 16];
			System.arraycopy(iv, 0, message, 0, 12);
			System.arraycopy(encryptData, 0, message, 12, encryptData.length);
			return Base64.getEncoder().encodeToString(message);
		} catch (	InvalidKeyException | InvalidAlgorithmParameterException | NoSuchAlgorithmException | 
					IllegalBlockSizeException | BadPaddingException | NoSuchPaddingException e) {
			
			throw new EncryptionCredentialException(e.getMessage(),e);
		}
	}

	public String decrypt(String base64Content, String encryptPass){

		try {
			byte[] content = Base64.getDecoder().decode(base64Content);

			GCMParameterSpec params = new GCMParameterSpec(128, content, 0, 12);
			Cipher cipher = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);
			cipher.init(Cipher.DECRYPT_MODE, getSecretKey(encryptPass), params);
			byte[] decryptData = cipher.doFinal(content, 12, content.length - 12);
			return new String(decryptData, StandardCharsets.UTF_8);
		} catch (	InvalidKeyException | InvalidAlgorithmParameterException | IllegalArgumentException |
					NoSuchAlgorithmException | NoSuchPaddingException | 
					IllegalBlockSizeException | BadPaddingException e) {
			
			throw new EncryptionCredentialException(e.getMessage(),e);
		}
	}

	/**
	 * Generate encryption key
	 *
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	private SecretKeySpec getSecretKey(String encryptPass) throws NoSuchAlgorithmException {
		KeyGenerator kg = KeyGenerator.getInstance(KEY_ALGORITHM);
		// Initialize the key generator, AES requires the key length to be 128 bits, 192
		// bits, 256 bits
		SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
		secureRandom.setSeed(encryptPass.getBytes());
		kg.init(128, secureRandom);
		SecretKey secretKey = kg.generateKey();
		return new SecretKeySpec(secretKey.getEncoded(), KEY_ALGORITHM);// Convert to AES private key
	}

}
