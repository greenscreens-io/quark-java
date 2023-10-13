/*
 * Copyright (C) 2015, 2023. Green Screens Ltd.
 */
package io.greenscreens.quark.websocket;

import java.io.IOException;
import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;
import jakarta.websocket.CloseReason;
import jakarta.websocket.CloseReason.CloseCodes;
import jakarta.websocket.EncodeException;
import jakarta.websocket.Extension;
import jakarta.websocket.MessageHandler;
import jakarta.websocket.MessageHandler.Partial;
import jakarta.websocket.MessageHandler.Whole;
import jakarta.websocket.RemoteEndpoint.Async;
import jakarta.websocket.RemoteEndpoint.Basic;
import jakarta.websocket.Session;
import jakarta.websocket.WebSocketContainer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.greenscreens.quark.internal.QuarkConstants;
import io.greenscreens.quark.utils.QuarkUtil;
import io.greenscreens.quark.websocket.data.IWebSocketResponse;
import io.greenscreens.quark.websocket.data.WebSocketInstruction;
import io.greenscreens.quark.websocket.data.WebSocketResponse;

/**
 * Class for holding WebSocket session data. Purpose of this class is similar to
 * HttpSession
 */
public class WebSocketSession implements Session, Comparable<WebSocketSession> {

	private static final Logger LOG = LoggerFactory.getLogger(WebSocketSession.class);

	private final Session session;

	public WebSocketSession(final Session session) {
		super();
		this.session = session;
	}

	public WebSocketSession(final Session session, final HttpSession httpSession) {

		this.session = session;

		if (Objects.nonNull(httpSession)) {
			WebSocketStorage.store(session, httpSession);
		}
	}

	@Override
	public final void addMessageHandler(final MessageHandler arg0) {
		session.addMessageHandler(arg0);
	}

	@Override
	public final void close() throws IOException {

		if (session.isOpen()) {

			final IWebSocketResponse response = new WebSocketResponse(WebSocketInstruction.BYE);

			try {
				session.getBasicRemote().sendObject(response);
			} catch (EncodeException e) {
				final String msg = QuarkUtil.toMessage(e);
				LOG.error(msg);
				LOG.debug(msg, e);
			}
			close(new CloseReason(CloseCodes.NORMAL_CLOSURE, ""));
		}
	}

	@Override
	public final void close(final CloseReason arg0) throws IOException {
		if (session.isOpen()) {
			session.close(arg0);
		}
	}

	public final ServletContext getContext() {
		ServletContext ctx = WebSocketStorage.get(this, ServletContext.class);
		if (Objects.isNull(ctx)) {
			final HttpSession httpSession = getHttpSession();
			if (Objects.nonNull(httpSession)) {
				ctx = httpSession.getServletContext();
			}
		}
		return ctx;
	}

	/**
	 * Send response to a client
	 * @param wsResponse
	 * @param async
	 * @return
	 */
	public final boolean sendResponse(final IWebSocketResponse wsResponse, final boolean async) {

		if (Objects.isNull(wsResponse)) return false;

		if (!session.isOpen()) {
			LOG.warn("Websocket response not sent, session is closed for {}!", this);
			try {
				close(new CloseReason(CloseCodes.CANNOT_ACCEPT, ""));
			} catch (IOException e) {
				LOG.warn(e.getMessage());
				LOG.debug(e.getMessage(), e);
			}
			return false;
		}

		boolean success = true;

		try {		
			if (async) {
				session.getAsyncRemote().sendObject(wsResponse);
			} else {
				session.getBasicRemote().sendObject(wsResponse);
			}
		} catch (IllegalStateException e) {
			// session invalidated
			LOG.warn(e.getMessage());
			LOG.debug(e.getMessage(), e);
			success = false;
		} catch (Exception e) {
			success = false;
			final String msg = QuarkUtil.toMessage(e);
			LOG.error(msg);
			LOG.debug(msg, e);
		}

		return success;
	}

	@Override
	public final WebSocketContainer getContainer() {
		return session.getContainer();
	}

	@Override
	public final String getId() {
		return session.getId();
	}

	@Override
	public final int getMaxBinaryMessageBufferSize() {
		return session.getMaxBinaryMessageBufferSize();
	}

	@Override
	public final long getMaxIdleTimeout() {
		return session.getMaxIdleTimeout();
	}

	@Override
	public final int getMaxTextMessageBufferSize() {
		return session.getMaxTextMessageBufferSize();
	}

	@Override
	public final Set<MessageHandler> getMessageHandlers() {
		return session.getMessageHandlers();
	}

	@Override
	public final List<Extension> getNegotiatedExtensions() {
		return session.getNegotiatedExtensions();
	}

	@Override
	public final String getNegotiatedSubprotocol() {
		return session.getNegotiatedSubprotocol();
	}

	@Override
	public final Set<Session> getOpenSessions() {
		return session.getOpenSessions();
	}

	@Override
	public final Map<String, String> getPathParameters() {
		return session.getPathParameters();
	}

	@Override
	public final String getProtocolVersion() {
		return session.getProtocolVersion();
	}

	@Override
	public final String getQueryString() {
		return session.getQueryString();
	}

	@Override
	public final Map<String, List<String>> getRequestParameterMap() {
		return session.getRequestParameterMap();
	}

	@Override
	public final URI getRequestURI() {
		return session.getRequestURI();
	}

	@Override
	public final Principal getUserPrincipal() {
		return session.getUserPrincipal();
	}

	@Override
	public final Map<String, Object> getUserProperties() {
		return session.getUserProperties();
	}

	@Override
	public final boolean isOpen() {
		return session.isOpen();
	}

	@Override
	public final boolean isSecure() {
		return session.isSecure();
	}

	@Override
	public final void removeMessageHandler(final MessageHandler arg0) {
		session.removeMessageHandler(arg0);
	}

	@Override
	public final void setMaxBinaryMessageBufferSize(final int arg0) {
		session.setMaxBinaryMessageBufferSize(arg0);
	}

	@Override
	public final void setMaxIdleTimeout(final long arg0) {
		session.setMaxIdleTimeout(arg0);
	}

	@Override
	public final void setMaxTextMessageBufferSize(final int arg0) {
		session.setMaxTextMessageBufferSize(arg0);
	}
	
	/**
	 * Check if socket storage has data by key
	 * @param key
	 * @return
	 */
	public final boolean contains(final String key) {
		return WebSocketStorage.contains(this, key);
	}

	/**
	 * Get data from socket storage
	 * @param <T>
	 * @param key
	 * @return
	 */
	public final <T> T get(final String key) {
		return WebSocketStorage.get(this, key);
	}

	/**
	 * Get data from socket storage
	 * @param <T>
	 * @param key
	 * @return
	 */
	public final <T> T remove(final String key) {
		return WebSocketStorage.remove(this, key);
	}
	
	/**
	 * Set data to socket storage
	 * @param <T>
	 * @param key
	 * @param value
	 * @return
	 */
	public final <T> T set(final String key, T value) {
		return WebSocketStorage.store(this, key, value);
	}
	
	public final HttpSession getHttpSession() {
		return WebSocketStorage.get(this, HttpSession.class);
	}

	public final boolean isValidHttpSession() {

		final HttpSession httpSession = getHttpSession();

		if (Objects.isNull(httpSession)) return false;

		try {
			final String attr = (String) httpSession.getAttribute(QuarkConstants.HTTP_SEESION_STATUS);
			return Boolean.TRUE.toString().equals(attr);
		} catch (Exception e) {
			LOG.debug(e.getMessage(), e);
			return false;
		}

	}

	@Override
	public final boolean equals(final Object obj) {

		boolean status = false;

		if (obj instanceof WebSocketSession) {
			status = ((WebSocketSession) obj).hashCode() == hashCode(); 			
		}

		return status;
	}

	@Override
	public final int hashCode() {
		return session.hashCode();
	}

	@Override
	public final Async getAsyncRemote() {
		return session.getAsyncRemote();
	}

	@Override
	public final Basic getBasicRemote() {
		return session.getBasicRemote();
	}

	@Override
	public <T> void addMessageHandler(final Class<T> arg0, final Whole<T> arg1) {
		// not used
	}

	@Override
	public <T> void addMessageHandler(final Class<T> arg0, final Partial<T> arg1) {
		// not used
	}

	@Override
	public int compareTo(final WebSocketSession o) {
		if (Objects.isNull(o)) return 1;
		final int lh = hashCode();
		final int rh = o.hashCode();
		if (lh == rh) return 0;
		return (lh > rh) ? 1 : -1;
	}
}
