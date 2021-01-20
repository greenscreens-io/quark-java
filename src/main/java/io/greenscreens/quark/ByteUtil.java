/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 *
 * https://www.greenscreens.io
 *
 */
package io.greenscreens.quark;

import java.nio.ByteBuffer;

enum ByteUtil {
;
	protected static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
	

	/**
	 * Converts byte array to hex string
	 *
	 * @param bytes
	 * @return
	 */
	public static String bytesToHex(final byte[] bytes) {

		final char[] hexChars = new char[bytes.length * 2];
		int v = 0;

		for (int j = 0; j < bytes.length; j++) {
			v = bytes[j] & 0xFF;
			hexChars[j * 2] = HEX_ARRAY[v >>> 4];
			hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
		}

		return new String(hexChars);
	}

	/**
	 * Convert char array to hex string
	 *
	 * @param bytes
	 * @return
	 */
	public static String charsToHex(final char[] bytes) {

		final char[] hexChars = new char[bytes.length * 2];
		int v = 0;

		for (int j = 0; j < bytes.length; j++) {
			v = bytes[j];
			hexChars[j * 2] = HEX_ARRAY[v >>> 4];
			hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
		}

		return new String(hexChars);
	}

	/**
	 * Converts hex string into byte array
	 *
	 * @param s
	 * @return
	 */
	public static byte[] hexStringToByteArray(final String s) {

		final int len = s.length();
		final byte[] data = new byte[len / 2];

		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
		}
		return data;
	}


	/**
	 * Clone ByteBuffer
	 *
	 * @param original
	 * @return
	 */
	public static ByteBuffer clone(final ByteBuffer original) {
		final ByteBuffer clone = ByteBuffer.allocate(original.capacity());
		original.rewind();// copy from the beginning
		clone.put(original);
		original.rewind();
		clone.flip();
		return clone;
	}
	
}
