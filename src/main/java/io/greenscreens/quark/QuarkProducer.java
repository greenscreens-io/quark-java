/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 * 
 * https://www.greenscreens.io
 * 
 */
package io.greenscreens.quark;

import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.servlet.http.HttpSession;

import io.greenscreens.quark.async.QuarkAsyncContext;
import io.greenscreens.quark.web.QuarkContext;
import io.greenscreens.quark.websocket.WebSocketSession;

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
		return getWebSession(true);
	}	

	public static HttpSession getWebSession(final boolean create) {
		
		final QuarkContext ctx = httpThreadLocal.get();
		if (Objects.nonNull(ctx)) return ctx.getRequest().getSession(create);
		
		final WebSocketSession wss = websocketContextThreadLocal.get();
		if (Objects.nonNull(wss)) return wss.getHttpSession(); 
		
		final QuarkAsyncContext ctxa = asyncContextThreadLocal.get();
		if (Objects.nonNull(ctxa)) ctxa.getRequest().getSession(create);
		
		return null;		
	}
	
	public static void attachRequest(final QuarkContext request) {
		if (Objects.nonNull(request)) {
			httpThreadLocal.set(request);
		}
	}

	public static void releaseRequest() {
		httpThreadLocal.remove();
	}

	public static void attachSession(final WebSocketSession session) {
		if (Objects.nonNull(session)) {
			websocketContextThreadLocal.set(session);
		}
	}

	public static void releaseSession() {
		websocketContextThreadLocal.remove();
	}

	public static void attachAsync(final QuarkAsyncContext session) {
		if (Objects.nonNull(session)) {
			asyncContextThreadLocal.set(session);
		}
	}
	
	public static void releaseAsync() {
		asyncContextThreadLocal.remove();
	}

}
