/*
 * Copyright (C) 2015, 2023. Green Screens Ltd.
 */
package io.greenscreens.quark.websocket;

import java.io.IOException;
import java.nio.ByteBuffer;

import jakarta.enterprise.inject.Vetoed;
import jakarta.websocket.DecodeException;
import jakarta.websocket.Decoder;
import jakarta.websocket.EndpointConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.greenscreens.quark.security.IQuarkKey;
import io.greenscreens.quark.stream.QuarkStream;
import io.greenscreens.quark.utils.QuarkUtil;
import io.greenscreens.quark.websocket.data.WebSocketRequest;

/**
 * Internal JSON decoder for WebSocket ExtJS request
 */
@Vetoed
public class WebsocketDecoderBinary implements Decoder.Binary<WebSocketRequest> {

	private static final Logger LOG = LoggerFactory.getLogger(WebsocketDecoderBinary.class);
	
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
	public boolean willDecode(final ByteBuffer buffer) {
		return QuarkStream.isGSStream(buffer);
	}

	@Override
	public WebSocketRequest decode(final ByteBuffer buffer) throws DecodeException {		
		try {
			final ByteBuffer data = QuarkStream.unwrap(buffer, key);
			final String message = QuarkStream.asString(data);
			return WebsocketUtil.decode(message);
		} catch (IOException e) {
			final String msg = QuarkUtil.toMessage(e);
			LOG.error(msg);
			LOG.debug(msg, e);
			throw new DecodeException(buffer, msg, e);
		}
	}
	
}
