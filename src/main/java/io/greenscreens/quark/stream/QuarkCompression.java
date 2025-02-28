/*
 * Copyright (C) 2015, 2023 Green Screens Ltd.
 */
package io.greenscreens.quark.stream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import io.greenscreens.quark.util.QuarkUtil;
import jakarta.enterprise.inject.Vetoed;

/**
 * Data compression
 */
@Vetoed
public enum QuarkCompression {
	;

	// GZIP Compression level - 3 is enough for text data
	public static int LEVEL = 3;
	
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
        OutputStream gzip = null;
        OutputStreamWriter osw = null;

        try {
        	bos = new ByteArrayOutputStream();
        	gzip = new QuarkCompressionStream(bos, LEVEL);
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

    public static long stream(final InputStream inStream, final OutputStream outStream, final boolean autoClose) throws IOException {

    	long transfered = 0;
        OutputStream gzip = null;

        try {
        	gzip = new QuarkCompressionStream(outStream, LEVEL);
            // 02.01.2024. seems transferTo does not work properly
            //transfered = inStream.transferTo(gzip);
            transfered = QuarkStream.stream(inStream, gzip);
        	gzip.flush();
        } finally {
        	if (autoClose) close(gzip);
		}
        return transfered;
    }
	
	public static void close(final AutoCloseable closeable) {
		QuarkUtil.close(closeable);
	}

}
