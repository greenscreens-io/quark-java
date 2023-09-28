/*
 * Copyright (C) 2015, 2023 Green Screens Ltd.
 */
package io.greenscreens.quark;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Handle binary Quark data stream between browser and server
 */
public enum QuarkStream {
;
	private static final int COMPRESSION_RATE = 1024 * 2;
	
	private static final int HEAD_LEN = 8;
	private static final byte FLAG_COMPRESS = 1;
	private static final byte FLAG_ENCRYPT = 2;
	
	private static final int POS_TYPE = 3;
	private static final int POS_LEN = 4;
	private static final int IV_SIZE = 16;
	private static final byte VERSION = 0x05;
	private static final short ITENTIFIER = 18259; // GS read as short

	private static final ByteBuffer EMPTY = ByteBuffer.allocate(0);
	
	/**
	 * Check if received stream is Quark data stream
	 * @param buffer
	 * @return
	 */
	public static boolean isGSStream(final ByteBuffer buffer) {
		buffer.rewind();
		boolean isGSStream = buffer.capacity() > HEAD_LEN && buffer.getShort() == ITENTIFIER && buffer.get() == VERSION;
		final int len = isGSStream ? buffer.getInt(POS_LEN) : 0;
		isGSStream = isGSStream && len + HEAD_LEN == buffer.limit();
		buffer.rewind();
		return isGSStream;
	}
	
	/**
	 * Get length of data inside Quark Stream
	 * @param buffer
	 * @return
	 */
	public static int length(final ByteBuffer buffer) {
		return buffer.getInt(POS_LEN);
	}

	/**
	 * Get data type flag
	 * @param buffer
	 * @return
	 */
	public static int type(final ByteBuffer buffer) {
		return isGSStream(buffer) ? buffer.get(POS_TYPE) : 0;
	}

	/**
	 * Check if compress flag is set 
	 * @param type
	 * @return
	 */
	public static boolean isCompress(final int type) {
		return (type & FLAG_COMPRESS) == FLAG_COMPRESS;
	}

	/**
	 * Check if encrypt flag is set
	 * @param type
	 * @return
	 */
	public static boolean isEncrypt(final int type) {
		return (type & FLAG_ENCRYPT) == FLAG_ENCRYPT;
	}
	
	/**
	 * Get encryption initialization vector
	 * @param buffer
	 * @return
	 */
	public static ByteBuffer iv(final ByteBuffer buffer) {
		buffer.position(HEAD_LEN);
		final ByteBuffer iv = buffer.slice();
		iv.limit(IV_SIZE);
		return iv;
	}

	/**
	 * Get received data
	 * @param buffer
	 * @param isEncrypt
	 * @return
	 */
	public static ByteBuffer data(final ByteBuffer buffer, final boolean isEncrypt) {		
		final int len = isEncrypt ? length(buffer) - IV_SIZE : length(buffer);		
		buffer.position(isEncrypt ? HEAD_LEN + IV_SIZE : HEAD_LEN);	
		final ByteBuffer data = buffer.slice();
		data.limit(len);
		return data;
	}
	
	/**
	 * Convert ByteBuffer to raw byte array
	 * @param buffer
	 * @return
	 */
	public static byte[] asBytes(final ByteBuffer buffer) {
		final byte[] raw = new byte[buffer.remaining()];
		buffer.get(raw);
		return raw;
	}
	
	public static String asString(final ByteBuffer buffer) {
		return asString(buffer, StandardCharsets.UTF_8);
	}
	
	public static String asString(final ByteBuffer buffer, final Charset charset) {
		return new String(asBytes(buffer), charset);
	}
	
	public static ByteBuffer asBuffer(final InputStream inStream, final boolean autoClose) throws IOException {
        return ByteBuffer.wrap(asBytes(inStream, autoClose));
    }
	
	public static String asString(final InputStream inStream, final Charset encoding, final boolean autoClose) throws IOException {
        return new String(asBytes(inStream, autoClose), encoding);
    }
	
	public static String asString(final InputStream inStream, final boolean autoClose) throws IOException {
		return asString(inStream, StandardCharsets.UTF_8, autoClose);
	}
	
	public static byte[] asBytes(final InputStream inStream, final boolean autoClose) throws IOException {
		
		byte [] result = null;
		ByteArrayOutputStream bos = null;
		
		try {
			bos = new ByteArrayOutputStream();
			inStream.transferTo(bos);
			bos.flush();
			result = bos.toByteArray();
		} finally {
			QuarkUtil.close(bos);
			if (autoClose) QuarkUtil.close(inStream);
		}
        
        return result;
    }
	
	public static long stream(final InputStream input, final OutputStream output) throws IOException {
	    try (
	        final ReadableByteChannel inputChannel = Channels.newChannel(input);
	        final WritableByteChannel outputChannel = Channels.newChannel(output);
	    ) {
	        final ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
	        long size = 0;
	        
	        while (inputChannel.read(buffer) != -1) {
	            buffer.flip();
	            size += outputChannel.write(buffer);
	            buffer.clear();
	        }

	        return size;
	    }
	}
	
	/**
	 * Convert Quark data steam into raw data (usually JSON string)
	 * @param buffer
	 * @param key
	 * @return
	 * @throws IOException
	 */
	public static ByteBuffer unwrap(final ByteBuffer buffer, final IQuarkKey key) throws IOException {
		
		buffer.rewind();
		if (!isGSStream(buffer)) return buffer;
		buffer.rewind();

		final int type = type(buffer);
		final boolean isCompress = isCompress(type);
		final boolean isEncrypt = isEncrypt(type);
		
		ByteBuffer data = data(buffer, isEncrypt);
		
		if (isEncrypt) {
			if (Objects.isNull(key)) throw new IOException("No encryption key");
			final ByteBuffer iv = iv(buffer);	
			//data = ByteBuffer.wrap(key.decrypt(asBytes(data), asBytes(iv)));			
			data = key.decrypt(data, asBytes(iv));
		} 
		
		if (isCompress) {
			data = QuarkDecompression.asBuffer(data);
		}
		
		return data;
	}
	
	public static ByteBuffer wrap(final String data, final IQuarkKey key, final boolean isCompress) throws IOException {
		return wrap(data.getBytes(StandardCharsets.UTF_8), key, isCompress);
	}
	
	public static ByteBuffer wrap(final byte[] data, final IQuarkKey key, final boolean isCompress) throws IOException {
		return wrap(ByteBuffer.wrap(data), key, isCompress);
	}
	
	public static ByteBuffer wrap(final ByteBuffer buffer, final IQuarkKey key, final boolean isCompress) throws IOException {
		
		final boolean isEncrypt = Objects.nonNull(key);
		ByteBuffer data = buffer;
		ByteBuffer iv = null;
		byte type = 0;
		
		if (isCompress && data.remaining() > COMPRESSION_RATE) {
			data = QuarkCompression.asBuffer(data);
			type = FLAG_COMPRESS;
		}
		
		if (isEncrypt) {
			iv = ByteBuffer.wrap(QuarkSecurity.getRandom(IV_SIZE));
			//data = ByteBuffer.wrap(key.encrypt(asBytes(data), asBytes(iv)));
			data = key.encrypt(data, asBytes(iv));
			iv.rewind();
			data.rewind();
			type = (byte) (type | FLAG_ENCRYPT);
		} else {
			iv = EMPTY;
		}
		
		final int dataSize = data.remaining() + iv.remaining();
		final ByteBuffer result = ByteBuffer.allocate(HEAD_LEN + dataSize);
		result.putShort(ITENTIFIER)
			  .put(VERSION)
			  .put((byte)type)
			  .putInt(dataSize)
			  .put(iv)
			  .put(data);
		
		result.rewind();
		return result;
	}
}
