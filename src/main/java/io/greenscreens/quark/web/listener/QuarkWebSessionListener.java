/*
 * Copyright (C) 2015, 2023 Green Screens Ltd.
 */
package io.greenscreens.quark.web.listener;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionActivationListener;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import io.greenscreens.quark.internal.QuarkConstants;
import io.greenscreens.quark.internal.QuarkValidator;
import io.greenscreens.quark.websocket.heartbeat.HeartbeatService;

/**
 * Set session status flags to detect if session is destroyed. 
 * For example, in SPA apps, user can logout, but on second browser 
 * tab websocket is still active, so WS has to know session is
 * ended to prevent invalid calls.
 */
public final class QuarkWebSessionListener implements HttpSessionListener, HttpSessionActivationListener, ServletContextListener {

	private static final Map<Integer, HttpSession> sessions = new ConcurrentHashMap<>();

	/**
	 * Unload Quark engine
	 */
	@Override
	public void contextDestroyed(final ServletContextEvent event) {
		QuarkValidator.releaseValidator();
		HeartbeatService.terminate();
	}

	/**
	 * Load Quark engine 
	 */
	@Override
	public void contextInitialized(final ServletContextEvent event) {
		QuarkValidator.initValidator();
		HeartbeatService.initialize();
	}
	
	@Override
	public void sessionCreated(final HttpSessionEvent event) {
		final HttpSession httpSession = event.getSession();
		httpSession.setAttribute(QuarkConstants.HTTP_SEESION_STATUS, Boolean.TRUE.toString());
	}

	@Override
	public void sessionDestroyed(final HttpSessionEvent event) {
		final HttpSession httpSession = event.getSession();
		httpSession.setAttribute(QuarkConstants.HTTP_SEESION_STATUS, Boolean.FALSE.toString());
	}
	
	public static Map<Integer, HttpSession> get() {
		return Collections.unmodifiableMap(sessions);
	}

	public static HttpSession get(final String key) {
		return sessions.get(key.hashCode());
	}

	public static HttpSession get(final int key) {
		return sessions.get(key);
	}

	public static void updateSessionTimeout(final int tout) {
		final Iterator<HttpSession> sess = sessions.values().iterator();
		while (sess.hasNext()) {
			sess.next().setMaxInactiveInterval(tout);
		}
	}

}
