/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 * 
 * https://www.greenscreens.io
 * 
 */
package io.greenscreens.quark.websocket;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import javax.enterprise.inject.Vetoed;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;

import io.greenscreens.quark.QuarkUtil;
import io.greenscreens.quark.web.QuarkConstants;
import io.greenscreens.quark.web.listener.SessionCollector;

/**
 * Config object for @ServerEndpoint annotation used to intercept WebSocket
 * initialization for custom setup.
 */
@Vetoed
public class WebSocketConfigurator extends ServerEndpointConfig.Configurator {

	private static final List<String> LANG;

	static {
		LANG = new ArrayList<>();
		LANG.add("UTF-8");
	}

	/**
	 * Find query parameter q, which contains request challenge
	 * 
	 * @param request
	 * @return
	 */
	String findChallenge(final HandshakeRequest request) {
		return WebsocketUtil.findQuery(request, "q");
	}

	String findCompression(final HandshakeRequest request) {
		return WebsocketUtil.findQuery(request, "c");
	}

	/**
	 * Find session link token based on custom pairing
	 * 
	 * @param request
	 * @return
	 */
	int findSessionToken(final HandshakeRequest request) {

		final List<String> cookies = request.getHeaders().get("cookie");
		final Map<String, String> map = WebsocketUtil.parseCookies(cookies);

		String val = map.get("X-Authorization");
		if (val == null) {
			val = WebsocketUtil.findQuery(request, "t");
		}

		return QuarkUtil.toInt(val);
	}

	/**
	 * Find http session attached to websocket
	 * 
	 * @param request
	 * @return
	 */
	HttpSession findSession(final HandshakeRequest request) {

		HttpSession httpSession = (HttpSession) request.getHttpSession();

		if (httpSession == null) {
			final int token = findSessionToken(request);
			httpSession = SessionCollector.get(token);
		}

		return httpSession;
	}

	
	@Override
	public final String getNegotiatedSubprotocol(final List<String> supported, final List<String> requested) {
		return QuarkConstants.QUARK_SUBPROTOCOL;
	}

	/**
	 * modifyHandshake() is called before getEndpointInstance()!
	 */
	@Override
	public void modifyHandshake(final ServerEndpointConfig sec, final HandshakeRequest request,
			final HandshakeResponse response) {

		super.modifyHandshake(sec, request, response);

		response.getHeaders().put("Accept-Language", LANG);

		final HttpSession httpSession = findSession(request);
		final String challenge = findChallenge(request);
		final String compression = findCompression(request);
		final Locale locale = WebsocketUtil.getLocale(request);
		final boolean isCompression = "true".equalsIgnoreCase(compression);
		
		WebSocketStorage.store(sec, QuarkConstants.QUARK_PATH, sec.getPath());
		WebSocketStorage.store(sec, QuarkConstants.QUARK_CHALLENGE, challenge);
		WebSocketStorage.store(sec, QuarkConstants.QUARK_COMPRESSION, isCompression);
		WebSocketStorage.store(sec, Locale.class.getCanonicalName(), locale);
		WebSocketStorage.store(sec, HttpSession.class.getCanonicalName(), httpSession);
		if (Objects.nonNull(httpSession)) {
			WebSocketStorage.store(sec, ServletContext.class.getCanonicalName(), httpSession.getServletContext());
		}
	}
	
	/**
	 * Store data to websocket user data
	 * 
	 * @param sec
	 * @param key
	 * @param value
	 */
	public void store(final ServerEndpointConfig sec, final String key, final Object value) {
		if (key != null && value != null) {
			sec.getUserProperties().put(key, value);
		}
	}


}
