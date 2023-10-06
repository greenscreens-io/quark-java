/*
 * Copyright (C) 2015, 2023 Green Screens Ltd.
 */
package io.greenscreens.quark.security;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.util.Objects;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import jakarta.enterprise.inject.Vetoed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.greenscreens.quark.utils.QuarkUtil;

/**
 * AEC encryption & Decryption utility
 */
@Vetoed
class AesCrypt implements IAesKey {

	private static final Logger LOG = LoggerFactory.getLogger(AesCrypt.class);

	private static final String TRANSFORMATION = "AES/CTR/NoPadding";
	
	/**
	 * Use SunJCE as it might support hardware AES-NI.
	 * BouncyCastle does not support it. - ~250.000 calc/sec
	 * Hardware AES-NI is about 10x faster - ~2 million calc/sec
	 */			
	private static final String PROVIDER = "SunJCE"; 
	//private static final String PROVIDER = SecurityProvider.PROVIDER_NAME;
	
	private Cipher cipher;

	private SecretKeySpec keyspec;
	private int size;

	public AesCrypt(final byte[] secretKey) throws IOException {
		setSecretKey(secretKey);
		initSize();
	}

	private void initSize() throws IOException {
		try {
			cipher = Cipher.getInstance(TRANSFORMATION, PROVIDER);
			size = cipher.getBlockSize();
		} catch (Exception e) {
			final String msg = QuarkUtil.toMessage(e);
			LOG.error(msg);
			LOG.debug(msg, e);
			throw new IOException(e);
		}
	}
	
	@Override
	public boolean isValid() {
		return size > 0 && Objects.nonNull(keyspec);
	}


	/**
	 * Set key used to encrypt data, length must be 32 bytes
	 *
	 * @param secretKey
	 * @throws IOException 
	 */
	@Override
	public void setSecretKey(final byte[] secretKey) throws IOException {
		if (Objects.isNull(secretKey) || secretKey.length != 32) {
			throw new IOException("Invalid AES key length");
		}
		keyspec = new SecretKeySpec(secretKey, "AES");
	}

	@Override
	public byte[] encrypt(final byte[] data, final byte[] iv) throws IOException {
		return encrypt(data, new IvParameterSpec(iv));
	}

	@Override
	public byte[] decrypt(final byte[] data, final byte[] iv) throws IOException {
		return decrypt(data, new IvParameterSpec(iv));
	}

	@Override
	public ByteBuffer encrypt(final ByteBuffer data, final byte[] iv) throws IOException {
		return encrypt(data, new IvParameterSpec(iv));
	}

	@Override
	public ByteBuffer decrypt(final ByteBuffer data, final byte[] iv) throws IOException {
		return decrypt(data, new IvParameterSpec(iv));
	}
	
	@Override
	public byte[] encrypt(final byte[] data, final IvParameterSpec ivSpec) throws IOException {

		try {
			cipher.init(Cipher.ENCRYPT_MODE, keyspec, ivSpec);
			return cipher.doFinal(data);
		} catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException | InvalidAlgorithmParameterException e) {
			throw new IOException(e);
		}

	}
	
	@Override
	public byte[] decrypt(final byte[] data, final IvParameterSpec ivSpec) throws IOException {
		try {
			cipher.init(Cipher.DECRYPT_MODE, keyspec, ivSpec);
			return cipher.doFinal(data);
		} catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException | InvalidAlgorithmParameterException e) {
			throw new IOException(e);
		}
	}

	@Override
	public ByteBuffer encrypt(final ByteBuffer data, final IvParameterSpec ivSpec) throws IOException {
		final ByteBuffer result = ByteBuffer.allocate(data.limit());
		try {
			cipher.init(Cipher.ENCRYPT_MODE, keyspec, ivSpec);
			cipher.doFinal(data, result);
		} catch (IllegalBlockSizeException | InvalidKeyException | InvalidAlgorithmParameterException | ShortBufferException | BadPaddingException  e) {
			throw new IOException(e);
		}
		return result;
	}

	@Override
	public ByteBuffer decrypt(final ByteBuffer data, final IvParameterSpec ivSpec) throws IOException {
		final ByteBuffer result = ByteBuffer.allocate(data.limit());
		try {
			cipher.init(Cipher.DECRYPT_MODE, keyspec, ivSpec);
			cipher.doFinal(data, result);
			result.rewind();
		} catch (IllegalBlockSizeException | InvalidKeyException | InvalidAlgorithmParameterException | ShortBufferException | BadPaddingException  e) {
			throw new IOException(e);
		}
		return result;
	}
	
	@Override
	public int getBlockSize() {
		return size;
	}

	@Override
	public Cipher getCipher() {
		return cipher;
	}

}
