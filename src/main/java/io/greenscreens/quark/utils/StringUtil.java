/*
 * Copyright (C) 2015, 2023. Green Screens Ltd.
 */
package io.greenscreens.quark.utils;

import java.util.Optional;
import java.util.regex.Pattern;

/**
 * General string helper class
 */
enum StringUtil {
;

	private static final Pattern HEX = Pattern.compile("[0-9A-F]+", Pattern.CASE_INSENSITIVE);
	
	public static boolean isHex(final String value) {
		if (isEmpty(value)) return false;
		return HEX.matcher(value).matches();
	}

	/**
	 * Prevent null string while triming 
	 *
	 * @param data
	 * @return
	 */
	public static String trim(final String data) {
		return normalize(data, "").trim();
	}

	/**
	 * Prevent null string
	 *
	 * @param data
	 * @return
	 */
	public static String normalize(final String data) {
		return normalize(data, "");
	}

	/**
	 * Prevent null string
	 *
	 * @param data
	 * @return
	 */
	public static String normalize(final String data, final String def) {
		return Optional.ofNullable(data).orElse(def);
	}

	/**
	 * Get string length, null support
	 *
	 * @param data
	 * @return
	 */
	public static int length(final String data) {
		return Optional.ofNullable(data).orElse("").length();
	}

	public static boolean isEqual(final String val1, final String val2) {
		return normalize(val1).equals(normalize(val2));
	}

	/**
	 * Null safe check if string is empty
	 * 
	 * @param val
	 * @return
	 */
	public static boolean isEmpty(final String val) {
		return normalize(val).isEmpty();
	}
	
}
