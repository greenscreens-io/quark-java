/*
 * Copyright (C) 2015, 2023 Green Screens Ltd.
 */
package io.greenscreens.quark.websocket;

import java.util.Optional;

import io.greenscreens.quark.util.QuarkUtil;
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
	public static <T> T store(final EndpointConfig sec, final T value) {
	    return Optional.ofNullable(value).map(v -> v.getClass().getCanonicalName()).map(name -> store(sec, name, value)).orElse(value);
	}

	/**
	 * Store data to Endpoint , key is class canonical name
	 * @param <T>
	 * @param sec
	 * @param key
	 * @param value
	 * @return
	 */
	public static <T> T store(final EndpointConfig sec, final Class<T> key, final T value) {
	    return Optional.ofNullable(key).map(k -> k.getCanonicalName()).map(name -> store(sec, name, value)).orElse(value);
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
	    return (T) Optional.ofNullable(sec).map(s -> s.getUserProperties()).map(s -> QuarkUtil.store(s, key, value)).orElse(value);
	}

	/**
	 * Remove data from Endpoint , key is value class canonical name
	 * @param <T>
	 * @param sec
	 * @param value
	 * @return
	 */
	public static <T> T remove(final EndpointConfig sec, final T value) {
	    return (T) Optional.ofNullable(value).map(v -> v.getClass().getCanonicalName()).map(name -> remove(sec, name)).orElse(value);	    
	}
	
	/**
	 * Remove data from Endpoint , key is class canonical name
	 * @param <T>
	 * @param sec
	 * @param value
	 * @return
	 */
	public static <T> T remove(final EndpointConfig sec, final Class<T> value) {
	    return (T) Optional.ofNullable(value).map(v -> v.getCanonicalName()).map(name -> remove(sec, name)).orElse(value);
	}

	/**
	 * Remove data from Endpoint by given key
	 * @param <T>
	 * @param sec
	 * @param key
	 * @return
	 */
	public static <T> T remove(final EndpointConfig sec, final String key) {
	    return (T) Optional.ofNullable(sec).map(s -> s.getUserProperties()).map(s-> QuarkUtil.remove(s, key));	    
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
	    return Optional.ofNullable(key)
	            .map(k -> k.getCanonicalName())
	            .map(name -> get(sec, name, defaults))
	            .orElse(null);
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
	    return (T) Optional.ofNullable(sec)
	            .map(s -> s.getUserProperties())
	            .map(p -> QuarkUtil.load(p, key))
	            .orElse(defaults);
	}
	
	/**
	 * Check if data exist in Endpoint by given key
	 * @param sec
	 * @param key
	 * @return
	 */
	public static boolean contains(final EndpointConfig sec, final String key) {
        return Optional.ofNullable(sec)
                .map(s -> s.getUserProperties())
                .map(p -> QuarkUtil.contains(p, key))
                .orElse(false);        
	}
		
	/**
	 * Check if data exist in WebSocket SEssion by given key
	 * @param session
	 * @param key
	 * @return
	 */
	public static boolean contains(final Session session, final String key) {
        return Optional.ofNullable(session)
                .map(s -> s.getUserProperties())
                .map(p -> QuarkUtil.contains(p, key))
                .orElse(false);
    }
	
	/**
	 * Check if data exist in WebSocket Session, key is class canonical name 
	 * @param <T>
	 * @param session
	 * @param type
	 * @return
	 */
	public static <T> boolean contains(final Session session, final Class<T> type) {
        return Optional.ofNullable(type)
                .map(t -> t.getCanonicalName())
                .map(key -> contains(session, key))
                .orElse(false);        
    }

	/**
	 * Remove data from WebSocket Session, by given key name
	 * @param <T>
	 * @param session
	 * @param key
	 * @return
	 */
	public static <T> T remove(final Session session, final String key) {
        return (T) Optional.ofNullable(session)
                .map(s -> s.getUserProperties())
                .map(p -> QuarkUtil.remove(p, key))
                .orElse(null);        
    }
	
	/**
	 * Remove data from WebSocket Session, key is class canonical name
	 * @param <T>
	 * @param session
	 * @param type
	 * @return
	 */
	public static <T> T remove(final Session session, final Class<T> type) {
        return (T) Optional.ofNullable(type)
                .map(t -> t.getCanonicalName())
                .map(key -> remove(session, key))
                .orElse(null);        
    }

	/**
	 * Remove data from WebSocket Session, key is value class canonical name
	 * @param <T>
	 * @param session
	 * @param value
	 * @return
	 */
	public static <T> T remove(final Session session, final T value) {
        return (T) Optional.ofNullable(value)
                .map(t -> t.getClass())
                .map(t -> t.getCanonicalName())
                .map(key -> remove(session, key))
                .orElse(null);        
    }
	
	/**
	 * Retrieve data from WebSocket Session, key is class canonical name
	 * @param <T>
	 * @param session
	 * @param type
	 * @return
	 */
	public static <T> T get(final Session session, final Class<T> type) {
        return (T) Optional.ofNullable(type)
                .map(t -> t.getCanonicalName())
                .map(key -> get(session, key))
                .orElse(null);
    }

	/**
	 * Retrieve data from WebSocket Session by given key name
	 * @param <T>
	 * @param session
	 * @param key
	 * @return
	 */
	public static <T> T get(final Session session, final String key) {
        return (T) Optional.ofNullable(session)
                .map(s -> s.getUserProperties())
                .map(p -> QuarkUtil.load(p, key))
                .orElse(null);  	    
    }

	/**
	 * Store data to WebSocket Session, key is value class canonical name
	 * @param <T>
	 * @param session
	 * @param value
	 * @return
	 */
	public static <T> T store(final Session session, final T value) {
        return (T) Optional.ofNullable(value)
                .map(t -> t.getClass())
                .map(t -> t.getCanonicalName())
                .map(key -> store(session, key))
                .orElse(null);   
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
        return (T) Optional.ofNullable(key)
                .map(t -> t.getCanonicalName())
                .map(k-> store(session, k))
                .orElse(null);        
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
        return (T) Optional.ofNullable(session)
                .map(s -> s.getUserProperties())
                .map(p -> QuarkUtil.store(p, key, value))
                .orElse(null);          
	}
	
	/**
	 * Replace data in WebSocket Session, key is value class canonical name
	 * @param <T>
	 * @param session
	 * @param value
	 * @return
	 */
	public static <T> T replace(final Session session, final T value) {
	    final T old = (T) Optional.ofNullable(value).map(v -> v.getClass()).map(k -> remove(session, k)).orElse(null);
		store(session, value);
		return old;
	}

	/**
	 * Safe close WebSocket session
	 * @param session
	 */
	public static void close(final Session session) {
	    Optional.ofNullable(session)
	    .map(s -> s.getUserProperties())
	    .ifPresent(QuarkUtil::close);
	}
}
