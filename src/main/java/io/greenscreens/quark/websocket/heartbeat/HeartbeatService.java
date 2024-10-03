/*
 * Copyright (C) 2015, 2023. Green Screens Ltd.
 */
package io.greenscreens.quark.websocket.heartbeat;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.greenscreens.quark.utils.NamedThreadFactory;
import io.greenscreens.quark.utils.QuarkUtil;
import jakarta.websocket.CloseReason;
import jakarta.websocket.CloseReason.CloseCodes;
import jakarta.websocket.Session;


/**
 * Batch job for WebSocket heart beat
 */
public enum HeartbeatService {	
;
	private static final Logger LOG = LoggerFactory.getLogger(HeartbeatService.class);
	
	private static ScheduledExecutorService mainService = null;
	private static ExecutorService workerService = null;
	private static final Map<Session, HeartbeatSession> sessionHeartbeats = new ConcurrentHashMap<>();

	private static final ThreadFactory factory = NamedThreadFactory.get("WebSocket ping service", Thread.NORM_PRIORITY);
	
	private HeartbeatService() {}

	public static void terminate() {
		LOG.info("Terminating Quark WebSocket Heartbeat service!");
		mainService = QuarkUtil.safeTerminate(mainService);
		workerService = QuarkUtil.safeTerminate(workerService);
	}
	
	/**
	 * Invoke on application startup, for the scheduler to be initiated.
	 */
	public static void initialize() {
		LOG.info("Initializing Quark WebSocket Heartbeat service!");
		if (Objects.isNull(workerService)) {
			final int cores = Runtime.getRuntime().availableProcessors();
			workerService = Executors.newFixedThreadPool(cores, factory);
		}
		
		if (Objects.nonNull(mainService)) return;
		mainService = new ScheduledThreadPoolExecutor(1, factory);
		mainService.scheduleAtFixedRate(HeartbeatService::pingClients,
				Properties.WEBSOCKET_PING_SCHEDULED_TIME_IN_SECONDS,
				Properties.WEBSOCKET_PING_SCHEDULED_TIME_IN_SECONDS, TimeUnit.SECONDS);
	}

	/**
	 * Add session to the registry.
	 * 
	 * @param heart beat
	 */
	public static void registerSession(final Session session) {
		final HeartbeatSession heartbeat = new HeartbeatSession(session);
		sessionHeartbeats.put(session, heartbeat);
	}

	/**
	 * Remove the from the registry.
	 * 
	 * @param heart beat
	 */
	public static void deregisterSession(final Session session) {
		sessionHeartbeats.remove(session);
	}
	
	public static void updateSession(final Session session) {
		if (!sessionHeartbeats.containsKey(session)) return;
		final HeartbeatSession heartbeat = sessionHeartbeats.get(session);
		heartbeat.getLastMessageOnInMillis().set(System.currentTimeMillis());
		heartbeat.getRetry().set(Properties.MAX_RETRY_COUNT);
	}

	public static void handlePong(final Session session) {
		if (!sessionHeartbeats.containsKey(session)) return;
		final HeartbeatSession heartbeat = sessionHeartbeats.get(session);
		heartbeat.getLastPongReceived().set(System.currentTimeMillis());
		heartbeat.getRetry().set(Properties.MAX_RETRY_COUNT);
	}

	private static void pingClients() {
		sessionHeartbeats.values().parallelStream().forEach(heartbeat -> {
			submitToWorker(heartbeat);
		});
	}

	private static void submitToWorker(final HeartbeatSession heartbeat) {
		if (Objects.nonNull(workerService))  
			workerService.submit(() -> {
			final boolean expired = hasIdleTimeExpired(heartbeat);
			if (heartbeat.getRetry().get() == 0 || expired) {
				closeSession(heartbeat);
			} else {
				pingToClient(heartbeat);
			}
		});
	}

	private static void pingToClient(final HeartbeatSession heartbeat) {
		final Session session = heartbeat.getUserSession();
		try {
			if (session.isOpen()) {
				final ByteBuffer payload = ByteBuffer.wrap(LocalDateTime.now().toString().getBytes());

				session.getBasicRemote().sendPing(payload);
				heartbeat.getLastPingAt().set(System.currentTimeMillis());
				heartbeat.getRetry().set(Properties.MAX_RETRY_COUNT);
			} else {
				heartbeat.getRetry().decrementAndGet();
			}
		} catch (Exception e) {
			heartbeat.getRetry().decrementAndGet();
			final String msg = QuarkUtil.toMessage(e);
			LOG.error(msg);
			LOG.debug(msg, e);
		}
	}

	private static boolean hasIdleTimeExpired(final HeartbeatSession heartbeat) {
		final Long lastWsSessionPingTimeInMillis = heartbeat.getLastMessageOnInMillis().get();
		return (System.currentTimeMillis() - lastWsSessionPingTimeInMillis) > TimeUnit.MINUTES
				.toMillis(Properties.WEBSOCKET_SESSION_IDLE_TIME_IN_MINUTES);
	}

	private static void closeSession(final HeartbeatSession heartbeat) {
		Session session = heartbeat.getUserSession();
		try {
			session.close(new CloseReason(CloseCodes.NORMAL_CLOSURE,
					"SessionId: " + session.getId() + ", Client does not respond"));
		} catch (IOException e) {
			final String msg = QuarkUtil.toMessage(e);
			LOG.error(msg);
			LOG.debug(msg, e);
		}
	}
}