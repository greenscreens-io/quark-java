/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 * 
 * https://www.greenscreens.io
 * 
 */
package io.greenscreens.quark.security;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

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

	// timeout value from config file
	private static long time;

	/**
	 * Get password timeout value in seconds
	 * 
	 * @return
	 */
	public static long getTime() {
		return time;
	}

	/**
	 * Get password timeout value in milliseconds
	 * 
	 * @return
	 */
	public static long getTimeMilis() {
		return time * 1000;
	}

	/**
	 * Set password timeout value in seconds
	 * 
	 * @param time
	 */
	public static void setTime(final long time) {
		Security.time = time;
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

	public static byte[] getRandom(final int size) {
		byte[] bytes = new byte[size];
		try {
			SecureRandom.getInstanceStrong().nextBytes(bytes);
		} catch (NoSuchAlgorithmException e) {
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
	public static IAesKey initAES(final String k, final boolean webCryptoAPI) throws IOException {

		IAesKey aes = null;
		final boolean isHex = QuarkUtil.isHex(k);
		
		final byte[] aesData = RsaCrypt.decrypt(k, RsaKey.getPrivateKey(), isHex, webCryptoAPI);

		if (aesData != null) {

			final byte[] aesIV = Arrays.copyOfRange(aesData, 0, 16);
			final byte[] aesKey = Arrays.copyOfRange(aesData, 16, 32);

			aes = new AesCrypt(aesKey, aesIV);

		}

		return aes;
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
	 * Generate new RSA key
	 * 
	 * @throws Exception
	 */
	public static void generateRSAKeys() {
		RsaKey.initialize();
	}

	/**
	 * Load RSA keys from PEM format
	 * 
	 * @param publicKey
	 * @param privateKey
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeySpecException 
	 * @throws Exception
	 */
	public static void setRSAKeys(final String publicKey, final String privateKey) throws InvalidKeySpecException, NoSuchAlgorithmException {
		final PublicKey pubKey = RsaUtil.getPublicKey(publicKey);
		final PrivateKey privKey = RsaUtil.getPrivateKey(privateKey);
		RsaKey.setKeys(pubKey, privKey);
	}

	/**
	 * Get active RSA public key in PEM format
	 * 
	 * @return
	 */
	public static String getRSAPublic(final boolean webCryptoAPI) {
		return RsaKey.getPublicEncoder(webCryptoAPI);
	}

	public static String getRSAVerifier(final boolean webCryptoAPI) {
		return RsaKey.getPublicVerifier(webCryptoAPI);
	}

	/**
	 * Get active RSA private key in PEM format
	 * 
	 * @return
	 */
	public static String getRSAPrivate(final boolean webCryptoAPI) {
		return RsaKey.getPrivateEncoder(webCryptoAPI);
	}

	/**
	 * Sign data with RSA key
	 * 
	 * @param data
	 * @return
	 */
	public static String sign(final String data, final boolean isHex, final boolean webCryptoAPI) {

		String msg = null;

		try {
			msg = RsaKey.sign(data, isHex, webCryptoAPI);
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
	public static String signChallenge(final String data, final boolean isHex, final boolean webCryptoAPI) {

		String msg = null;

		try {
			msg = RsaKey.signChallenge(data, isHex, webCryptoAPI);
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
	public static String decodeRequest(final String d, final String k, final IAesKey crypt, final boolean webCryptoAPI) throws IOException {

		final byte[] aesData = RsaCrypt.decrypt(k, RsaKey.getPrivateKey(), webCryptoAPI, webCryptoAPI);

		if (aesData != null) {
			final byte[] aesIV = Arrays.copyOfRange(aesData, 0, 16);
			final byte[] decoded = crypt.decryptData(d, aesIV);
			return new String(decoded, UTF8);
		}
		
		return null;
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
			final MessageDigest md = MessageDigest.getInstance(QuarkUtil.normalize(type));
			result = md.digest(data);
		} catch (NoSuchAlgorithmException e) {
			result = new byte[0];
			final String msg = QuarkUtil.toMessage(e);
			LOG.error(msg);
			LOG.debug(msg, e);
		}
		
		return result;
	}

}
