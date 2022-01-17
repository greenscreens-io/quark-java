/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 * 
 * https://www.greenscreens.io
 * 
 */
package io.greenscreens.quark.websocket;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

import javax.websocket.EncodeException;
import javax.websocket.server.HandshakeRequest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.greenscreens.quark.QuarkSecurity;
import io.greenscreens.quark.QuarkUtil;
import io.greenscreens.quark.websocket.data.IWebSocketResponse;
import io.greenscreens.quark.websocket.data.WebSocketInstruction;
import io.greenscreens.quark.JsonDecoder;
import io.greenscreens.quark.security.IAesKey;

/**
 * Internal encoder for WebSocket ExtJS response
 */
public enum WebsocketUtil {
	;

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

			final ObjectMapper mapper = JsonDecoder.getJSONEngine();

			if (mapper != null) {
				final IAesKey key = data.getKey();
				data.setKey(null);

				response = mapper.writeValueAsString(data);
				response = encrypt(response, key);
			}

		} catch (Exception e) {
			throw new EncodeException(data, e.getMessage(), e);
		}

		if (response == null) {
			response = "";
		}

		return response;
	}

	/**
	 * Encrypt data with AES for encrypted response
	 * 
	 * @param data
	 * @param crypt
	 * @return
	 * @throws Exception
	 */
	private static String encrypt(final String data, final IAesKey crypt) throws IOException {

		if (crypt == null) {
			return data;
		}

		final byte[] iv = QuarkSecurity.getRandom(crypt.getBlockSize());
		final String enc = crypt.encrypt(data, iv);
		final ObjectNode node = JsonNodeFactory.instance.objectNode();
		node.put("iv", QuarkUtil.bytesToHex(iv));
		node.put("d", enc);
		node.put("cmd", WebSocketInstruction.ENC.toString());
		return JsonDecoder.getJSONEngine().writeValueAsString(node);
	}

	/**
	 * Parse browser received cookie strings
	 * 
	 * @param cookies
	 * @return
	 */
	public static Map<String, String> parseCookies(final List<String> cookies) {


		if (cookies == null) {
			return Collections.emptyMap();
		}

		final Map<String, String> map = new HashMap<>();
		Scanner scan = null;
		String[] pair = null;

		for (String cookie : cookies) {

			try {

				scan = new Scanner(cookie);
				scan.useDelimiter(";");

				while (scan.hasNext()) {
					pair = scan.next().split("=");
					if (pair.length > 1) {
						map.put(pair[0], pair[1]);
					}
				}

			} finally {
				QuarkUtil.close(scan);
			}

		}

		return Collections.unmodifiableMap(map);
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

		if (params != null && !params.isEmpty()) {
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
		if (list == null || list.isEmpty()) {
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

		if (data != null) {
			data = data.split(";")[0];
			data = data.split(",")[0];
			locale = new Locale(data);
		}

		return locale;
	}

}
