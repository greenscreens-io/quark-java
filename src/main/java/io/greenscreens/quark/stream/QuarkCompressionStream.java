/*
 * Copyright (C) 2015, 2023 Green Screens Ltd.
 */
package io.greenscreens.quark.stream;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import jakarta.enterprise.inject.Vetoed;

/**
 * Add support to set custom compression level.
 * Seems, there is some issue with underlying native zip,
 * as there is significant performance change if level is set before compressing the data.   
 */
@Vetoed
class QuarkCompressionStream extends GZIPOutputStream {
	
	public QuarkCompressionStream(final OutputStream out, final int size, final int level, final boolean syncFlush) throws IOException  {
		super(out, size, syncFlush);
		this.def.setLevel(level);
	}

	public QuarkCompressionStream(final OutputStream out, final int size, final int level) throws IOException {
		this(out, size, level, false);
	}
	
	public QuarkCompressionStream(final OutputStream out, final int level) throws IOException {
		this(out, 512, level, false);
	}
	
	public QuarkCompressionStream(final OutputStream out, final int level, final boolean syncFlush) throws IOException {
		this(out, 512, level, syncFlush);
	}

	public void setLevel(final int level) {
		this.def.setLevel(level);		
	}
	
	public QuarkCompressionStream(final OutputStream out) throws IOException {
		super(out, 512, false);
	}
}
