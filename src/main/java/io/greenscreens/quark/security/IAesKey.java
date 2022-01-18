/*
 * Copyright (C) 2015, 2022 Green Screens Ltd.
 */
package io.greenscreens.quark.security;

import java.io.IOException;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;

public interface IAesKey {

	/**
	 * If session stored key is valid
	 * @return
	 */
	boolean isValid();
	
	/**
	 * Set key used to encrypt data, length must be 16 bytes
	 *
	 * @param secretKey
	 * @throws IOException 
	 */
	void setSecretKey(byte[] secretKey) throws IOException;
	
	/**
	 * Set key used to encrypt data
	 * 
	 * @param secretKey
	 * @throws IOException 
	 */
	void setSecretKey(String secretKey) throws IOException;

	/**
	 * Set Initialization vector to encrypt data to prevent same hash for same
	 * passwords
	 * 
	 * @param iv
	 */
	void setIv(String iv);


	/**
	 * Set Initialization vector to encrypt data to prevent same hash for same
	 * passwords
	 * 
	 * @param iv
	 */
	void setIv(byte[] iv);
	
	/**
	 * Encrypt string and return raw byte's
	 * 
	 * @param text
	 * @return
	 * @throws Exception
	 */
	byte[] encryptData(String text) throws IOException;

	byte[] encryptData(String text, byte[] iv) throws IOException;

	byte[] encryptData(String text, IvParameterSpec iv) throws IOException;

	/**
	 * Decrypt hex encoded data to byte array
	 * 
	 * @param code
	 * @return
	 * @throws Exception
	 */
	byte[] decryptData(String code) throws IOException;

	byte[] decryptData(String code, String iv) throws IOException;

	byte[] decryptData(String code, byte[] iv) throws IOException;

	byte[] decryptData(String code, IvParameterSpec iv) throws IOException;

	byte[] decryptData(byte[] data, byte[] iv) throws IOException;

	byte[] decryptData(byte[] data, IvParameterSpec iv) throws IOException;

	/**
	 * Encrypts string to hex string
	 */
	String encrypt(String text) throws IOException;

	String encrypt(String text, byte[] iv) throws IOException;

	String encrypt(String text, IvParameterSpec iv) throws IOException;

	/**
	 * Decrypts hex string to string value
	 */
	String decrypt(String text) throws IOException;

	byte[] decrypt(byte[] data) throws IOException;

	byte[] encrypt(byte[] data) throws IOException;

	int getBlockSize();

	Cipher getCipher();

}