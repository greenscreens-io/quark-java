/*
 * Copyright (C) 2015, 2023 Green Screens Ltd.
 */
package io.greenscreens.quark.util.override;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.greenscreens.quark.util.QuarkUtil;

/**
 * Simple util class for string handling
 */
public enum Util {
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

	/**
	 * Close any closable objects like stream
	 * 
	 * @param closeable
	 */
	public static boolean close(final AutoCloseable closeable) {
	    if (Objects.nonNull(closeable)) {
			try {
				closeable.close();
				return true;
			} catch (Exception e) {
				// ignored
			}
		}
	    return false;
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

	/**
	 * Print exception trace in a safe manner
	 * @param e
	 */
	public static void printError(final Throwable e, final Logger log) {
		final String error = QuarkUtil.normalize(e.getMessage());
		final String msg = error.replace("\n", "; ");
		final String[] items = error.split("\n");
		for (String item : items) {
			log.error(item);
		}
		log.debug(msg, e);
	}
	
	/**
	 * Calculate difference between current time and given timestamp Timestamp can
	 * be in UNIX format (PHP) or Java
	 * 
	 * @param time
	 * @return
	 */
	public static final long timediff(final long time) {

		if (time <= 0) {
			return 0;
		}

		final long stime = System.currentTimeMillis();
		return Math.abs(stime - time);
	}
    
    /**
     * Safely terminate executor
     * @param <T>
     * @param service
     * @return
     */
    public static <T extends ExecutorService> T safeTerminate(final T service, final boolean forced) {
        if (Objects.isNull(service)) return service; 
        try {
            if (forced) {
                if (service instanceof ThreadPoolExecutor) {
                    ((ThreadPoolExecutor) service).purge();
                }
                service.shutdownNow();
            } else {
                service.shutdown();
            }
        } catch (Exception e) {
            final String msg = toMessage(e);
            LOG.error(msg);
            LOG.debug(msg, e);
        }
        return service;
    }
    
    public static <T extends ExecutorService> T safeTerminate(final T service) {
        return safeTerminate(service, false);
    }     

}
