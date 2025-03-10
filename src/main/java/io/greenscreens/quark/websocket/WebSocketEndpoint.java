/*
 * Copyright (C) 2015, 2023. Green Screens Ltd.
 */
package io.greenscreens.quark.websocket;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.greenscreens.quark.QuarkProducer;
import io.greenscreens.quark.cdi.BeanManagerUtil;
import io.greenscreens.quark.ext.ExtJSDirectRequest;
import io.greenscreens.quark.internal.QuarkBuilder;
import io.greenscreens.quark.internal.QuarkConstants;
import io.greenscreens.quark.internal.QuarkHandler;
import io.greenscreens.quark.util.QuarkUtil;
import io.greenscreens.quark.websocket.data.IWebSocketResponse;
import io.greenscreens.quark.websocket.data.WebSocketInstruction;
import io.greenscreens.quark.websocket.data.WebSocketRequest;
import io.greenscreens.quark.websocket.data.WebSocketResponse;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.websocket.CloseReason;
import jakarta.websocket.CloseReason.CloseCodes;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.Session;

/**
 * Internal CDI injectable object used by WebSocket endpoint instance. Used to
 * separate internal logic from WebSocketService.
 */
public class WebSocketEndpoint {

	private static final Logger LOG = LoggerFactory.getLogger(WebSocketEndpoint.class);
	private static final String MSG_HTTP_SEESION_REQUIRED = "WebSocket requires valid http session";

	private static final Collection<WebSocketSession> sessions = new ConcurrentSkipListSet<>();

	@Inject
	BeanManagerUtil beanManagerUtil;
	
	@Inject
	Event<WebsocketEvent> webSocketEvent;

	public WebSocketEndpoint() {
		super();
	}

	/**
	 * Send messages to all connected parties
	 * 
	 * @param message
	 */
	public static void broadcast(final IWebSocketResponse message) {
	    
	    LOG.trace("Broadcasting message {}", message);
	    
	    for (WebSocketSession session : sessions) {
	        session.sendResponse(message, true);
	        LOG.trace("  for {}", session);
	    }
	}

	/*
	 * PUBLIC SECTION
	 */
	
	public void onMessage(final WebSocketRequest message, final Session session) {

		WebSocketSession wsession = null;

		try {

			if (!QuarkConstants.MESSAGE_TYPE.equals(message.getType())) {
				return;
			}

			LOG.trace("Received message {} \n      for session : {}", message, session);
			
			wsession = toInstance(session);

			QuarkProducer.attachSession(wsession);

			final WebSocketInstruction cmd = message.getCmd();
			if (Objects.isNull(cmd)) {
				LOG.warn("No message instruction >> {}", message);	
				return;
			}
			
			if (cmd.isSimple()) {
				processSimple(wsession, message);
			} else {
				processData(wsession, message);
			} 

		} catch (Exception e) {

			final String msg = QuarkUtil.toMessage(e);
			LOG.error(msg);
			LOG.debug(msg, e);

			if (Objects.nonNull(wsession)) {
			    final IWebSocketResponse resp = WebSocketResponse.asError();
				wsession.sendResponse(resp, true);
			}
		}
	}

	// allow this websocket endpoint only for clients with valid session attached
	public void onOpen(final Session session, final EndpointConfig config) {

		try {
			
			LOG.trace("Openning new WebSocket connection : {} ", session);
			
            final WebSocketSession wsession = new WebSocketSession(session);
            WebSocketStorage.store(session, wsession);

			final Boolean requireSession = wsession.get(QuarkConstants.QUARK_SESSION);

			// disable websocket session timeout due to inactivity
			session.setMaxIdleTimeout(0);

			final Object path = WebSocketStorage.get(config, QuarkConstants.QUARK_PATH);
			wsession.set(QuarkConstants.QUARK_PATH, path);

			QuarkProducer.attachSession(wsession);

			boolean allowed = true;
			String reason = null;

			// wsOperations.setRequiredSession(requireSession);

			if (Objects.nonNull(requireSession) && requireSession.booleanValue() && !wsession.isValidHttpSession()) {
				allowed = false;
				reason = MSG_HTTP_SEESION_REQUIRED;
			}

			if (allowed) {
				webSocketEvent.fire(new WebsocketEvent(wsession, WebSocketEventStatus.START));
			} else {
				LOG.error(reason);
				session.close(new CloseReason(CloseCodes.PROTOCOL_ERROR, reason));
			}

			updateSessions(wsession);

			if (wsession.contains(QuarkConstants.QUARK_CHALLENGE)) {
				sendAPI(wsession);	
			}
			
		} catch (IOException e) {
			final String msg = QuarkUtil.toMessage(e);
			LOG.error(msg);
			LOG.debug(msg, e);
		}

	}

	public void onClose(final Session session, final CloseReason reason) {

	    final WebSocketSession wsession = toInstance(session);
		
		LOG.warn("Closing WebSocket session with reason code : {}, Session: {}", reason.getCloseCode().getCode(), wsession);

		try {

			QuarkProducer.attachSession(wsession);
			WebsocketEvent event = new WebsocketEvent(wsession, WebSocketEventStatus.CLOSE, reason);
			webSocketEvent.fire(event);

		} finally {
			updateSessions(wsession);
		}

	}

	public void onError(final Session session, final Throwable throwable) {

        final WebSocketSession wsession = toInstance(session);
		final String msg = QuarkUtil.toMessage(throwable);

		LOG.error("WebSocket error for session : {},  Message: {}", wsession, msg);

		QuarkProducer.attachSession(wsession);
		webSocketEvent.fire(new WebsocketEvent(wsession, WebSocketEventStatus.ERROR, throwable));
	}

	/*
	 * PRIVATE SECTION
	 */

	 private WebSocketSession toInstance(final Session session) {
	     return Optional.ofNullable(WebSocketStorage.get(session, WebSocketSession.class))
	             .orElse(new WebSocketSession(session));
	 }
	        
	private void updateSessions(final WebSocketSession session) {

		if (!session.isOpen()) {
			sessions.remove(session);
		} else if (!sessions.contains(session)) {
			sessions.add(session);
		}
	}
	
	/**
	 * Generate a JSON definition structure of exposed Controllers.
	 * Used by front Quark Engine to generate JavAScript objects and calls.
	 * @param session
	 * @return
	 */
	private boolean sendAPI(final WebSocketSession session) {
		
		if (Objects.isNull(beanManagerUtil)) return false;

		
		if (!session.contains(QuarkConstants.QUARK_CHALLENGE)) {
			return false;
		}
		
		final String challenge = session.get(QuarkConstants.QUARK_CHALLENGE);
		final WebSocketResponse wsResponse = new WebSocketResponse(WebSocketInstruction.API);		
		
		final ArrayNode api = beanManagerUtil.getAPI();
		final ObjectNode root = QuarkBuilder.buildAPI(api, challenge); 
		wsResponse.setData(root);		
			
		session.sendResponse(wsResponse);
		return true;
	}

	/**
	 * Process single request
	 * @param session
	 * @param message
	 */
	private void processSimple(final WebSocketSession session, final WebSocketRequest message) {
		
		final WebSocketInstruction cmd = message.getCmd();
				
		if (WebSocketInstruction.API == cmd && Objects.nonNull(beanManagerUtil)) {
			sendAPI(session);
		}
			
		final IWebSocketResponse response = WebSocketResponse.create(cmd);
		session.sendResponse(response, true);	

	}

	/**
	 * Process array of requests.
	 * @param encrypted
	 * @param session
	 * @param wsMessage
	 */
	private void processData(final WebSocketSession session, final WebSocketRequest wsMessage) {

		final List<ExtJSDirectRequest<JsonNode>> requests = wsMessage.getData();
		
		for (final ExtJSDirectRequest<JsonNode> request : requests) {
			QuarkHandler.call(session, request);
		}
		
	}
}
