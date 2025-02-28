/*
 * Copyright (C) 2015, 2023. Green Screens Ltd.
 */
package io.greenscreens.quark;

import java.lang.ScopedValue.Carrier;
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


	private static final ScopedValue<WebSocketSession> websocketContextScope = ScopedValue.newInstance();
	private static final ScopedValue<QuarkAsyncContext> asyncContextScope = ScopedValue.newInstance();
	private static final ScopedValue<QuarkContext> httpScope = ScopedValue.newInstance();
	
	@Produces
	public static WebSocketSession getWebSocketSession() {
		return websocketContextScope.get();
	}

	@Produces
	public static QuarkAsyncContext getQuarkAsyncContext() {
		return asyncContextScope.get();
	}

	@Produces
	public static QuarkContext getQuarkContext() {
		return httpScope.get();
	}

	@Produces
	public static HttpSession getWebSession() {
		return getWebSession(true).orElse(null);
	}	

    public static Optional<WebSocketSession> getWebSocketSessionSafe() {
        return Optional.ofNullable(websocketContextScope.get());
    }

    public static Optional<QuarkAsyncContext> getQuarkAsyncContextSafe() {
        return Optional.ofNullable(asyncContextScope.get());
    }

    public static Optional<QuarkContext> getQuarkContextSafe() {
        return Optional.ofNullable(httpScope.get());
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
    public static Optional<Carrier> attachRequest(final QuarkContext request) {
        return Optional.ofNullable(request).map(obj -> ScopedValue.where(httpScope, obj));
    }
    
	/**
	 * Store current websocket instance to thread context
	 * @param session
	 */
	public static Optional<Carrier> attachSession(final WebSocketSession session) {
		return Optional.ofNullable(session).map(obj -> ScopedValue.where(websocketContextScope, obj));
	}

	/**
	 * Used from Async servlet to store for later injection for Async Controllers
	 * @param session
	 */
	public static Optional<Carrier> attachAsync(final QuarkAsyncContext session) {
		return Optional.ofNullable(session).map(obj -> ScopedValue.where(asyncContextScope, obj));
	}

}
