/*
 * Copyright (C) 2015, 2023 Green Screens Ltd.
 */
package io.greenscreens.quark.websocket;

import java.io.IOException;
import java.nio.ByteBuffer;

import javax.enterprise.inject.Vetoed;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.greenscreens.quark.IQuarkKey;
import io.greenscreens.quark.QuarkStream;
import io.greenscreens.quark.QuarkUtil;
import io.greenscreens.quark.websocket.data.WebSocketResponse;

/**
 * Internal encoder for WebSocket ExtJS response
 */
@Vetoed
public class WebsocketEncoderBinary implements Encoder.Binary<WebSocketResponse> {

	private static final Logger LOG = LoggerFactory.getLogger(WebsocketEncoderBinary.class);

	EndpointConfig config = null;

	@Override
	public void init(final EndpointConfig cfg) {
		config = cfg;
	}

	@Override
	public void destroy() {
		config = null;
	}
	
	@Override
	public final ByteBuffer encode(final WebSocketResponse data) throws EncodeException {
		
		ByteBuffer buff = null;
		
		try {
			final String wsmsg = WebsocketUtil.encode(data);
			final IQuarkKey key = WebsocketUtil.key(config);
			final boolean compression = WebsocketUtil.isCompression(config);
			buff = QuarkStream.wrap(wsmsg, key, compression);
		} catch (IOException e) {
			final String msg = QuarkUtil.toMessage(e);
			LOG.error(msg);
			LOG.debug(msg, e);
		}
		
		return buff;
	}
	
}
