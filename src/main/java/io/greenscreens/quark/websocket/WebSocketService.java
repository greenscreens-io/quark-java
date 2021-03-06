/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 * 
 * https://www.greenscreens.io
 * 
 */
package io.greenscreens.quark.websocket;

import java.io.Closeable;
import java.io.IOException;
import java.util.Optional;

import javax.inject.Inject;
import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.PongMessage;
import javax.websocket.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.greenscreens.quark.QuarkEngine;
import io.greenscreens.quark.QuarkUtil;
import io.greenscreens.quark.websocket.data.IWebSocketResponse;
import io.greenscreens.quark.websocket.data.WebSocketRequest;

/**
 * Base WebSocket endpoint with ExtJS support. Should not be used directly.
 * Create new class extending this one and annotate new class
 * with @ServerEndpoint
 */
public class WebSocketService {

	private static final Logger LOG = LoggerFactory.getLogger(WebSocketService.class);

	@Inject
	public WebSocketEndpoint endpoint;

	// getopensessions does not work across different endpoints
	public void broadcast(final IWebSocketResponse data) {
		WebSocketEndpoint.broadcast(data);
	}

	@OnMessage
	public void onMessage(final WebSocketRequest message, final Session session) {
		endpoint.onMessage(message, session);
	}

	@OnOpen
	public void onOpen(final Session session, final EndpointConfig config) {

		endpoint = Optional.ofNullable(endpoint).orElse(QuarkEngine.getBean(WebSocketEndpoint.class));

		if (endpoint == null) {
			LOG.warn("WebSocketEndpoint not injected. If running in servlet only container, CDI framework is needed.");
			close(session);
			return;
		}

		endpoint.onOpen(session, config);
	}

	@OnClose
	public void onClose(final Session session, final CloseReason reason) {
		endpoint.onClose(session, reason);
	}

	@OnError
	public void onError(final Session session, final Throwable t) {
		endpoint.onError(session, t);
	}

	@OnMessage
	public void onPongMessage(final PongMessage pong, final Session session) {
		// not used
	}

	private void close(Closeable closeable) {

		if (closeable == null)
			return;

		try {
			closeable.close();
		} catch (IOException e) {
			final String msg = QuarkUtil.toMessage(e);
			LOG.error(msg);
			LOG.debug(msg, e);
		}

	}

}
