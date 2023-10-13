/*
 * Copyright (C) 2015, 2023. Green Screens Ltd.
 */
package io.greenscreens.quark.websocket;

import jakarta.enterprise.inject.Vetoed;
import jakarta.websocket.EncodeException;
import jakarta.websocket.Encoder;
import jakarta.websocket.EndpointConfig;

import io.greenscreens.quark.websocket.data.WebSocketResponse;

/**
 * Internal encoder for WebSocket ExtJS response
 */
@Vetoed
public class WebsocketEncoder implements Encoder.Text<WebSocketResponse> {

	@Override
	public void init(final EndpointConfig arg0) {
		// not used
	}

	@Override
	public void destroy() {
		// not used
	}

	@Override
	public final String encode(final WebSocketResponse data) throws EncodeException {
		return WebsocketUtil.encode(data);
	}

}
