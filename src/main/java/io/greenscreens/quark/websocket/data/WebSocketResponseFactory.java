/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 * 
 * https://www.greenscreens.io
 * 
 */
package io.greenscreens.quark.websocket.data;

/**
 * Helper class to create websocket reponse object
 */
public enum WebSocketResponseFactory {
	;

	static public IWebSocketResponse create(final WebSocketInstruction cmd) {
		return create(cmd, false, false);
	}
	
	static public IWebSocketResponse create(final WebSocketInstruction cmd, final boolean isBinary) {
		return create(cmd, isBinary, false);
	}
	
	static public IWebSocketResponse createAsData(final boolean isBinary, final boolean isCompression) {
		return create(WebSocketInstruction.DATA, isBinary, isCompression);
	}

	static public IWebSocketResponse createAsError(final boolean isBinary, final boolean isCompression) {
		return create(WebSocketInstruction.ERR, isBinary, isCompression);
	}
	
	static public IWebSocketResponse create(final WebSocketInstruction cmd, final boolean isBinary, final boolean isCompression) {
		if(isBinary) {
			return new WebSocketResponseBinary(cmd, isCompression);
		} else {
			return new WebSocketResponse(cmd);
		}
	}

}
