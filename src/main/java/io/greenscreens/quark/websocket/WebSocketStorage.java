/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 *
 * https://www.greenscreens.io
 */
package io.greenscreens.quark.websocket;

import javax.websocket.EndpointConfig;
import javax.websocket.Session;

import io.greenscreens.quark.QuarkUtil;


@SuppressWarnings("unchecked")
public enum WebSocketStorage {
;

	public static <T> void store(final EndpointConfig sec, final T value) {
		if (value != null) {
			store(sec, value.getClass().getCanonicalName(), value);
		}
	}

	public static <T> void store(final EndpointConfig sec, final String key, final T value) {
		if (key != null && value != null) {
			sec.getUserProperties().put(key, value);
		}
	}
	
	public static <T> T get(final EndpointConfig sec, final Class<T> key) {
		if (key != null) return get(sec, key.getCanonicalName());
		return null;
	}
	
	public static <T> T get(final EndpointConfig sec, final String key) {
		if (key != null) {
			return (T) sec.getUserProperties().get(key);
		}
		return  null;
	}
	
	public static boolean contains(final EndpointConfig sec, final String key) {
		if (key != null) {
			return sec.getUserProperties().containsKey(key);
		}
		return  false;
	}
		
	public static boolean contains(final Session session, final String key) {
		return session.getUserProperties().containsKey(key);
    }
	
	public static <T> boolean contains(final Session session, final Class<T> type) {
    	final String key = type.getCanonicalName();
    	return contains(session, key);
    }

	public static <T> T remove(final Session session, final String key) {  	
		if (key != null) {
			return (T) session.getUserProperties().remove(key);
		}
    	return null;
    }
	
	public static <T> T remove(final Session session, final Class<T> type) {
    	final String key = type.getCanonicalName();    	
    	if (key != null) {
    	   return (T) session.getUserProperties().remove(key);
    	}
    	return null;
    }

	public static <T> T get(final Session session, final Class<T> type) {
    	final String key = type.getCanonicalName();
    	if (key != null) {
    	   return (T) session.getUserProperties().get(key);
    	}
    	return null;
    }

	public static <T> T get(final Session session, final String key) {
		if (key != null) {
			return (T) session.getUserProperties().get(key);
    	}
    	return null;
    }

	public static <T> T store(final Session session, final T value) {
    	final String key = value.getClass().getCanonicalName();
    	if (key != null) {
     	   return (T) session.getUserProperties().put(key, value);
    	}
    	return null;
    }

	public static <T> T store(final Session session, final String key, final T value) {
		if (key != null) {
			return (T) session.getUserProperties().put(key, value);
		}
		return null;
	}
	
	public static <T> T replace(final Session session, final T value) {
		final T old = (T) remove(session, value.getClass());
		store(session, value);
		return old;
	}

	public static void close(final Session session) {
		session.getUserProperties()
			.values().stream()
			.filter(v ->  v instanceof AutoCloseable)
			.forEach(v -> QuarkUtil.close((AutoCloseable) v));		
	}
}
