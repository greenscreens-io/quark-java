/*
 * Copyright (C) 2015, 2023 Green Screens Ltd.
 */
package io.greenscreens.quark.websocket;

import java.io.IOException;
import java.util.Objects;
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

		if (Objects.isNull(endpoint)) {
			LOG.warn("WebSocketEndpoint not injected. If running in servlet only container, CDI framework is needed.");
			QuarkUtil.close(session);
			return;
		}

		endpoint.onOpen(session, config);
		try {
			session.getBasicRemote().sendText("{\"msg\":\"WS4IS\"}");
		} catch (IOException e) {
			final String msg = QuarkUtil.toMessage(e);
			LOG.error(msg);
			LOG.debug(msg, e);
		}
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

}
