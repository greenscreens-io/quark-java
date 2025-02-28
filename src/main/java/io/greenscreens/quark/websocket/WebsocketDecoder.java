/*
 * Copyright (C) 2015, 2023. Green Screens Ltd.
 */
package io.greenscreens.quark.websocket;

import jakarta.enterprise.inject.Vetoed;
import jakarta.websocket.DecodeException;
import jakarta.websocket.Decoder;
import jakarta.websocket.EndpointConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.greenscreens.quark.util.QuarkUtil;
import io.greenscreens.quark.websocket.data.WebSocketRequest;

/**
 * Internal JSON decoder for WebSocket ExtJS request
 */
@Vetoed
public class WebsocketDecoder implements Decoder.Text<WebSocketRequest> {

	private static final Logger LOG = LoggerFactory.getLogger(WebsocketDecoder.class);
	
	@Override
	public void init(final EndpointConfig arg0) {
		// not used
	}

	@Override
	public void destroy() {
		// not used
	}

	@Override
	public final WebSocketRequest decode(final String message) throws DecodeException {

		WebSocketRequest wsMessage = null;
		LOG.trace("WebSocket request {}", message);

		try {
			wsMessage = WebsocketUtil.decode(message);
		} catch (Exception e) {
			final String msg = QuarkUtil.toMessage(e);
			LOG.error(msg);
			LOG.debug(msg, e);
			throw new DecodeException(message, msg, e);
		}

		return wsMessage;
	}

	@Override
	public final boolean willDecode(final String message) {
		return WebsocketUtil.isJson(message);
	}

}
