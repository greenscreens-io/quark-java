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
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.util.Collections;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.enterprise.inject.Vetoed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.greenscreens.quark.IQuarkKey;
import io.greenscreens.quark.QuarkUtil;


/**
 * AEC encryption & Decryption utility
 */
@Vetoed
class AesCrypt implements IQuarkKey {

	private static final Logger LOG = LoggerFactory.getLogger(AesCrypt.class);

	private static final Charset ASCII = StandardCharsets.US_ASCII;
	private static final Charset UTF8 = StandardCharsets.UTF_8;
	private static final String TRANSFORMATION = "AES/CTR/NoPadding";
	
	private Cipher cipher;

	private IvParameterSpec ivspec;
	private SecretKeySpec keyspec;
	private int size;
	
	public AesCrypt(final String secretKey, final String vector) throws IOException {
		init(secretKey, vector);
	}
		
	public AesCrypt(final String secretKey) throws IOException {
		init(secretKey, secretKey);
	}

	public AesCrypt(final byte[] secretKey) throws IOException {
		init(secretKey, secretKey);
	}

	public AesCrypt(final byte[] secretKey, final byte[] vector) throws IOException {
		init(secretKey, vector);
	}
	
	void init(final String secretKey, final String vector) throws IOException {
		setSecretKey(secretKey);
		setIv(vector);
		initSize();
	}

	void init(final byte[] secretKey, final byte[] vector) throws IOException {
		setSecretKey(secretKey);
		setIv(vector);
		initSize();
	}

	private void initSize() {
		try {
			cipher = Cipher.getInstance(TRANSFORMATION);
			size = cipher.getBlockSize();
		} catch (Exception e) {
			final String msg = QuarkUtil.toMessage(e);
			LOG.error(msg);
			LOG.debug(msg, e);
		}
	}
	
	@Override
	public boolean isValid() {
		return size > 0 && keyspec != null && ivspec != null;
	}

	/**
	 * Set key used to encrypt data
	 * 
	 * @param secretKey
	 * @throws IOException 
	 */
	@Override
	public void setSecretKey(final String secretKey) throws IOException {
		if (secretKey == null || secretKey.length() != 16) {
			throw new IOException("Invalid AES key length");
		}
		keyspec = new SecretKeySpec(secretKey.getBytes(ASCII), "AES");
	}

	/**
	 * Set key used to encrypt data, length must be 16 bytes
	 *
	 * @param secretKey
	 * @throws IOException 
	 */
	@Override
	public void setSecretKey(final byte[] secretKey) throws IOException {
		if (secretKey == null || secretKey.length != 16) {
			throw new IOException("Invalid AES key length");
		}
		keyspec = new SecretKeySpec(secretKey, "AES");
	}
	
	/**
	 * Set Initialization vector to encrypt data to prevent same hash for same
	 * passwords
	 * 
	 * @param iv
	 */
	@Override
	public void setIv(final String iv) {
		ivspec = new IvParameterSpec(iv.getBytes(ASCII));
	}

	/**
	 * Set Initialization vector to encrypt data to prevent same hash for same
	 * passwords
	 * 
	 * @param iv
	 */
	@Override
	public void setIv(final byte[] iv) {
		ivspec = new IvParameterSpec(iv);
	}

	/**
	 * Encrypt string and return raw byte's
	 * 
	 * @param text
	 * @return
	 * @throws Exception
	 */
	@Override
	public byte[] encryptData(final String text) throws IOException {
		return encryptData(text, ivspec);
	}

	@Override
	public byte[] encryptData(final String text, final byte[] iv) throws IOException {
		return encryptData(text, new IvParameterSpec(iv));
	}

	@Override
	public byte[] encryptData(final String text, final IvParameterSpec iv) throws IOException {

		if (text == null || text.length() == 0) {
			throw new IOException("Empty string");
		}

		try {
			cipher.init(Cipher.ENCRYPT_MODE, keyspec, iv);
			return cipher.doFinal(padString(text).getBytes(UTF8));
		} catch (InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
			throw new IOException(e);
		}
	}

	/**
	 * Decrypt hex encoded data to byte array
	 * 
	 * @param code
	 * @return
	 * @throws Exception
	 */
	@Override
	public byte[] decryptData(final String code) throws IOException {
		return decryptData(code, ivspec);
	}

	@Override
	public byte[] decryptData(final String code, final String iv) throws IOException {
		return decryptData(code, iv.getBytes());
	}

	@Override
	public byte[] decryptData(final String code, final byte[] iv) throws IOException {
		return decryptData(code, new IvParameterSpec(iv));
	}

	@Override
	public byte[] decryptData(final String code, final IvParameterSpec iv) throws IOException {

		if (code == null || code.length() == 0) {
			throw new IOException("Empty string");
		}

		try {
			cipher.init(Cipher.DECRYPT_MODE, keyspec, iv);
			return cipher.doFinal(QuarkUtil.hexStringToByteArray(code));
		} catch (InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
			throw new IOException(e);
		}
		
	}

	@Override
	public byte[] decryptData(final byte[] data, final byte[] iv) throws IOException {
		return decryptData(data, new IvParameterSpec(iv));
	}

	@Override
	public byte[] decryptData(final byte[] data, final IvParameterSpec iv) throws IOException {

		try {
			cipher.init(Cipher.DECRYPT_MODE, keyspec, iv);
			return cipher.doFinal(data);
		} catch (InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
			throw new IOException(e);
		}
	}

	/**
	 * Encrypts string to hex string
	 */
	@Override
	public String encrypt(final String text) throws IOException {
		return QuarkUtil.bytesToHex(encryptData(text));
	}

	@Override
	public String encrypt(final String text, final byte[] iv) throws IOException {
		return QuarkUtil.bytesToHex(encryptData(text, new IvParameterSpec(iv)));
	}

	@Override
	public String encrypt(final String text, IvParameterSpec iv) throws IOException {
		return QuarkUtil.bytesToHex(encryptData(text, iv));
	}

	/**
	 * Decrypts hex string to string value
	 */
	@Override
	public String decrypt(final String text) throws IOException {
		return new String(decryptData(text), UTF8);
	}

	/**
	 * Blank padding for AES algorithm
	 * 
	 * @param source
	 * @return
	 */
	private String padString(final String source) {
		return QuarkUtil.padString(source, 16);
	}

	@Override
	public byte[] decrypt(final byte[] data) throws IOException {
		return decryptData(data, ivspec);
	}

	@Override
	public byte[] encrypt(final byte[] data) throws IOException {

		try {
			cipher.init(Cipher.ENCRYPT_MODE, keyspec, ivspec);
			return cipher.doFinal(data);
		} catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException | InvalidAlgorithmParameterException e) {
			throw new IOException(e);
		}

	}

	@Override
	public int getBlockSize() {
		return size;
	}

	@Override
	public Cipher getCipher() {
		return cipher;
	}

	static final void pro() {
		Collections.emptyList().stream().filter(s -> s.equals("")).findFirst();
	}

}
