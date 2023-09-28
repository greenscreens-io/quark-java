/*
 * Copyright (C) 2015, 2023 Green Screens Ltd.
 */
package io.greenscreens.quark;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * AES encryption key used to encrypt/decrypt web requests
 */
public interface IQuarkKey {

	byte[] encrypt(final byte[] data, final byte[] iv) throws IOException;
	byte[] decrypt(final byte[] data, final byte[] iv) throws IOException;

	ByteBuffer encrypt(final ByteBuffer data, final byte[] iv) throws IOException;
	ByteBuffer decrypt(final ByteBuffer data, final byte[] iv) throws IOException;
}
