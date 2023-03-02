/*
 * Copyright (C) 2015, 2022 Green Screens Ltd.
 */
package io.greenscreens.quark.security;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Objects;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.greenscreens.quark.QuarkUtil;

/**
 * Helper class for handling encryption One can use
 * http://travistidwell.com/jsencrypt/demo/ to generate keys.
 * 
 * Must be initialized with ICrypt module. Create class that implements ICrypt
 * and implement encryption / decryption code.
 * 
 */
public enum Security {
	;

	private static final Logger LOG = LoggerFactory.getLogger(Security.class);

	private static final Charset ASCII = StandardCharsets.US_ASCII;
	private static final Charset UTF8 = StandardCharsets.UTF_8;	

	private static byte RMTCH = 1;
	private static final SecureRandom SECURE_RANDOM;
	
	static {
		SECURE_RANDOM = getSecureRandom();	
	}
	
	private static SecureRandom getSecureRandom() {
		try {
			return SecureRandom.getInstanceStrong();
		} catch (NoSuchAlgorithmException e) {
			return new SecureRandom();
		}
	}
	
	/**
	 * Get initialization vector for AES
	 * 
	 * @param value
	 * @return
	 */
	public static IvParameterSpec getIV(final String value) {
		final String aesIV = value.substring(0, 16);
		return new IvParameterSpec(aesIV.getBytes(ASCII));
	}
	
	/**
	 * Get random byte from prng generator
	 * 
	 * @param size
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws Exception
	 */

	public static void nextBytes(byte[] bytes) {
		SECURE_RANDOM.nextBytes(bytes);
	}
	
	public static byte[] getRandom(final int size) {
		byte[] bytes = new byte[size];
		try {
			SECURE_RANDOM.nextBytes(bytes);
			if (bytes[0] == RMTCH) {
				RMTCH = bytes[1];
				SECURE_RANDOM.setSeed(bytes);
			}
		} catch (Exception e) {
			final String msg = QuarkUtil.toMessage(e);
			LOG.error(msg);
			LOG.debug(msg, e);
		}
		return bytes;
	}
	
	/**
	 * Init aes encryption from url encrypted request
	 * 
	 * @param k
	 * @return
	 * @throws IOException 
	 */
	public static IAesKey initAESURL(final String k) throws IOException {

		final boolean isHex = QuarkUtil.isHex(k);
		
		final byte[] aesData = SharedSecret.generate(k, AsyncKey.getPrivateKey(), isHex);

		if (Objects.isNull(aesData) || aesData.length < 32) {
			throw new IOException("Ivalid AES encryption key!");
		}

		final byte[] aesKey = Arrays.copyOfRange(aesData, 0, 16);
		final byte[] aesIV = Arrays.copyOfRange(aesData, 16, 32);
		return new AesCrypt(aesKey, aesIV);
	}

	public static IAesKey initWebKey(final String publicKey) {
		if (QuarkUtil.nonEmpty(publicKey)) {
			try {
				return Security.initAESURL(publicKey);
			} catch (IOException e) {
				final String msg = QuarkUtil.toMessage(e);
				LOG.error(msg);
				LOG.debug(msg, e);
			}			
		} 
		return null;
	}
	
	/**
	 * Init AES from password
	 * 
	 * @param secretKey
	 * @return
	 * @throws IOException 
	 */
	public static IAesKey initAES(final String secretKey) throws IOException {
		return new AesCrypt(secretKey);
	}
	
	public static IAesKey initAES(final byte [] secretKey) throws IOException {
		return new AesCrypt(secretKey);
	}
	
	/**
	 * Generate new Async key
	 * 
	 * @throws Exception
	 */
	public static void generateAsyncKeys() {
		AsyncKey.initialize();
	}

	/**
	 * Load RSA keys from PEM format
	 * 
	 * @param publicKey
	 * @param privateKey
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeySpecException 
	 * @throws NoSuchProviderException 
	 * @throws Exception
	 */
	public static void setAsyncKeys(final String publicKey, final String privateKey) throws InvalidKeySpecException, NoSuchAlgorithmException, NoSuchProviderException {
		final PublicKey pubKey = AsyncKeyUtil.getPublicKey(publicKey);
		final PrivateKey privKey = AsyncKeyUtil.getPrivateKey(privateKey);
		AsyncKey.setKeys(pubKey, privKey);
	}

	/**
	 * Get active RSA public key in PEM format
	 * 
	 * @return
	 */
	public static String getPublicKey() {
		return AsyncKey.getPublicEncoder(true);
	}

	public static String getVerifier() {
		return AsyncKey.getPublicVerifier(true);
	}

	/**
	 * Get active RSA private key in PEM format
	 * 
	 * @return
	 */
	public static String getPrivateKey() {
		return AsyncKey.getPrivateEncoder(true);
	}

	/**
	 * Sign data with RSA key
	 * 
	 * @param data
	 * @return
	 */
	public static String sign(final String data, final boolean isHex) {

		String msg = null;

		try {
			msg = AsyncKey.sign(data, isHex);
		} catch (Exception e) {
			final String msge = QuarkUtil.toMessage(e);
			LOG.error(msge);
			LOG.debug(msge, e);
		}

		return msg;
	}

	/**
	 * Sign data with RSA key
	 * 
	 * @param data
	 * @return
	 */
	public static String signChallenge(final String data, final boolean isHex) {

		String msg = null;

		try {
			msg = AsyncKey.signChallenge(data, isHex);
		} catch (Exception e) {
			final String msge = QuarkUtil.toMessage(e);
			LOG.error(msge);
			LOG.debug(msge, e);
		}

		return msg;
	}

	/**
	 * Decode url encrypted request
	 * 
	 * @param d     - data encrypted with AES
	 * @param k     - AES IV encrypted with RSA, used to decrypt d
	 * @param crypt
	 * @return
	 * @throws Exception
	 */
	public static String decryptRequest(final String d, final String k, final IAesKey crypt) throws IOException {
		final byte[] raw = convert(d);
		final byte[] iv = convert(k);
		final byte[] decoded = crypt.decryptData(raw, iv);
		return new String(decoded, UTF8);		
	}

	public static byte [] convert(final String data) {
		return QuarkUtil.hexStringToByteArray(data);
	} 
			
	public static byte [] decrypt(final byte [] data, final SecretKeySpec secret, final String algo) {
		byte [] value = null;
		try {
			final Cipher cipher = Cipher.getInstance(algo);
			cipher.init(Cipher.DECRYPT_MODE, secret);			
			value = cipher.doFinal(data);
		} catch (Exception e) {
			final String msg = QuarkUtil.toMessage(e);
			LOG.error(msg);
			LOG.debug(msg, e);
		}
		return value;
	}

	/**
	 * Digest data for required algorithm 
	 * @param type
	 * @param data
	 * @return
	 */
	public static byte[] digest(final String type, final byte[] data) {

		byte[] result = null;
		try {
			final MessageDigest md = getDigest(type);
			result = md.digest(data);
		} catch (NoSuchAlgorithmException e) {
			result = new byte[0];
			final String msg = QuarkUtil.toMessage(e);
			LOG.error(msg);
			LOG.debug(msg, e);
		}
		
		return result;
	}
	
	public static byte[] digest(final String type, final InputStream inputStream) throws IOException {
		byte[] result = null;
		try {
			final MessageDigest md = getDigest(type);
			result = digest(md, inputStream).digest();
		} catch (NoSuchAlgorithmException e) {
			result = new byte[0];
			final String msg = QuarkUtil.toMessage(e);
			LOG.error(msg);
			LOG.debug(msg, e);
		}
		
		return result;
	}
	
    public static MessageDigest digest(final MessageDigest digest, final InputStream inputStream) throws IOException {
    	 	final int bufferLength = 1024;
            final byte[] buffer = new byte[bufferLength];
            int read = inputStream.read(buffer, 0, bufferLength);

            while (read > -1) {
                digest.update(buffer, 0, read);
                read = inputStream.read(buffer, 0, bufferLength);
            }

            return digest;
        }

    public static MessageDigest getDigest(final String type) throws NoSuchAlgorithmException {
    	return MessageDigest.getInstance(QuarkUtil.normalize(type));
    }

}
