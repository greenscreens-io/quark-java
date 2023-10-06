/*
 * Copyright (C) 2015, 2023 Green Screens Ltd.
 */
package io.greenscreens.quark.stream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;

import io.greenscreens.quark.utils.QuarkUtil;
import jakarta.enterprise.inject.Vetoed;

/**
 * Data decompression
 */
@Vetoed
enum QuarkDecompression {
	;

	public static byte [] asBytes(final byte[] bytes) throws IOException {        
		return asBytes(new ByteArrayInputStream(bytes), true);
    }

	public static byte[] asBytes(final ByteBuffer buffer) throws IOException {
        return asBytes(new ByteBufferInputStream(buffer), true);
    }
	
	public static String asString(final byte[] bytes) throws IOException {
		return asString(new ByteArrayInputStream(bytes), true);
    }

	public static String asString(final ByteBuffer buffer) throws IOException {	
		return asString(new ByteBufferInputStream(buffer), true);
    }

	public static ByteBuffer asBuffer(final ByteBuffer buffer) throws IOException {
        return buffer.hasRemaining() ? asBuffer(new ByteBufferInputStream(buffer), true) : buffer;
    }
	
	public static ByteBuffer asBuffer(final InputStream inStream, final boolean autoClose) throws IOException {
		return ByteBuffer.wrap(asBytes(inStream, autoClose));
	}
	
	public static byte[] asBytes(final InputStream inStream, final boolean autoClose) throws IOException {
		
		byte [] result = null;
		ByteArrayOutputStream bos = null;
		
		try {
			bos = new ByteArrayOutputStream();
			stream(inStream, bos, autoClose);
			result = bos.toByteArray();
		} finally {
			close(bos);
		}
        
        return result;
    }
	
	public static String asString(final InputStream inStream, final boolean autoClose) throws IOException {
		
		String result = null;
		StringWriter sw = null;
		InputStream gis = null;
		InputStreamReader isr = null;
		
		try {
			sw = new StringWriter();
			gis = new GZIPInputStream(inStream);
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
			if (autoClose) close(inStream);
		}
        
        return result;
    }
	
    public static void stream(final InputStream inStream, final OutputStream outStream, final boolean autoClose) throws IOException {

    	InputStream gzip = null;

        try {
        	gzip = new GZIPInputStream(inStream);
        	gzip.transferTo(outStream);
        	outStream.flush();
        } finally {
        	if (autoClose) close(gzip);
		}
    }
	public static void close(final AutoCloseable closeable) {
		QuarkUtil.close(closeable);
	}

}
