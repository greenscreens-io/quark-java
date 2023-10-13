/*
 * Copyright (C) 2015, 2023. Green Screens Ltd.
 */
package io.greenscreens.quark.websocket;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import jakarta.enterprise.inject.Vetoed;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;
import jakarta.websocket.HandshakeResponse;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.ServerEndpointConfig;
import io.greenscreens.quark.internal.QuarkConstants;
import io.greenscreens.quark.security.IQuarkKey;
import io.greenscreens.quark.security.QuarkSecurity;
import io.greenscreens.quark.utils.QuarkUtil;
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
	public void modifyHandshake(final ServerEndpointConfig sec, final HandshakeRequest request,	final HandshakeResponse response) {

		final HttpSession httpSession = findSession(request);
		final String challenge = findChallenge(request);
		final String compression = findCompression(request);
		final String publicKey = findWebKey(request);

		final Locale locale = WebsocketUtil.getLocale(request);
		final boolean isCompression = "true".equalsIgnoreCase(compression);
		final IQuarkKey aesKey = QuarkSecurity.initWebKey(publicKey);

		super.modifyHandshake(sec, request, response);
		response.getHeaders().put("Accept-Language", LANG);	
		
		WebSocketStorage.store(sec, QuarkConstants.ENCRYPT_ENGINE, aesKey);
		WebSocketStorage.store(sec, QuarkConstants.QUARK_PATH, sec.getPath());
		WebSocketStorage.store(sec, QuarkConstants.QUARK_CHALLENGE, challenge);
		WebSocketStorage.store(sec, QuarkConstants.QUARK_COMPRESSION, isCompression);
		WebSocketStorage.store(sec, Locale.class, locale);
		WebSocketStorage.store(sec, HttpSession.class, httpSession);
		if (Objects.nonNull(httpSession)) {
			WebSocketStorage.store(sec, ServletContext.class, httpSession.getServletContext());
		}
	}
	
}
