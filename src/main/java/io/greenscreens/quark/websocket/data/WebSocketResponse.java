/*
 * Copyright (C) 2015, 2023 Green Screens Ltd.
 */
package io.greenscreens.quark.websocket.data;

import java.io.Serializable;

import io.greenscreens.quark.web.QuarkConstants;
import jakarta.enterprise.inject.Vetoed;

/**
 * Object to be converted into JSON structure. {type :'ws' , sid : session_id ,
 * tid : transaction_id, timeout : 0 , ....}
 */
@Vetoed
public class WebSocketResponse implements IWebSocketResponse, Serializable {

	private static final long serialVersionUID = 1L;

	private String type = QuarkConstants.MESSAGE_TYPE;

	private final WebSocketInstruction cmd;

	private String errMsg;
	private int errId;
	private Object data;
		
	public WebSocketResponse(final WebSocketInstruction cmd) {
		this.cmd = cmd;
	}

	public WebSocketResponse(final Object data) {
		this.cmd = WebSocketInstruction.DATA;
		this.data = data;
	}
	
	@Override
	public final String getType() {
		return type;
	}

	@Override
	public final void setType(final String type) {
		this.type = type;
	}

	@Override
	public final String getErrMsg() {
		return errMsg;
	}

	@Override
	public final void setErrMsg(final String errMsg) {
		this.errMsg = errMsg;
	}

	@Override
	public final int getErrId() {
		return errId;
	}

	@Override
	public final void setErrId(final int errId) {
		this.errId = errId;
	}

	@Override
	public final Object getData() {
		return data;
	}

	@Override
	public final void setData(final Object data) {
		this.data = data;
	}

	@Override
	public final WebSocketInstruction getCmd() {
		return cmd;
	}
	
	@Override
	public String toString() {
		return "WebSocketResponse [type=" + type + ", cmd=" + cmd + ", errMsg=" + errMsg + ", errId=" + errId + ", data=" + data + "]";
	}

	public static IWebSocketResponse asData(final Object data) {
		return new WebSocketResponse(data);
	}
	
	public static IWebSocketResponse asData() {
		return create(WebSocketInstruction.DATA);
	}

	public static IWebSocketResponse asError() {
		return create(WebSocketInstruction.ERR);
	}
	
	public static IWebSocketResponse create(final WebSocketInstruction cmd) {
		return new WebSocketResponse(cmd);
	}
}
