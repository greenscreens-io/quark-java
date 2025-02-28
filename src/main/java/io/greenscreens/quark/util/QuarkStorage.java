/*
 * Copyright (C) 2015, 2023 Green Screens Ltd.
 */
package io.greenscreens.quark.util;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import io.greenscreens.quark.QuarkProducer;
import io.greenscreens.quark.web.ServletStorage;
import io.greenscreens.quark.websocket.WebSocketStorage;

/**
 * Receive data from HttpSession or WebSocket session 
 */
public enum QuarkStorage {
	;
    
    @SuppressWarnings("unchecked")
    static <T> T get(final Optional<T> o1, final Optional<T> o2) {
        return (T) Stream.of(o1, o2).filter(Optional::isPresent).findFirst().map(Optional::get);
    }

    public static boolean contains(final String key) {
        final boolean o1 = QuarkProducer.getWebSession(false).map(s -> ServletStorage.contains(s, key)).orElse(false);     
        final boolean o2 = QuarkProducer.getWebSocketSessionSafe().map(s -> WebSocketStorage.contains(s, key)).orElse(false);
        return o1 || o2;
    }
    
	/**
	 * Get value by given key name.
	 * @param <T>
	 * @param key
	 * @return
	 */
    public static <T> T get(final String key) {
	    final Optional<T> o1 = QuarkProducer.getWebSession(false).map(s -> ServletStorage.get(s, key));	    
	    final Optional<T> o2 = QuarkProducer.getWebSocketSessionSafe().map(s -> WebSocketStorage.get(s, key));
	    return get(o1, o2);
	}
    
	/**
	 * Get value by class canonical name
	 * @param <T>
	 * @param key
	 * @return
	 */
	public static <T> T get(final Class<T> key) {
       final Optional<T> o1 = QuarkProducer.getWebSession(false).map(s -> ServletStorage.get(s, key));     
       final Optional<T> o2 = QuarkProducer.getWebSocketSessionSafe().map(s -> WebSocketStorage.get(s, key));
       return get(o1, o2);
	}

	/**
	 * Store value. Value class canonical name is used as a key.
	 * @param <T>
	 * @param value
	 * @return
	 */
	public static <T> T set(final T value) {
       final Optional<T> o1 = QuarkProducer.getWebSession(false).map(s -> ServletStorage.put(s, value));     
       final Optional<T> o2 = QuarkProducer.getWebSocketSessionSafe().map(s -> WebSocketStorage.store(s, value));
       return get(o1, o2);
	}
	
	/**
	 * Store value by given class. Class canonical name is used as a key.
	 * @param <T>
	 * @param key
	 * @param value
	 * @return
	 */
	public static <T> T set(final Class<T> key, final T value) {
	    final Optional<T> o1 = QuarkProducer.getWebSession(false).map(s -> ServletStorage.put(s, key, value));     
	    final Optional<T> o2 = QuarkProducer.getWebSocketSessionSafe().map(s -> WebSocketStorage.store(s, key, value));
	    return get(o1, o2);
	}

	/**
	 * Store value by given key name.
	 * @param <T>
	 * @param key
	 * @param value
	 * @return
	 */
	public static <T> T set(final String key, final T value) {
        final Optional<T> o1 = QuarkProducer.getWebSession(false).map(s -> ServletStorage.put(s, key, value));     
        final Optional<T> o2 = QuarkProducer.getWebSocketSessionSafe().map(s -> WebSocketStorage.store(s, key, value));
        return get(o1, o2);
	}
	
	/**
	 * Remove value from storage by key name
	 * @param <T>
	 * @param key
	 * @return
	 */
	public static <T> T remove(final String key) {
        final Optional<T> o1 = QuarkProducer.getWebSession(false).map(s -> ServletStorage.remove(s, key));     
        final Optional<T> o2 = QuarkProducer.getWebSocketSessionSafe().map(s -> WebSocketStorage.remove(s, key));
        return get(o1, o2);
	}

	/**
	 * Remove value from storage. Use class name as a key.
	 * @param <T>
	 * @param key
	 * @return
	 */
	public static <T> T remove(final Class<T> key) {
        final Optional<T> o1 = QuarkProducer.getWebSession(false).map(s -> ServletStorage.remove(s, key));     
        final Optional<T> o2 = QuarkProducer.getWebSocketSessionSafe().map(s -> WebSocketStorage.remove(s, key));
        return get(o1, o2);
	}

	/**
	 * Remove value from storage. Use value class name as a key.
	 * @param <T>
	 * @param value
	 * @return
	 */
	public static <T> T remove(final T value) {
        final Optional<T> o1 = QuarkProducer.getWebSession(false).map(s -> ServletStorage.remove(s, value));     
        final Optional<T> o2 = QuarkProducer.getWebSocketSessionSafe().map(s -> WebSocketStorage.remove(s, value));
        return get(o1, o2);
	}
	
	/**
	 * Check if value is stored under given class type 
	 * @param <T>
	 * @param key
	 * @return
	 */
	public static <T> boolean contains(final Class<T> key) {
        return Optional.ofNullable(key)
                .map(c -> get(c))
                .map(Objects::nonNull)
                .orElse(false);
	}

	/**
	 * Check if value is stored under given class type based on value 
	 * @param <T>
	 * @param key
	 * @return
	 */
	public static <T> boolean contains(final T value) {
		return Optional.ofNullable(value)
		        .map(c -> contains(c))
		        .orElse(false);
	}
	
}
