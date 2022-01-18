/*
 * Copyright (C) 2015, 2022 Green Screens Ltd.
 */
package io.greenscreens.quark.websocket;

/**
 * WebSocket event types triggered
 */
public enum WebSocketEventStatus {
	START, CLOSE, ERROR, DESTROY
	// MESSAGE - not used - to optimize
}
