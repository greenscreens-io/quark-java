/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 * 
 * https://www.greenscreens.io
 * 
 */
package io.greenscreens.quark.websocket;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.greenscreens.quark.QuarkUtil;
import io.greenscreens.quark.websocket.data.WebSocketResponseBinary;

/**
 * Internal encoder for WebSocket ExtJS response
 *
 */
public class WebsocketEncoderBinary implements Encoder.Binary<WebSocketResponseBinary> {

	private static final Logger LOG = LoggerFactory.getLogger(WebsocketEncoderBinary.class);

	@Override
	public final void destroy() {
		// not used
	}

	@Override
	public final void init(final EndpointConfig arg0) {
		// not used
	}

	@Override
	public final ByteBuffer encode(final WebSocketResponseBinary data) throws EncodeException {
		
		final String wsmsg = WebsocketUtil.encode(data);
		ByteBuffer buff = null;
		
		if (wsmsg != null && wsmsg.length() > 0) {
			
			try {
				if (data.isCompression()) {
					final byte [] bytes = QuarkUtil.gzip(wsmsg);
					buff = ByteBuffer.wrap(bytes);					
				} else {
					buff = ByteBuffer.wrap(wsmsg.getBytes(StandardCharsets.UTF_8));
				}
			} catch (Exception e) {
				final String msg = QuarkUtil.toMessage(e);
				LOG.error(msg);
				LOG.debug(msg, e);
				throw new EncodeException(data, msg);
			}
		}
		
		return buff;
	}
	
}
