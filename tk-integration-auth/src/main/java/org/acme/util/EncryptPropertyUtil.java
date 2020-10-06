package org.acme.util;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.config.inject.ConfigProperty;


@ApplicationScoped
public class EncryptPropertyUtil {
	static final String ALGORITHM_TYPE = "AES";
	
	@ConfigProperty(name = "acme.jwt.password.keylength",defaultValue = "128")
	Integer keylength;
	
	
	@ConfigProperty(name = "acme.jwt.enctypted-key")
	String encodedKey;
	
	SecretKey secretKey;

	static EncryptPropertyUtil instance;

	static String byteArrayToHexString(byte[] b) {
	    StringBuffer sb = new StringBuffer(b.length * 2);
	    for (int i = 0; i < b.length; i++) {
	        int v = b[i] & 0xff;
	        if (v < 16) {
	            sb.append('0');
	        }
	        sb.append(Integer.toHexString(v));
	    }
	    return sb.toString().toUpperCase();
	}

	static byte[] hexStringToByteArray(String s) {
	    byte[] b = new byte[s.length() / 2];
	    for (int i = 0; i < b.length; i++) {
	        int index = i * 2;
	        int v = Integer.parseInt(s.substring(index, index + 2), 16);
	        b[i] = (byte) v;
	    }
	    return b;
	}

	public String decrypt(String encryptedValue) throws NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
		SecretKeySpec sks = new SecretKeySpec(hexStringToByteArray(encodedKey), EncryptPropertyUtil.ALGORITHM_TYPE);
	    Cipher cipher = Cipher.getInstance(EncryptPropertyUtil.ALGORITHM_TYPE);
	    
	    cipher.init(Cipher.DECRYPT_MODE, sks);
	    byte[] decrypted = cipher.doFinal(hexStringToByteArray(encryptedValue));
	    return new String(decrypted);
	}

	public String encrypt(String value) throws InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException {
		
				
		SecretKeySpec sks = new SecretKeySpec(hexStringToByteArray(encodedKey), EncryptPropertyUtil.ALGORITHM_TYPE);
	    Cipher cipher = Cipher.getInstance(EncryptPropertyUtil.ALGORITHM_TYPE);
	    cipher.init(Cipher.ENCRYPT_MODE, sks, cipher.getParameters());
	    byte[] encrypted = cipher.doFinal(value.getBytes());
	    
	    return byteArrayToHexString(encrypted);

	}

	
}
