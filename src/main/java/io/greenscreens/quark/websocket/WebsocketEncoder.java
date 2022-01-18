/*
 * Copyright (C) 2015, 2022 Green Screens Ltd.
 */
package io.greenscreens.quark.websocket;

import javax.enterprise.inject.Vetoed;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

import io.greenscreens.quark.websocket.data.WebSocketResponse;

/**
 * Internal encoder for WebSocket ExtJS response
 */
@Vetoed
public class WebsocketEncoder implements Encoder.Text<WebSocketResponse> {

	@Override
	public final void destroy() {
		// not used
	}

	@Override
	public final void init(final EndpointConfig arg0) {
		// not used
	}

	@Override
	public final String encode(final WebSocketResponse data) throws EncodeException {
		return WebsocketUtil.encode(data);
	}

}
