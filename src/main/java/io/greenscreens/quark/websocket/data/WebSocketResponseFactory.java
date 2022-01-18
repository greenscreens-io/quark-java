/*
 * Copyright (C) 2015, 2022 Green Screens Ltd.
 */
package io.greenscreens.quark.websocket.data;

/**
 * Helper class to create WebSocket response object
 */
public enum WebSocketResponseFactory {
	;

	public static IWebSocketResponse create(final WebSocketInstruction cmd) {
		return create(cmd, false, false);
	}
	
	public static IWebSocketResponse create(final WebSocketInstruction cmd, final boolean isBinary) {
		return create(cmd, isBinary, false);
	}
	
	public static IWebSocketResponse createAsData(final boolean isBinary, final boolean isCompression) {
		return create(WebSocketInstruction.DATA, isBinary, isCompression);
	}

	public static IWebSocketResponse createAsError(final boolean isBinary, final boolean isCompression) {
		return create(WebSocketInstruction.ERR, isBinary, isCompression);
	}
	
	public static IWebSocketResponse create(final WebSocketInstruction cmd, final boolean isBinary, final boolean isCompression) {
		if(isBinary) {
			return new WebSocketResponseBinary(cmd, isCompression);
		} else {
			return new WebSocketResponse(cmd);
		}
	}

}
