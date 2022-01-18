/*
 * Copyright (C) 2015, 2022 Green Screens Ltd.
 */
package io.greenscreens.quark.security;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.MGF1ParameterSpec;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource.PSpecified;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.greenscreens.quark.QuarkUtil;

/**
 * Helper class for handling RSA keys Support for Web Crypto API
 */
enum RsaCrypt {
	;

	private static final Logger LOG = LoggerFactory.getLogger(RsaCrypt.class);

	private static final String LEGACY_MODE = "RSA/ECB/PKCS1Padding";
	private static final String WEB_MODE = "RSA/NONE/OAEPWithSHA256AndMGF1Padding";
	private static final String WEB_MODE_JCA = "RSA/NONE/OAEPWithSHA-256AndMGF1Padding";
	private static final OAEPParameterSpec oaepParams = new OAEPParameterSpec("SHA-256", "MGF1", new MGF1ParameterSpec("SHA-256"), PSpecified.DEFAULT);

	/**
	 * Encrypt string with given RSA key
	 * 
	 * @param Buffer
	 * @param key
	 * @return
	 */
	public static String encrypt(final String data, final PublicKey key, final boolean isHex,
			final boolean webCryptoApi) {
		if (isHex) {
			return encryptHex(data, key, webCryptoApi);
		} else {
			return encryptBase64(data, key, webCryptoApi);
		}
	}

	/**
	 * Encrypt string with given RSA key into Base64 String format
	 * 
	 * @param data
	 * @param key
	 * @param webCryptoApi
	 * @return
	 */
	public static String encryptBase64(final String data, final PublicKey key, final boolean webCryptoApi) {
		final byte[] enc = encrypt(data.getBytes(), key, webCryptoApi);
		final Encoder base64 = Base64.getEncoder();
		return base64.encodeToString(enc);
	}

	/**
	 * Encrypt string with given RSA key into Hex String format
	 * 
	 * @param data
	 * @param key
	 * @param webCryptoApi
	 * @return
	 */
	public static String encryptHex(final String data, final PublicKey key, final boolean webCryptoApi) {
		final byte[] enc = encrypt(data.getBytes(), key, webCryptoApi);
		return QuarkUtil.bytesToHex(enc);
	}

	/**
	 * Decode from base64 string
	 * 
	 * @param data
	 * @param key
	 * @return
	 */
	public static byte[] decrypt(final String data, final PrivateKey key, final boolean isHex,
			final boolean webCryptoApi) {
		if (isHex) {
			return decryptHex(data, key, webCryptoApi);
		} else {
			return decryptBase64(data, key, webCryptoApi);
		}
	}

	/**
	 * Decode from base64 string
	 * 
	 * @param data
	 * @param key
	 * @return
	 */
	public static byte[] decryptBase64(final String data, final PrivateKey key, final boolean webCryptoApi) {
		final Decoder base64 = Base64.getDecoder();
		byte[] bin = base64.decode(data);
		return decrypt(bin, key, webCryptoApi);
	}

	/**
	 * Decode from base64 string
	 * 
	 * @param data
	 * @param key
	 * @return
	 */
	public static byte[] decryptHex(final String data, final PrivateKey key, final boolean webCryptoApi) {
		byte[] bin = QuarkUtil.hexStringToByteArray(data);
		return decrypt(bin, key, webCryptoApi);
	}
	
	/**
	 * Decrypt data with private key and given mode
	 * 
	 * @param buffer
	 * @param key
	 * @param mode
	 * @return
	 */
	public static byte[] decrypt(final byte[] buffer, final PrivateKey key, final boolean webCryptoApi) {

		byte[] data = null;

		try {

			final Cipher cipher = getCipher(webCryptoApi, key, Cipher.DECRYPT_MODE);
			data = cipher.doFinal(buffer);

		} catch (Exception e) {
			final String msg = QuarkUtil.toMessage(e);
			LOG.error(msg);
			LOG.debug(msg, e);
			data = new byte[0];
		}

		return data;
	}

	/**
	 * Encrypt data with public key
	 * 
	 * @param Buffer
	 * @param key
	 * @return
	 */
	public static byte[] encrypt(final byte[] data, final PublicKey key, boolean webCryptoApi) {

		byte[] result = null;

		try {

			final Cipher cipher = getCipher(webCryptoApi, key, Cipher.ENCRYPT_MODE);
			result = cipher.doFinal(data);

		} catch (Exception e) {
			result = new byte[0];
			final String msg = QuarkUtil.toMessage(e);
			LOG.error(msg);
			LOG.debug(msg, e);
		}

		return result;
	}

	private static Cipher getCipher(final boolean webCryptoApi, final Key key, final int mode) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException  {

		Cipher cipher = null;

		if (webCryptoApi) {
			try {
				cipher = Cipher.getInstance(WEB_MODE, BouncyCastleProvider.PROVIDER_NAME);
			} catch (Exception e) {
				final String msg = QuarkUtil.toMessage(e);
				LOG.error(msg);
				LOG.debug(msg, e);
				cipher = Cipher.getInstance(WEB_MODE_JCA);
			}
			cipher.init(mode, key, oaepParams);
		} else {
			cipher = Cipher.getInstance(LEGACY_MODE);
			cipher.init(mode, key);
		}

		return cipher;
	}

}
