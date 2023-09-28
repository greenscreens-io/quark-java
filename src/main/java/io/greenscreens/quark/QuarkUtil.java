/*
 * Copyright (C) 2015, 2023 Green Screens Ltd.
 */
package io.greenscreens.quark;

import java.nio.ByteBuffer;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Simple util class for string handling
 */
public enum QuarkUtil {
	;

	/**
	 * Convert byteBuffer to HEX tring
	 * @param buffer
	 * @return
	 */
	public static String bufferToHex(final ByteBuffer buffer) {
		return ByteUtil.bufferToHex(buffer);
	}
	
	/**
	 * Converts byte array to hex string
	 * 
	 * @param bytes
	 * @return
	 */
	public static String bytesToHex(final byte[] bytes) {
		return ByteUtil.bytesToHex(bytes);	}

	/**
	 * Convert char array to hex string
	 * 
	 * @param bytes
	 * @return
	 */
	public static String charsToHex(final char[] bytes) {
		return ByteUtil.charsToHex(bytes);
	}

	public static byte[] hexStringToByteArray(final String s) {
		return ByteUtil.hexStringToByteArray(s);
	}

	/**
	 * Internal text to boolean conversion
	 * 
	 * @param value
	 * @return
	 */
	public static boolean toBoolean(final String value) {
		return Util.toBoolean(value);
	}

	/**
	 * Internal text to int conversion
	 * 
	 * @param value
	 * @return
	 */
	public static int toInt(final String value) {
		return Util.toInt(value);
	}

	/**
	 * Internal text to long conversion
	 * 
	 * @param value
	 * @return
	 */
	public static long toLong(final String value) {
		return Util.toLong(value);
	}

	/**
	 * Prevent null string
	 * 
	 * @param data
	 * @return
	 */
	public static String normalize(final String data) {
		return StringUtil.normalize(data, "");
	}

	/**
	 * Prevent null string
	 * 
	 * @param data
	 * @return
	 */
	public static String normalize(final String data, final String def) {
		return StringUtil.normalize(data, def);
	}

	/**
	 * Get string length, null support
	 * 
	 * @param data
	 * @return
	 */
	public static int length(final String data) {
		return StringUtil.length(data);
	}

	/**
	 * Checks if two string values are equal in safe way, preventing accidental null pointer exception
	 * @param val1
	 * @param val2
	 * @return
	 */
	public static boolean isEqual(final String val1, final String val2) {
		return StringUtil.isEqual(val1, val2);
	}

	/**
	 * Convert generic object to given type result
	 * 
	 * @param object
	 * @return
	 */
	public static <T> T getObject(final Object object) {
		return Util.getObject(object);
	}

	/**
	 * Clone ByteBuffer
	 * 
	 * @param original
	 * @return
	 */
	public static ByteBuffer clone(final ByteBuffer original) {
		return ByteUtil.clone(original);
	}

	/**
	 * Close any closable objects like stream
	 * 
	 * @param closeable
	 */
	public static void close(final AutoCloseable closeable) {
		Util.close(closeable);
	}

	/**
	 * Create API response object
	 * 
	 * @param api
	 * @param challenge
	 */
	public static ObjectNode buildAPI(final ArrayNode api, final String challenge) {

		final ObjectNode root = JsonNodeFactory.instance.objectNode();
		root.set("api", api);

		final String keyEnc = QuarkSecurity.getPublic();
		final String keyVer = QuarkSecurity.getVerifier();
		final String signature = QuarkSecurity.signApiKey(challenge);

		root.put("keyEnc", keyEnc);
		root.put("keyVer", keyVer);		
		root.put("signature", signature);
		
		return root;

	}

	/**
	 * Null safe Exception error message
	 * 
	 * @param e
	 * @return
	 */
	public static String toMessage(final Throwable e) {
		return Util.toMessage(e);
	}

	/**
	 * Convert exception to string value or default value if there is no message in exception 
	 * @param e
	 * @param def
	 * @return
	 */
	public static String toMessage(final Throwable e, final String def) {
		return Util.toMessage(e, def);
	}

	/**
	 * Verifies if string is null or empty
	 * @param val
	 * @return
	 */
	public static boolean isEmpty(final String val) {
		return StringUtil.isEmpty(val);
	}
	
	public static boolean nonEmpty(final String val) {
		return !isEmpty(val);
	}
	
	/**
	 * Verifies if string is HEX data
	 * @param val
	 * @return
	 */
	public static boolean isHex(final String val) {
		return StringUtil.isHex(val);
	}

	/**
	 * Return time difference from current time
	 * @param ts
	 * @return
	 */
	public static long timediff(final long ts) {
		return Util.timediff(ts); 
	}
	
}
