/*
 * Copyright (C) 2015, 2023 Green Screens Ltd.
 */
package io.greenscreens.quark.stream;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import jakarta.enterprise.inject.Vetoed;

/**
 * Used internally by compression tools
 */
@Vetoed
final class ByteBufferInputStream extends InputStream {

    ByteBuffer buf;

    public ByteBufferInputStream(final ByteBuffer buf) {
        this.buf = buf;
    }

    public int read() throws IOException {
    	if (buf.hasRemaining()) return buf.get() & 0xFF;
    	return -1;
    }

    @Override
    public int read(byte[] bytes, int off, int len) throws IOException {
        
    	if (buf.hasRemaining()) {
    		len = Math.min(len, buf.remaining());
    		buf.get(bytes, off, len);
    		return len;
        }
    	
    	return -1;
    }

	@Override
	public int available() throws IOException {
		return buf.remaining();
	}
    
    
}
