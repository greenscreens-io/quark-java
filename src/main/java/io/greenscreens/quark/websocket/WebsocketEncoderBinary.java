/*
 * Copyright (C) 2015, 2023 Green Screens Ltd.
 */
package io.greenscreens.quark.websocket;

import java.io.IOException;
import java.nio.ByteBuffer;

import jakarta.enterprise.inject.Vetoed;
import jakarta.websocket.EncodeException;
import jakarta.websocket.Encoder;
import jakarta.websocket.EndpointConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.greenscreens.quark.security.IQuarkKey;
import io.greenscreens.quark.stream.QuarkStream;
import io.greenscreens.quark.util.QuarkUtil;
import io.greenscreens.quark.websocket.data.WebSocketInstruction;
import io.greenscreens.quark.websocket.data.WebSocketResponse;

/**
 * Internal encoder for WebSocket ExtJS response
 */
@Vetoed
public class WebsocketEncoderBinary implements Encoder.Binary<WebSocketResponse> {

	private static final Logger LOG = LoggerFactory.getLogger(WebsocketEncoderBinary.class);

    IQuarkKey key = null;
    boolean compression = false;        

    @Override
	public void init(final EndpointConfig config) {
        key = WebsocketUtil.key(config);
        compression = WebsocketUtil.isCompression(config);        
	}

	@Override
	public void destroy() {
	    key = null;
	    compression = false;        
	}
	
	@Override
	public final ByteBuffer encode(final WebSocketResponse data) throws EncodeException {
		
		ByteBuffer buff = null;
		ObjectNode node = null;
		
		try {
			final boolean isAPI = data.getCmd() == WebSocketInstruction.API ;			
			final String wsmsg = WebsocketUtil.encode(data, null);
			if (isAPI) node = (ObjectNode) data.getData();
			buff = QuarkStream.wrap(wsmsg, key, compression, node);
		} catch (IOException e) {
			final String msg = QuarkUtil.toMessage(e);
			LOG.error(msg);
			LOG.debug(msg, e);
		}
		
		return buff;
	}
	
}
