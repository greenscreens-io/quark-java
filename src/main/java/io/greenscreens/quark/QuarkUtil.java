/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 * 
 * https://www.greenscreens.io
 * 
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

	/**
	 * Converts hex string into byte array
	 * 
	 * @param s
	 * @return
	 */
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
	 * Blank padding for AES algorithm
	 * 
	 * @param source
	 * @return
	 */
	public static String padString(final String source, int size) {
		return StringUtil.padString(source, size);
	}

	/**
	 * Create API response object
	 * 
	 * @param api
	 * @param challenge
	 */
	public static ObjectNode buildAPI(final ArrayNode api, final String challenge) {

		final boolean webCryptoAPI = !StringUtil.isEmpty(challenge);
		final ObjectNode root = JsonNodeFactory.instance.objectNode();
		root.set("api", api);

		final String keyEnc = QuarkSecurity.getRSAPublic(webCryptoAPI);
		final String keyVer = QuarkSecurity.getRSAVerifier(webCryptoAPI);
		root.put("keyEnc", keyEnc);
		root.put("keyVer", keyVer);
		
		if (webCryptoAPI) {
			final String signature = QuarkSecurity.signApiKey(challenge);
			root.put("signature", signature);
		}
		
		return root;

	}

	public static String ungzip(final byte[] bytes) throws Exception {
        return Util.ungzip(bytes);
    }

    public static byte[] gzip(final String s) throws Exception {
        return Util.gzip(s);
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
	
	public static String toMessage(final Throwable e, final String def) {
		return Util.toMessage(e, def);
	}

	public static boolean isEmpty(final String val) {
		return StringUtil.isEmpty(val);
	}

	public static boolean isHex(final String val) {
		return StringUtil.isHex(val);
	}
}
