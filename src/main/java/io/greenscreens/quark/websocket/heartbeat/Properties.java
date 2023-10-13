/*
 * Copyright (C) 2015, 2023. Green Screens Ltd.
 */
package io.greenscreens.quark.websocket.heartbeat;

/**
 * Settings for WebSocet heartBeat
 */
enum Properties {
;
	public static final Integer MAX_RETRY_COUNT = 3;
	public static final Integer WEBSOCKET_PING_SCHEDULED_TIME_IN_SECONDS = 60;
	public static final Integer WEBSOCKET_SESSION_IDLE_TIME_IN_MINUTES = 3;
}
