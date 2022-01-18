/*
 * Copyright (C) 2015, 2022 Green Screens Ltd.
 */
package io.greenscreens.quark.websocket;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentSkipListSet;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.servlet.http.HttpSession;
import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCode;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.greenscreens.quark.QuarkProducer;
import io.greenscreens.quark.QuarkUtil;
import io.greenscreens.quark.cdi.BeanManagerUtil;
import io.greenscreens.quark.ext.ExtJSDirectRequest;
import io.greenscreens.quark.ext.ExtJSResponse;
import io.greenscreens.quark.web.QuarkConstants;
import io.greenscreens.quark.web.QuarkHandler;
import io.greenscreens.quark.websocket.data.IWebSocketResponse;
import io.greenscreens.quark.websocket.data.WebSocketInstruction;
import io.greenscreens.quark.websocket.data.WebSocketRequest;
import io.greenscreens.quark.websocket.data.WebSocketResponse;
import io.greenscreens.quark.websocket.data.WebSocketResponseFactory;

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

		if (Objects.nonNull(sessions)) {

			LOG.trace("Broadcasting message {}", message);

			for (WebSocketSession session : sessions) {
				session.sendResponse(message, true);
				LOG.trace("  for {}", session);
			}

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

			wsession = new WebSocketSession(session);
			QuarkProducer.attachSession(wsession);

			final WebSocketInstruction cmd = message.getCmd();
			if (cmd == null) {
				LOG.warn("No message instruction >> {}", message);	
				return;
			}
			
			if (cmd.isSimple()) {
				processSimple(wsession, message);
			} else if (cmd == WebSocketInstruction.ENC) {
				processData(true, wsession, message);
			} else if (cmd == WebSocketInstruction.DATA) {
				processData(false, wsession, message);
			} else {
				LOG.warn("Invalid message instruction >> {}", message);
			}
			
			// ECLIPSE COMPILER ISSUE
			/* 
			switch (cmd) {
			case WELCO:
			case API:
			case ECHO:
			case PING:
			case BYE:
				processSimple(wsession, message);
				break;
			case ENC:
				processData(true, wsession, message);
				break;
			case DATA:
				processData(false, wsession, message);
				break;
			case ERR:
				break;
			default:
				break;
			}
			*/

		} catch (Exception e) {

			final String msg = QuarkUtil.toMessage(e);
			LOG.error(msg);
			LOG.debug(msg, e);

			if (Objects.nonNull(wsession)) {
				final boolean isCompression = wsession.get(QuarkConstants.QUARK_COMPRESSION);
				wsession.sendResponse(getErrorResponse(e, message.isBinary(), isCompression), true);
			}

		} finally {
			QuarkProducer.releaseSession();
		}
	}

	// allow this websocket endpoint only for clients with valid session attached
	public void onOpen(final Session session, final EndpointConfig config) {

		try {

			LOG.trace("Openning new WebSocket connection : {} ", session);

			final HttpSession httpSession = WebSocketStorage.get(config, HttpSession.class);
			final WebSocketSession wsession = new WebSocketSession(session, httpSession);

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
				final CloseCode closeCode = new CloseCodeImpl();
				session.close(new CloseReason(closeCode, ""));
			}

			updateSessions(wsession);

			if (wsession.contains(QuarkConstants.QUARK_CHALLENGE)) {
				sendAPI(wsession);	
			}
			
		} catch (IOException e) {
			final String msg = QuarkUtil.toMessage(e);
			LOG.error(msg);
			LOG.debug(msg, e);
		} finally {
			QuarkProducer.releaseSession();
		}

	}

	public void onClose(final Session session, final CloseReason reason) {

		final WebSocketSession wsession = new WebSocketSession(session);
		LOG.warn("Closing WebSocket session with reason code : {}, Session: {}", reason.getCloseCode().getCode(), wsession);

		try {

			QuarkProducer.attachSession(wsession);
			WebsocketEvent event = new WebsocketEvent(wsession, WebSocketEventStatus.CLOSE, reason);
			webSocketEvent.fire(event);

		} finally {

			QuarkProducer.releaseSession();
			updateSessions(wsession);

		}

	}

	public void onError(final Session session, final Throwable throwable) {

		final WebSocketSession wsession = new WebSocketSession(session);

		final String msg = QuarkUtil.toMessage(throwable);

		LOG.error("WebSocket error for session : {},  Message: {}", wsession, msg);

		try {

			QuarkProducer.attachSession(wsession);
			webSocketEvent.fire(new WebsocketEvent(wsession, WebSocketEventStatus.ERROR, throwable));

		} finally {
			QuarkProducer.releaseSession();
		}
	}

	/*
	 * PRIVATE SECTION
	 */

	private void updateSessions(final WebSocketSession session) {

		if (!session.isOpen()) {
			sessions.remove(session);
		} else if (!sessions.contains(session)) {
			sessions.add(session);
		}
	}

	private IWebSocketResponse getErrorResponse(final Exception exception, final boolean isBinary, final boolean isCompression) {

		final ExtJSResponse response = new ExtJSResponse(exception, exception.getMessage());
		final IWebSocketResponse wsResponse = WebSocketResponseFactory.createAsError(isBinary, isCompression);
		wsResponse.setData(response);
		wsResponse.setErrMsg(exception.getMessage());
		return wsResponse;
	}
	
	/**
	 * Generate a JSON definition structure of exposed Controllers.
	 * Used by front Quark Engine to generate JavAScript objects and calls.
	 * @param session
	 * @return
	 */
	private boolean sendAPI(final WebSocketSession session) {
		
		if (Objects.nonNull(beanManagerUtil)) return false;

		
		if (!session.contains(QuarkConstants.QUARK_CHALLENGE)) {
			return false;
		}
		
		final String challenge = session.get(QuarkConstants.QUARK_CHALLENGE);
		final WebSocketResponse wsResposne = new WebSocketResponse(WebSocketInstruction.API);		
		
		final ArrayNode api = beanManagerUtil.getAPI();
		final ObjectNode root = QuarkUtil.buildAPI(api, challenge); 
		wsResposne.setData(root);		
		
		session.sendResponse(wsResposne, true);
		return true;
	}

	/**
	 * Process single request
	 * @param session
	 * @param message
	 */
	private void processSimple(final WebSocketSession session, final WebSocketRequest message) {
		
		final WebSocketInstruction cmd = message.getCmd();
				
		if (WebSocketInstruction.API == cmd && beanManagerUtil != null) {
			sendAPI(session);
		}

		final boolean isCompression = session.get(QuarkConstants.QUARK_COMPRESSION);		
		final IWebSocketResponse response = WebSocketResponseFactory.create(cmd, message.isBinary(), isCompression);
		session.sendResponse(response, true);	

	}

	/**
	 * Process array of requests.
	 * @param encrypted
	 * @param session
	 * @param wsMessage
	 */
	private void processData(final boolean encrypted, final WebSocketSession session, final WebSocketRequest wsMessage) {

		final List<ExtJSDirectRequest<JsonNode>> requests = wsMessage.getData();
		
		for (final ExtJSDirectRequest<JsonNode> request : requests) {
			QuarkHandler.call(session, request, wsMessage.isBinary(), encrypted);
		}
		
	}
	
	public static final class CloseCodeImpl implements CloseCode {
		@Override
		public int getCode() {
			return 4000;
		}
	}
}
