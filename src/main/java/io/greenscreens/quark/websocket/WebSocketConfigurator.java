/*
 * Copyright (C) 2015, 2022 Green Screens Ltd.
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
import io.greenscreens.quark.security.IAesKey;
import io.greenscreens.quark.security.Security;
import io.greenscreens.quark.web.QuarkConstants;
import io.greenscreens.quark.web.listener.QuarkWebSessionListener;

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

	Map<String, String> coookies(final HandshakeRequest request) {
		final List<String> cookies = request.getHeaders().get("cookie");
		return WebsocketUtil.parseCookies(cookies);	
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
	 * ECDH browser public key
	 * @param request
	 * @return
	 */
	String findWebKey(final HandshakeRequest request) {
		return coookies(request).get(QuarkConstants.WEB_KEY);
	}
	
	/**
	 * Find session link token based on custom pairing
	 * 
	 * @param request
	 * @return
	 */
	int findSessionToken(final HandshakeRequest request) {

		final Map<String, String> map = coookies(request);

		String val = map.get("X-Authorization");
		if (QuarkUtil.nonEmpty(val)) {
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

		if (Objects.isNull(httpSession)) {
			final int token = findSessionToken(request);
			httpSession = QuarkWebSessionListener.get(token);
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
		final String publicKey = findWebKey(request);
		
		final Locale locale = WebsocketUtil.getLocale(request);
		final boolean isCompression = "true".equalsIgnoreCase(compression);
		final IAesKey aesKey = Security.initWebKey(publicKey);
		
		WebSocketStorage.store(sec, QuarkConstants.ENCRYPT_ENGINE, aesKey);
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
		if (QuarkUtil.nonEmpty(key)  && Objects.nonNull(value)) {
			sec.getUserProperties().put(key, value);
		}
	}


}
