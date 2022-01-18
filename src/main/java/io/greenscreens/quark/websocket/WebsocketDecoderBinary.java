/*
 * Copyright (C) 2015, 2022 Green Screens Ltd.
 */
package io.greenscreens.quark.websocket;

import java.nio.ByteBuffer;

import javax.enterprise.inject.Vetoed;
import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.greenscreens.quark.JsonDecoder;
import io.greenscreens.quark.QuarkUtil;
import io.greenscreens.quark.websocket.data.WebSocketRequest;

/**
 * Internal JSON decoder for WebSocket ExtJS request
 */
@Vetoed
public class WebsocketDecoderBinary implements Decoder.Binary<WebSocketRequest> {

	private static final Logger LOG = LoggerFactory.getLogger(WebsocketDecoderBinary.class);

	
	@Override
	public boolean willDecode(final ByteBuffer buff) {
		return true;
	}
	
	
	@Override
	public WebSocketRequest decode(final ByteBuffer buffer) throws DecodeException {

		String message = null;
		WebSocketRequest wsMessage = null;

		try {
			message = QuarkUtil.ungzip(buffer);
			final JsonDecoder<WebSocketRequest> jd = new JsonDecoder<>(WebSocketRequest.class, message);
			wsMessage = jd.getObject();
			wsMessage.setBinary(true);			
		} catch (Exception e) {
			final String msg = QuarkUtil.toMessage(e);
			LOG.error(msg);
			LOG.debug(msg, e);
			throw new DecodeException(message, msg, e);
		}

		return wsMessage;
	}


	@Override
	public void destroy() {
		// not used
	}

	@Override
	public void init(final EndpointConfig arg0) {
		// not used
	}


}
