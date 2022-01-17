/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 * 
 * https://www.greenscreens.io
 * 
 */
package io.greenscreens.quark;

import java.util.Objects;

import javax.servlet.http.HttpSession;

import io.greenscreens.quark.web.ServletUtils;
import io.greenscreens.quark.websocket.WebSocketSession;
import io.greenscreens.quark.websocket.WebSocketStorage;

/**
 * Receive data from HttpSession or WebSocket session 
 *
 */
public enum QuarkStorage {
	;

	public static <T> T get(final String key) {
		
		final HttpSession session = QuarkProducer.getWebSession(false);
		if (Objects.nonNull(session)) return ServletUtils.get(session, key);
		
		final WebSocketSession wss = QuarkProducer.getWebSocketSession();
		if (Objects.nonNull(wss)) return WebSocketStorage.get(wss, key);

		return null;
	}

	public static <T> T get(final Class<T> key) {
		
		final HttpSession session = QuarkProducer.getWebSession(false);
		if (Objects.nonNull(session)) return ServletUtils.get(session, key);
		
		final WebSocketSession wss = QuarkProducer.getWebSocketSession();
		if (Objects.nonNull(wss)) return WebSocketStorage.get(wss, key);

		return null;
	}

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
	
	public static <T> boolean contains(final String key) {
		return Objects.nonNull(get(key));
		
	}
	
	public static <T> boolean contains(final Class<T> key) {
		return Objects.nonNull(get(key));
	}

	public static <T> boolean contains(final T value) {
		if (Objects.nonNull(value)) return Objects.nonNull(get(value.getClass()));
		return false;
	}
}
