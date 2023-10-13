/*
 * Copyright (C) 2015, 2023. Green Screens Ltd.
 */
package io.greenscreens.quark;

import java.lang.ScopedValue.Carrier;
import java.util.Objects;

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
		return getWebSession(true);
	}	

	/**
	 * Determine if current context is from HTTP request or WebSocket
	 * Based on that, returns session from proper thread context. 
	 * @param create
	 * @return
	 */
	public static HttpSession getWebSession(final boolean create) {		
		final QuarkContext ctx = httpScope.get();
		if (Objects.nonNull(ctx)) return ctx.getRequest().getSession(create);
		
		final WebSocketSession wss = websocketContextScope.get();
		if (Objects.nonNull(wss)) return wss.getHttpSession(); 
		
		final QuarkAsyncContext ctxa = asyncContextScope.get();
		if (Objects.nonNull(ctxa)) ctxa.getRequest().getSession(create);
		
		return null;		
	}
	
	/**
	 * Store servlet request/response instances to current thread context 
	 * @param request
	 */
	public static Carrier attachRequest(final QuarkContext request) {
		return Objects.nonNull(request) ? ScopedValue.where(httpScope, request) : null;
	}

	/**
	 * Store current websocket instance to thread context
	 * @param session
	 */
	public static Carrier attachSession(final WebSocketSession session) {
		return Objects.nonNull(session)  ? ScopedValue.where(websocketContextScope, session) : null;
	}

	/**
	 * Used from Async servlet to store for later injection for Async Controllers
	 * @param session
	 */
	public static Carrier attachAsync(final QuarkAsyncContext session) {
		return Objects.nonNull(session)  ? ScopedValue.where(asyncContextScope, session) : null;
	}

}
