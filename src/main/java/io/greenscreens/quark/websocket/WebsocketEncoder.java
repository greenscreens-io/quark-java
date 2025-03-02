/*
 * Copyright (C) 2015, 2023. Green Screens Ltd.
 */
package io.greenscreens.quark.websocket;

import jakarta.enterprise.inject.Vetoed;
import jakarta.websocket.EncodeException;
import jakarta.websocket.Encoder;
import jakarta.websocket.EndpointConfig;
import io.greenscreens.quark.security.IQuarkKey;
import io.greenscreens.quark.websocket.data.WebSocketResponse;

/**
 * Internal encoder for WebSocket ExtJS response
 */
@Vetoed
public class WebsocketEncoder implements Encoder.Text<WebSocketResponse> {

    IQuarkKey key = null;
    
    @Override
    public void init(final EndpointConfig config) {
        key = WebsocketUtil.key(config);
    }

    @Override
    public void destroy() {
        key = null; 
    }

	@Override
	public final String encode(final WebSocketResponse data) throws EncodeException {
		return WebsocketUtil.encode(data, key);
	}

}
