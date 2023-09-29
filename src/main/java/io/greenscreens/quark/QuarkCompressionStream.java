package io.greenscreens.quark;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Add support to set custom compression level.
 */
public class QuarkCompressionStream extends GZIPOutputStream {
	
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
