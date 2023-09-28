/*
 * Copyright (C) 2015, 2023 Green Screens Ltd.
 */
package io.greenscreens.quark.websocket;

import io.greenscreens.quark.websocket.data.IWebSocketResponse;

/**
 * Class holding event data
 */
public class WebsocketBroadcastEvent {

	private final IWebSocketResponse data;

	public WebsocketBroadcastEvent(final IWebSocketResponse data) {
		super();
		this.data = data;
	}

	public IWebSocketResponse getData() {
		return data;
	}

}
