/*
 * Copyright (C) 2015, 2023. Green Screens Ltd.
 */
package io.greenscreens.quark.security;

import java.io.IOException;
import java.nio.ByteBuffer;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;

interface IAesKey extends IQuarkKey {

	/**
	 * If session stored key is valid
	 * @return
	 */
	boolean isValid();
	
	/**
	 * Set key used to encrypt data, length must be 32 bytes
	 *
	 * @param secretKey
	 * @throws IOException 
	 */
	void setSecretKey(final byte[] secretKey) throws IOException;
	
	byte[] encrypt(final byte[] data, final byte[] iv) throws IOException;
	byte[] decrypt(final byte[] data, final byte[] iv) throws IOException;
	
	byte[] encrypt(final byte[] data, final IvParameterSpec iv) throws IOException;
	byte[] decrypt(final byte[] data, final IvParameterSpec iv) throws IOException;
	
	ByteBuffer encrypt(final ByteBuffer data, final byte[]  ivSpec) throws IOException;
	ByteBuffer decrypt(final ByteBuffer data, final byte[]  ivSpec) throws IOException;

	ByteBuffer encrypt(final ByteBuffer data, final IvParameterSpec ivSpec) throws IOException;
	ByteBuffer decrypt(final ByteBuffer data, final IvParameterSpec ivSpec) throws IOException;

	int getBlockSize();

	Cipher getCipher();
}