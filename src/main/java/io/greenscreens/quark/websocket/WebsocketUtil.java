/*
 * Copyright (C) 2015, 2023. Green Screens Ltd.
 */
package io.greenscreens.quark.websocket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import jakarta.websocket.EncodeException;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.server.HandshakeRequest;
import io.greenscreens.quark.internal.QuarkConstants;
import io.greenscreens.quark.security.IQuarkKey;
import io.greenscreens.quark.utils.QuarkJson;
import io.greenscreens.quark.utils.QuarkUtil;
import io.greenscreens.quark.web.QuarkCookieUtil;
import io.greenscreens.quark.websocket.data.IWebSocketResponse;
import io.greenscreens.quark.websocket.data.WebSocketRequest;

/**
 * Internal encoder for WebSocket ExtJS response
 */
public enum WebsocketUtil {
	;
	
	final static IQuarkKey key(final EndpointConfig config) {
		return WebSocketStorage.get(config, QuarkConstants.ENCRYPT_ENGINE, null);		
	}
	
	final static boolean isCompression(final EndpointConfig config) {
		return WebSocketStorage.get(config, QuarkConstants.QUARK_COMPRESSION, false);		
	}
	
	final static WebSocketRequest decode(final ByteBuffer buffer) throws IOException {
		final String message = new String(buffer.array(), StandardCharsets.UTF_8);
		return decode(message);
	}
	
	final static WebSocketRequest decode(final String message) throws IOException {
		return QuarkJson.parse(WebSocketRequest.class, message);
	}
	
	/**
	 * Encrypt message for websocket response
	 * 
	 * @param data
	 * @return
	 * @throws EncodeException
	 */
	static String encode(final IWebSocketResponse data) throws EncodeException {

		String response = null;

		try {
			response = QuarkJson.stringify(data);
		} catch (Exception e) {
			throw new EncodeException(data, e.getMessage(), e);
		}

		return QuarkUtil.normalize(response);
	}

	static boolean isJson(final String message) {

		boolean sts = false;

		if (QuarkUtil.nonEmpty(message)) {
			sts = message.trim().startsWith("{") && message.trim().endsWith("}");
		}

		return sts;
	}

	/**
	 * Parse browser received cookie strings
	 * 
	 * @param cookies
	 * @return
	 */
	public static Map<String, String> parseCookies(final List<String> cookies) {
		return QuarkCookieUtil.parseCookies(cookies);
	}

	/**
	 * Get request header from websocket
	 * 
	 * @param request
	 * @param key
	 * @return
	 */
	public static String findHeader(final HandshakeRequest request, final String key) {

		final Map<String, List<String>> map = request.getHeaders();
		final List<String> params = map.get(key);

		if (Objects.nonNull(params) && !params.isEmpty()) {
			return params.get(0);
		}

		return null;
	}

	/**
	 * Generic method to find URL query parameter
	 * @param request
	 * @param name
	 * @return
	 */
	public static String findQuery(final HandshakeRequest request, final String name) {
		final List<String> list = request.getParameterMap().get(name);
		if (Objects.isNull(list) || list.isEmpty()) {
			return null;
		}
		return list.get(0);
	}

	/**
	 * Store current browser locale
	 * 
	 * Accept-Language:hr,en-US;q=0.8,en;q=0.6
	 * 
	 * @param request
	 * @return
	 */
	public static Locale getLocale(final HandshakeRequest request) {

		String data = WebsocketUtil.findHeader(request, "Accept-Language");
		Locale locale = Locale.ENGLISH;

		if (Objects.nonNull(data)) {
			data = data.split(";")[0];
			data = data.split(",")[0];
			locale = Locale.of(data);
		}

		return locale;
	}

}
