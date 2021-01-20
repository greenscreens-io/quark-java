/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 *
 * https://www.greenscreens.io
 *
 */
package io.greenscreens.quark;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple util class for string handling
 */
enum Util {
	;

	private static final Logger LOG = LoggerFactory.getLogger(Util.class);

	/**
	 * Internal text to boolean conversion
	 *
	 * @param value
	 * @return
	 */
	public static boolean toBoolean(final String value) {

		if (value == null) {
			return false;
		}

		if (Boolean.TRUE.toString().equals(value.trim().toLowerCase())) {
			return true;
		}

		if (Boolean.FALSE.toString().equals(value.trim().toLowerCase())) {
			return false;
		}

		return false;
	}

	/**
	 * Internal text to int conversion
	 *
	 * @param value
	 * @return
	 */
	public static int toInt(final String value) {

		int val = 0;

		try {
			val = Integer.parseInt(StringUtil.normalize(value, "0").trim());
		} catch (Exception e) {
			final String msg = toMessage(e);
			LOG.error(msg);
			LOG.debug(msg, e);
		}

		return val;
	}

	/**
	 * Internal text to long conversion
	 *
	 * @param value
	 * @return
	 */
	public static long toLong(final String value) {

		long val = 0;

		try {
			val = Long.parseLong(StringUtil.normalize(value, "0").trim());
		} catch (Exception e) {
			LOG.error(e.getMessage());
			LOG.debug(e.getMessage(), e);
		}

		return val;
	}

	/**
	 * Convert generic object to given type result
	 *
	 * @param object
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getObject(final Object object) {
		if (object == null) {
			return null;
		}
		return (T) object;
	}


	public static String ungzip(final byte[] bytes) throws Exception {
		
		String result = null;
		StringWriter sw = null;
		ByteArrayInputStream bis = null;
		GZIPInputStream gis = null;
		InputStreamReader isr = null;
		
		
		try {
			sw = new StringWriter();
			bis = new ByteArrayInputStream(bytes);
			gis = new GZIPInputStream(bis);
			isr = new InputStreamReader(gis, StandardCharsets.UTF_8);

			final char[] chars = new char[1024];
			for (int len; (len = isr.read(chars)) > 0; ) {
				sw.write(chars, 0, len);
			}
			sw.flush();
			result = sw.toString();
		} finally {
			close(isr);
			close(sw);
		}
        
        return result;
    }

    public static byte[] gzip(final String s) throws Exception {

    	final ByteArrayOutputStream bos = new ByteArrayOutputStream();
    	
        GZIPOutputStream gzip = null;
        OutputStreamWriter osw = null;
        
        try {
        	gzip = new GZIPOutputStream(bos);
        	osw = new OutputStreamWriter(gzip, StandardCharsets.UTF_8);
        	osw.write(s);
        	osw.flush();        	
        } finally {
			close(osw);
			close(gzip);
		}
        
        return bos.toByteArray();
    }

	/**
	 * Close any closable objects like stream
	 * 
	 * @param closeable
	 */
	public static void close(final AutoCloseable closeable) {
		if (closeable != null) {
			try {
				closeable.close();
			} catch (Exception e) {
				// ignored
			}
		}
	}
	
	/**
	 * Null safe Exception error message
	 * 
	 * @param e
	 * @return
	 */
	public static String toMessage(final Throwable e) {
		return toMessage(e, e == null ? "" : e.toString());
	}
	
	public static String toMessage(final Throwable e, final String def) {
		if (e == null) {
			return "";
		}

		String err = e.getMessage();
		if (err == null && e.getCause() != null) {
			err = e.getCause().getMessage();
		}

		if (err == null) {
			err = def;
		}

		return err;
	}

}
