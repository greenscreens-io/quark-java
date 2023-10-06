/*
 * Copyright (C) 2015, 2023 Green Screens Ltd.
 */
package io.greenscreens.quark.websocket;

import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.greenscreens.quark.QuarkEngine;
import io.greenscreens.quark.utils.QuarkUtil;
import io.greenscreens.quark.websocket.data.IWebSocketResponse;
import io.greenscreens.quark.websocket.data.WebSocketRequest;
import io.greenscreens.quark.websocket.heartbeat.HeartbeatService;
import jakarta.inject.Inject;
import jakarta.websocket.CloseReason;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.PongMessage;
import jakarta.websocket.Session;


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
		HeartbeatService.updateSession(session);
		if (Objects.nonNull(endpoint)) endpoint.onMessage(message, session);
	}

	@OnOpen
	public void onOpen(final Session session, final EndpointConfig config) {

		HeartbeatService.registerSession(session);
		endpoint = Optional.ofNullable(endpoint).orElse(QuarkEngine.getBean(WebSocketEndpoint.class));

		if (Objects.isNull(endpoint)) {
			LOG.warn("WebSocketEndpoint not injected. If running in servlet only container, CDI framework is needed.");
			QuarkUtil.close(session);
			return;
		}

		endpoint.onOpen(session, config);
		HeartbeatService.registerSession(session);
	}

	@OnClose
	public void onClose(final Session session, final CloseReason reason) {
		HeartbeatService.deregisterSession(session);
		if (Objects.nonNull(endpoint)) endpoint.onClose(session, reason);
	}

	@OnError
	public void onError(final Session session, final Throwable t) {
		if (Objects.nonNull(endpoint)) {
			endpoint.onError(session, t);			
		} else {
			final String msg = QuarkUtil.toMessage(t);
			LOG.error(msg);
			LOG.debug(msg, t);			
		}
	}

	@OnMessage
	public void onPongMessage(final PongMessage pong, final Session session) {
		HeartbeatService.handlePong(session);
	}

}
