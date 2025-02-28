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
import java.util.Optional;

import jakarta.websocket.EncodeException;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.server.HandshakeRequest;
import io.greenscreens.quark.internal.QuarkConstants;
import io.greenscreens.quark.security.IQuarkKey;
import io.greenscreens.quark.util.QuarkJson;
import io.greenscreens.quark.util.QuarkUtil;
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
    public static Optional<String> findHeader(final HandshakeRequest request, final String key) {
        return firstList(request.getHeaders(), key);
    }

    /**
     * Generic method to find URL query parameter
     * 
     * @param request
     * @param name
     * @return
     */
    public static Optional<String> findQuery(final HandshakeRequest request, final String key) {
        return firstList(request.getParameterMap(), key);
    }

    static Optional<List<String>> parameterMap(final HandshakeRequest request, final String key) {
        return mapList(request.getParameterMap(), key);
    }

    static Optional<List<String>> mapList(final Map<String, List<String>> store, final String key) {
        return Optional.ofNullable(store).map(m -> m.get(key));
    }

    static Optional<String> firstList(final Map<String, List<String>> store, final String key) {
        return mapList(store, key).map(list -> firstList(list).orElse(null));
    }

    static Optional<String> firstList(final List<String> store) {
        return Optional.ofNullable(store).map(l -> l.stream()).map(l -> l.findFirst().orElse(null));
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
        return WebsocketUtil.findHeader(request, "Accept-Language")
                .map(v -> v.split(";")[0])
                .map(v -> v.split(",")[0])
                .map(v -> Locale.of(v))
                .orElse(Locale.ENGLISH);
    }

}
