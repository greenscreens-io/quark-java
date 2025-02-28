/*
 * Copyright (C) 2015, 2023 Green Screens Ltd.
 */
package io.greenscreens.quark;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.servlet.http.HttpSession;

import io.greenscreens.quark.async.QuarkAsyncContext;
import io.greenscreens.quark.web.QuarkContext;
import io.greenscreens.quark.websocket.WebSocketSession;

/**
 * CDI helper to provide injections for Quark engine.
 * Mostly used for Controllers - exposed to web.
 */
@ApplicationScoped
public class QuarkProducer {

	private static final ThreadLocal<WebSocketSession> websocketContextThreadLocal = new ThreadLocal<>();
	private static final ThreadLocal<QuarkAsyncContext> asyncContextThreadLocal = new ThreadLocal<>();
	private static final ThreadLocal<QuarkContext> httpThreadLocal = new ThreadLocal<>();

	@Produces
	public static WebSocketSession getWebSocketSession() {
		return websocketContextThreadLocal.get();
	}

	@Produces
	public static QuarkAsyncContext getQuarkAsyncContext() {
		return asyncContextThreadLocal.get();
	}

	@Produces
	public static QuarkContext getQuarkContext() {
		return httpThreadLocal.get();
	}

	@Produces
	public static HttpSession getWebSession() {
		return getWebSession(true).orElse(null);
	}	

    public static Optional<WebSocketSession> getWebSocketSessionSafe() {
        return Optional.ofNullable(websocketContextThreadLocal.get());
    }

    public static Optional<QuarkAsyncContext> getQuarkAsyncContextSafe() {
        return Optional.ofNullable(asyncContextThreadLocal.get());
    }

    public static Optional<QuarkContext> getQuarkContextSafe() {
        return Optional.ofNullable(httpThreadLocal.get());
    }

    public static Optional<HttpSession> getWebSessionSafe() {
        return getWebSession(true);
    }   
    
	/**
	 * Determine if current context is from HTTP request or WebSocket
	 * Based on that, returns session from proper thread context. 
	 * @param create
	 * @return
	 */
	public static Optional<HttpSession> getWebSession(final boolean create) {
		
	    final Optional<HttpSession> s1 = getQuarkContextSafe().map(c -> c.getRequest().getSession(create));
	    final Optional<HttpSession> s2 = getWebSocketSessionSafe().map(c -> c.getHttpSession());	    
	    final Optional<HttpSession> s3 = getQuarkAsyncContextSafe().map(c -> c.getRequest().getSession(create));
	    
	    return Stream.of(s1, s2, s3).filter(Optional::isPresent).findFirst().map(Optional::get);
	}
	
	/**
	 * Store servlet request/response instances to current thread context 
	 * @param request
	 */
	public static void attachRequest(final QuarkContext request) {
		if (Objects.nonNull(request)) {
			httpThreadLocal.set(request);
		}
	}

	/**
	 * Remove servlet request/response instances from current thread context
	 */
	public static void releaseRequest() {
		httpThreadLocal.remove();
	}

	/**
	 * Store current websocket instance to thread context
	 * @param session
	 */
	public static void attachSession(final WebSocketSession session) {
		if (Objects.nonNull(session)) {
			websocketContextThreadLocal.set(session);
		}
	}

	/**
	 * Remove current websocket instance from thread context
	 */
	public static void releaseSession() {
		websocketContextThreadLocal.remove();
	}

	/**
	 * Used from Async servlet to store for later injection for Async Controllers
	 * @param session
	 */
	public static void attachAsync(final QuarkAsyncContext session) {
		if (Objects.nonNull(session)) {
			asyncContextThreadLocal.set(session);
		}
	}

	/**
	 * Release servlet aync from current thread context 
	 */
	public static void releaseAsync() {
		asyncContextThreadLocal.remove();
	}

}
