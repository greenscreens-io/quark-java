/*
 * Copyright (C) 2015, 2023 Green Screens Ltd.
 */
package io.greenscreens.quark.websocket;

import java.util.Objects;

import io.greenscreens.quark.utils.QuarkUtil;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.Session;

/**
 * Helper class to store data into WebSocket session
 */
@SuppressWarnings("unchecked")
public enum WebSocketStorage {
;

	/**
	 * Store data to Endpoint , key is value class canonical name
	 * @param <T>
	 * @param sec
	 * @param value
	 */
	public static <T> void store(final EndpointConfig sec, final T value) {
		if (value != null) {
			store(sec, value.getClass().getCanonicalName(), value);
		}
	}

	/**
	 * Store data to Endpoint , key is class cannnical name
	 * @param <T>
	 * @param sec
	 * @param key
	 * @param value
	 * @return
	 */
	public static <T> T store(final EndpointConfig sec, final Class<T> key, final T value) {
		if (Objects.nonNull(key) && Objects.nonNull(value)) {
			sec.getUserProperties().put(key.getCanonicalName(), value);
			return value;
		}
		return null;
	}
	
	/**
	 * Store data to Endpoint by given key
	 * @param <T>
	 * @param sec
	 * @param key
	 * @param value
	 * @return
	 */
	public static <T> T store(final EndpointConfig sec, final String key, final T value) {
		if (Objects.nonNull(key) && Objects.nonNull(value)) {
			sec.getUserProperties().put(key, value);
			return value;
		}
		return null;
	}

	/**
	 * Remove data from Endpoint , key is value class canonical name
	 * @param <T>
	 * @param sec
	 * @param value
	 * @return
	 */
	public static <T> T remove(final EndpointConfig sec, final T value) {
		T obj = null;
		if (Objects.nonNull(value)) {
			obj = (T) remove(sec, value.getClass());
		}
		return obj;
	}
	
	/**
	 * Remove data from Endpoint , key is class canonical name
	 * @param <T>
	 * @param sec
	 * @param value
	 * @return
	 */
	public static <T> T remove(final EndpointConfig sec, final Class<T> value) {
		T obj = null;
		if (Objects.nonNull(value)) {
			obj = remove(sec, value.getCanonicalName());
		}
		return obj;
	}

	/**
	 * Remove data from Endpoint by given key
	 * @param <T>
	 * @param sec
	 * @param key
	 * @return
	 */
	public static <T> T remove(final EndpointConfig sec, final String key) {
		final T obj = get(sec, key, null);
		if (Objects.nonNull(key)) {
			sec.getUserProperties().remove(key);
		}
		return obj;
	}
	
	/**
	 * Get data from Endpoint, key is class canonical name
	 * @param <T>
	 * @param sec
	 * @param key
	 * @return
	 */
	public static <T> T get(final EndpointConfig sec, final Class<T> key) {
		return get(sec, key, null);
	}
	
	public static <T> T get(final EndpointConfig sec, final Class<T> key, final T defaults) {
		if (Objects.nonNull(key)) return get(sec, key.getCanonicalName(), defaults);
		return null;
	}
	
	/**
	 * Get data from Endpoint by given key
	 * @param <T>
	 * @param sec
	 * @param key
	 * @return
	 */
	public static <T> T get(final EndpointConfig sec, final String key) {
		return get(sec, key, null);
	}
	
	public static <T> T get(final EndpointConfig sec, final String key, final T defaults) {
		if (Objects.nonNull(key)) {
			return (T) sec.getUserProperties().get(key);
		}
		return  defaults;
	}
	
	/**
	 * Check if data exist in Endpoint by given key
	 * @param sec
	 * @param key
	 * @return
	 */
	public static boolean contains(final EndpointConfig sec, final String key) {
		if (Objects.nonNull(key)) {
			return sec.getUserProperties().containsKey(key);
		}
		return  false;
	}
		
	/**
	 * Check if data exist in WebSocket SEssion by given key
	 * @param session
	 * @param key
	 * @return
	 */
	public static boolean contains(final Session session, final String key) {
		return session.getUserProperties().containsKey(key);
    }
	
	/**
	 * Check if data exist in WebSocket Session, key is class canonical name 
	 * @param <T>
	 * @param session
	 * @param type
	 * @return
	 */
	public static <T> boolean contains(final Session session, final Class<T> type) {
    	final String key = type.getCanonicalName();
    	return contains(session, key);
    }

	/**
	 * Remove data from WebSocket Session, by given key name
	 * @param <T>
	 * @param session
	 * @param key
	 * @return
	 */
	public static <T> T remove(final Session session, final String key) {  	
		if (Objects.nonNull(key)) {
			return (T) session.getUserProperties().remove(key);
		}
    	return null;
    }
	
	/**
	 * Remove data from WebSocket Session, key is class canonical name
	 * @param <T>
	 * @param session
	 * @param type
	 * @return
	 */
	public static <T> T remove(final Session session, final Class<T> type) {
    	if (Objects.nonNull(type)) {
    		final String key = type.getCanonicalName();    	
    	   return (T) session.getUserProperties().remove(key);
    	}
    	return null;
    }

	/**
	 * Remove data from WebSocket Session, key is value class canonical name
	 * @param <T>
	 * @param session
	 * @param value
	 * @return
	 */
	public static <T> T remove(final Session session, final T value) {  	
    	if (Objects.nonNull(value)) {
    		final String key = value.getClass().getCanonicalName();
    	   return (T) session.getUserProperties().remove(key);
    	}
    	return null;
    }
	
	/**
	 * Retrieve data from WebSocket Session, key is class canonical name
	 * @param <T>
	 * @param session
	 * @param type
	 * @return
	 */
	public static <T> T get(final Session session, final Class<T> type) {
    	final String key = type.getCanonicalName();
    	if (Objects.nonNull(key)) {
    	   return (T) session.getUserProperties().get(key);
    	}
    	return null;
    }

	/**
	 * Retrieve data from WebSocket Session by given key name
	 * @param <T>
	 * @param session
	 * @param key
	 * @return
	 */
	public static <T> T get(final Session session, final String key) {
		if (Objects.nonNull(key)) {
			return (T) session.getUserProperties().get(key);
    	}
    	return null;
    }

	/**
	 * Store data to WebSocket Session, key is value class canonical name
	 * @param <T>
	 * @param session
	 * @param value
	 * @return
	 */
	public static <T> T store(final Session session, final T value) {
    	final String key = value.getClass().getCanonicalName();
    	if (Objects.nonNull(key)) {
     	   return (T) session.getUserProperties().put(key, value);
    	}
    	return null;
    }

	/**
	 * Store data to WebSocket Session, key is class canonical name
	 * @param <T>
	 * @param session
	 * @param key
	 * @param value
	 * @return
	 */
	public static <T> T store(final Session session, final Class<T> key, final T value) {
		if (Objects.nonNull(key) && Objects.nonNull(value)) {
			session.getUserProperties().put(key.getCanonicalName(), value);
			return value;
		}
		return null;
	}
	
	/**
	 * Store data to WebSocket Session by given key name
	 * @param <T>
	 * @param session
	 * @param key
	 * @param value
	 * @return
	 */
	public static <T> T store(final Session session, final String key, final T value) {
		if (Objects.nonNull(key)) {
			return (T) session.getUserProperties().put(key, value);
		}
		return null;
	}
	
	/**
	 * Replace data in WebSocket Session, key is value class canonical name
	 * @param <T>
	 * @param session
	 * @param value
	 * @return
	 */
	public static <T> T replace(final Session session, final T value) {
		final T old = (T) remove(session, value.getClass());
		store(session, value);
		return old;
	}

	/**
	 * Safe close WebSocket session
	 * @param session
	 */
	public static void close(final Session session) {
		session.getUserProperties()
			.values().stream()
			.filter(v ->  v instanceof AutoCloseable)
			.forEach(v -> QuarkUtil.close((AutoCloseable) v));		
	}
}
