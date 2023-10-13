/*
 * Copyright (C) 2015, 2023. Green Screens Ltd.
 */
package io.greenscreens.quark.websocket.heartbeat;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import jakarta.websocket.Session;

/**
 * Session data
 */
final class HeartbeatSession {

	private final Session userSession;
	private final AtomicInteger retry = new AtomicInteger(Properties.MAX_RETRY_COUNT);
	private final AtomicReference<Long> lastPingAt = new AtomicReference<>(System.currentTimeMillis());
	private final AtomicReference<Long> lastPongReceived = new AtomicReference<>(System.currentTimeMillis());
	private final AtomicReference<Long> lastMessageOnInMillis = new AtomicReference<>(System.currentTimeMillis());

	public HeartbeatSession(final Session session) {
		super();
		this.userSession = session;
	}

	public Session getUserSession() {
		return userSession;
	}

	public AtomicInteger getRetry() {
		return retry;
	}

	public AtomicReference<Long> getLastPingAt() {
		return lastPingAt;
	}

	public AtomicReference<Long> getLastPongReceived() {
		return lastPongReceived;
	}

	public AtomicReference<Long> getLastMessageOnInMillis() {
		return lastMessageOnInMillis;
	}
	
}
