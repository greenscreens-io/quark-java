/*
 * Copyright (C) 2015, 2023 Green Screens Ltd.
 */
package io.greenscreens.quark;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPOutputStream;

/**
 * Data compression
 */
public enum QuarkCompression {
	;

	public static byte[] asBytes(final byte[] data) throws IOException {
        return asBytes(new ByteArrayInputStream(data), true);
    }

	public static byte[] asBytes(final ByteBuffer bytes) throws IOException {	
		return asBytes(new ByteBufferInputStream(bytes), true);
    }	

	public static ByteBuffer asBuffer(final byte[] data) throws IOException {
        return asBuffer(new ByteArrayInputStream(data), true);
    }

	public static ByteBuffer asBuffer(final ByteBuffer buffer) throws IOException {	
        return buffer.hasRemaining() ? asBuffer(new ByteBufferInputStream(buffer), true) : buffer;
    }
	
	public static ByteBuffer asBuffer(final InputStream inStream, final boolean autoClose) throws IOException {
		return ByteBuffer.wrap(asBytes(inStream, autoClose));
	}
	
    public static ByteBuffer asBuffer(final String s) throws IOException {
    	return ByteBuffer.wrap(asBytes(s));
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
	
    public static byte[] asBytes(final String s) throws IOException {

    	byte [] result = null;
    	ByteArrayOutputStream bos = null;
        GZIPOutputStream gzip = null;
        OutputStreamWriter osw = null;

        try {
        	bos = new ByteArrayOutputStream();
        	gzip = new GZIPOutputStream(bos);
        	osw = new OutputStreamWriter(gzip, StandardCharsets.UTF_8);
        	osw.write(s);
        	osw.flush();      
        	result = bos.toByteArray();
        } finally {
			close(osw);
			close(gzip);
			close(bos);
		}
        
        return result;
    }

    public static void stream(final InputStream inStream, final OutputStream outStream, final boolean autoClose) throws IOException {

        GZIPOutputStream gzip = null;

        try {
        	gzip = new GZIPOutputStream(outStream);
        	inStream.transferTo(gzip);
        	gzip.flush();
        } finally {
        	if (autoClose) close(gzip);
		}
    }
	
	public static void close(final AutoCloseable closeable) {
		Util.close(closeable);
	}

}
