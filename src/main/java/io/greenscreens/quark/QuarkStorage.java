/*
 * Copyright (C) 2015, 2022 Green Screens Ltd.
 */
package io.greenscreens.quark;

import java.util.Objects;

import javax.servlet.http.HttpSession;

import io.greenscreens.quark.web.ServletUtils;
import io.greenscreens.quark.websocket.WebSocketSession;
import io.greenscreens.quark.websocket.WebSocketStorage;

/**
 * Receive data from HttpSession or WebSocket session 
 */
public enum QuarkStorage {
	;

	/**
	 * Get value by given key name.
	 * @param <T>
	 * @param key
	 * @return
	 */
	public static <T> T get(final String key) {
		
		final HttpSession session = QuarkProducer.getWebSession(false);
		if (Objects.nonNull(session)) return ServletUtils.get(session, key);
		
		final WebSocketSession wss = QuarkProducer.getWebSocketSession();
		if (Objects.nonNull(wss)) return WebSocketStorage.get(wss, key);

		return null;
	}

	/**
	 * Get value by class canonical name
	 * @param <T>
	 * @param key
	 * @return
	 */
	public static <T> T get(final Class<T> key) {
		
		final HttpSession session = QuarkProducer.getWebSession(false);
		if (Objects.nonNull(session)) return ServletUtils.get(session, key);
		
		final WebSocketSession wss = QuarkProducer.getWebSocketSession();
		if (Objects.nonNull(wss)) return WebSocketStorage.get(wss, key);

		return null;
	}

	/**
	 * Store value. Value class canonical name is used as a key.
	 * @param <T>
	 * @param value
	 * @return
	 */
	public static <T> T set(final T value) {
		
		final HttpSession session = QuarkProducer.getWebSession(false);
		if (Objects.nonNull(session)) {
			return ServletUtils.put(session, value);
		}
		
		final WebSocketSession wss = QuarkProducer.getWebSocketSession();
		if (Objects.nonNull(wss)) {
			return WebSocketStorage.store(wss, value);
		}
		
		return null;
	}
	
	/**
	 * Store value by given class. Class canonical name is used as a key.
	 * @param <T>
	 * @param key
	 * @param value
	 * @return
	 */
	public static <T> T set(final Class<T> key, final T value) {
		
		final HttpSession session = QuarkProducer.getWebSession(false);
		if (Objects.nonNull(session)) {
			return ServletUtils.put(session, key, value);
		}
		
		final WebSocketSession wss = QuarkProducer.getWebSocketSession();
		if (Objects.nonNull(wss)) {
			return WebSocketStorage.store(wss, key, value);
		}

		return null;
	}

	/**
	 * Store value by given key name.
	 * @param <T>
	 * @param key
	 * @param value
	 * @return
	 */
	public static <T> T set(final String key, final T value) {
		
		final HttpSession session = QuarkProducer.getWebSession(false);
		if (Objects.nonNull(session)) {
			return ServletUtils.put(session, key, value);
		}
		
		final WebSocketSession wss = QuarkProducer.getWebSocketSession();
		if (Objects.nonNull(wss)) {
			return WebSocketStorage.store(wss, key, value);
		}
		
		return null;

	}
	
	/**
	 * Remove value from storage by key name
	 * @param <T>
	 * @param key
	 * @return
	 */
	public static <T> T remove(final String key) {
		
		final HttpSession session = QuarkProducer.getWebSession(false);
		if (Objects.nonNull(session)) {
			return ServletUtils.remove(session, key);
		}
		
		final WebSocketSession wss = QuarkProducer.getWebSocketSession();
		if (Objects.nonNull(wss)) {
			return WebSocketStorage.remove(wss, key);
		}
		
		return null;
	}

	/**
	 * Remove value from storage. Use class name as a key.
	 * @param <T>
	 * @param key
	 * @return
	 */
	public static <T> T remove(final Class<T> key) {
		
		final HttpSession session = QuarkProducer.getWebSession(false);
		if (Objects.nonNull(session)) {
			return ServletUtils.remove(session, key);
		}
		
		final WebSocketSession wss = QuarkProducer.getWebSocketSession();
		if (Objects.nonNull(wss)) {
			return WebSocketStorage.remove(wss, key);
		}

		return null;
	}

	/**
	 * Remove value from storage. Use value class name as a key.
	 * @param <T>
	 * @param value
	 * @return
	 */
	public static <T> T remove(final T value) {
		
		final HttpSession session = QuarkProducer.getWebSession(false);
		if (Objects.nonNull(session)) {
			return ServletUtils.remove(session, value);
		}
		
		final WebSocketSession wss = QuarkProducer.getWebSocketSession();
		if (Objects.nonNull(wss)) {
			return WebSocketStorage.remove(wss, value);
		}
		
		return null;

	}

	/**
	 * Check if value is stored under given key 
	 * @param <T>
	 * @param key
	 * @return
	 */
	public static <T> boolean contains(final String key) {
		return Objects.nonNull(get(key));
		
	}
	
	/**
	 * Check if value is stored under given class type 
	 * @param <T>
	 * @param key
	 * @return
	 */
	public static <T> boolean contains(final Class<T> key) {
		return Objects.nonNull(get(key));
	}

	/**
	 * Check if value is stored under given class type based on value 
	 * @param <T>
	 * @param key
	 * @return
	 */
	public static <T> boolean contains(final T value) {
		if (Objects.nonNull(value)) return Objects.nonNull(get(value.getClass()));
		return false;
	}
}
