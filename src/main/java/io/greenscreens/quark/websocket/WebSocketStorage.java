/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 *
 * https://www.greenscreens.io
 */
package io.greenscreens.quark.websocket;

import javax.websocket.Session;

@SuppressWarnings("unchecked")
public enum WebSocketStorage {
;
	
	public static boolean contains(final Session session, final String key) {
    	return session.getUserProperties().containsKey(key);
    }
	
	public static <T> boolean contains(final Session session, final Class<T> type) {
    	final String key = type.getCanonicalName();
    	return session.getUserProperties().containsKey(key);
    }
	
	public static <T> T remove(final Session session, final Class<T> type) {
    	final String key = type.getCanonicalName();
    	if (session.getUserProperties().containsKey(key)) {
    	   final Object obj = session.getUserProperties().remove(key);
    	   return (T) obj;
    	}
    	return null;
    }

	public static <T> T get(final Session session, final Class<T> type) {
    	final String key = type.getCanonicalName();
    	if (session.getUserProperties().containsKey(key)) {
    	   final Object obj = session.getUserProperties().get(key);
    	   return (T) obj;
    	}
    	return null;
    }

	public static <T> T get(final Session session, final String key) {
    	if (session.getUserProperties().containsKey(key)) {
    	   final Object obj = session.getUserProperties().get(key);
    	   return (T) obj;
    	}
    	return null;
    }

	public static <T> T store(final Session session, final T value) {
    	final String key = value.getClass().getCanonicalName();
    	return (T) session.getUserProperties().put(key, value);
    }

	public static <T> T store(final Session session, final String key, final T value) {
		return (T) session.getUserProperties().put(key, value);
	}
	
	public static <T> T replace(final Session session, final T value) {
		final T old = (T) remove(session, value.getClass());
		store(session, value);
		return old;
	}
}
