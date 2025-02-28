/*
 * Copyright (C) 2015, 2023 Green Screens Ltd.
 */
package io.greenscreens.quark.util;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.stream.Stream;

import org.slf4j.Logger;

import io.greenscreens.quark.stream.QuarkStream;
import io.greenscreens.quark.util.override.ByteUtil;
import io.greenscreens.quark.util.override.NamedThreadFactory;
import io.greenscreens.quark.util.override.StringUtil;
import io.greenscreens.quark.util.override.Util;

/**
 * Simple util class for string handling
 */
public enum QuarkUtil {
	;

	/**
	 * Convert byteBuffer to HEX tring
	 * @param buffer
	 * @return
	 */
	public static String bufferToHex(final ByteBuffer buffer) {
		return ByteUtil.bufferToHex(buffer);
	}
	
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
    public static byte[] fromHexAsBytes(final String s) {
        return ByteUtil.fromHexAsBytes(s);
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

	/**
	 * Checks if two string values are equal in safe way, preventing accidental null pointer exception
	 * @param val1
	 * @param val2
	 * @return
	 */
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
	public static boolean close(final AutoCloseable closeable) {
		return Util.close(closeable);
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

    public static String decompressString(final ByteBuffer bytes) throws Exception {
        return new String(decompress(bytes).array(), StandardCharsets.UTF_8);
    }

    public static ByteBuffer compressString(final String s) throws Exception {
        return compress(ByteBuffer.wrap(s.getBytes(StandardCharsets.UTF_8)));
    }

    public static ByteBuffer compress(final ByteBuffer raw) throws Exception {
        return QuarkStream.compress(raw);
    }

    public static ByteBuffer decompress(final ByteBuffer raw) throws Exception {
        return QuarkStream.decompress(raw);
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

	/**
	 * Convert exception to string value or default value if there is no message in exception 
	 * @param e
	 * @param def
	 * @return
	 */
	public static String toMessage(final Throwable e, final String def) {
		return Util.toMessage(e, def);
	}
	
	public static void printError(final Throwable e, final Logger log) {
		Util.printError(e, log);
	}

	/**
	 * Verifies if string is null or empty
	 * @param val
	 * @return
	 */
	public static boolean isEmpty(final String val) {
		return StringUtil.isEmpty(val);
	}
	
	public static boolean nonEmpty(final String val) {
		return !isEmpty(val);
	}
	
	/**
	 * Verifies if string is HEX data
	 * @param val
	 * @return
	 */
	public static boolean isHex(final String val) {
		return StringUtil.isHex(val);
	}

	/**
	 * Return time difference from current time
	 * @param ts
	 * @return
	 */
	public static long timediff(final long ts) {
		return Util.timediff(ts); 
	}

    public static <T extends ExecutorService> T safeTerminate(final T service, final boolean forced) {
        return Util.safeTerminate(service, forced);
    }
    
    public static final ThreadFactory getThreadFactory(final String name, final int priority) {
        return NamedThreadFactory.get(name, priority);
    }

    public static boolean contains(final Map<String, Object> storage, final String key) {
        final boolean isValid = Stream.of(storage, key).allMatch(o -> Objects.nonNull(o));
        return isValid ? storage.containsKey(key) : isValid;
    }
    
    public static Object remove(final Map<String, Object> storage, final String key) {
        final boolean isValid = Stream.of(storage, key).allMatch(o -> Objects.nonNull(o));
        return isValid ? storage.remove(key) : null;
    }
    
    public static Object store(final Map<String, Object> storage, final String key, final Object value) {
        final boolean isValid = Stream.of(storage, key, value).allMatch(o -> Objects.nonNull(o));
        return isValid ? storage.put(key, value) : value;
    }

    public static Object load(final Map<String, Object> storage, final String key) {
        final boolean isValid = Stream.of(storage, key).allMatch(o -> Objects.nonNull(o));
        return isValid ? storage.get(key) : null;
    }

    public static void close(final Map<String, Object> storage) {
        Optional.ofNullable(storage).map(s -> s.values()).ifPresent(QuarkUtil::close);
    }
    
    public static void close(final Collection<Object> storage) {
        Optional.ofNullable(storage).map(s -> s.stream()).ifPresent(QuarkUtil::close);
    }
    
    public static void close(final Stream<Object> storage) {
        Optional.ofNullable(storage).ifPresent(s -> 
            s.filter( o -> o instanceof AutoCloseable)
            .map(o -> (AutoCloseable)o)
            .forEach(QuarkUtil::close)
        );
    }
}
