/*
 * Copyright (C) 2015, 2023 Green Screens Ltd.
 */
package io.greenscreens.quark.websocket.data;

import java.util.List;

import javax.enterprise.inject.Vetoed;

import com.fasterxml.jackson.databind.JsonNode;

import io.greenscreens.quark.ext.ExtJSDirectRequest;
import io.greenscreens.quark.web.QuarkConstants;

/**
 * Class used to map JSON structure describing ExtJS WebSocket request.
 */
@Vetoed
public class WebSocketRequest {

	public final String type = QuarkConstants.MESSAGE_TYPE;

	private WebSocketInstruction cmd; // 'welcome , bye, data' ,
	private int timeout; // set only when cmd=welcome

	private String errMsg;
	private int errId;

	// list of commands - batch
	private List<ExtJSDirectRequest<JsonNode>> data;
	
	public final WebSocketInstruction getCmd() {
		return cmd;
	}

	public final void setCmd(final WebSocketInstruction cmd) {
		this.cmd = cmd;
	}

	public final int getTimeout() {
		return timeout;
	}

	public final void setTimeout(final int timeout) {
		this.timeout = timeout;
	}

	public final String getErrMsg() {
		return errMsg;
	}

	public final void setErrMsg(final String errMsg) {
		this.errMsg = errMsg;
	}

	public final int getErrId() {
		return errId;
	}

	public final void setErrId(final int errId) {
		this.errId = errId;
	}

	public final String getType() {
		return type;
	}

	public final List<ExtJSDirectRequest<JsonNode>> getData() {
		return data;
	}

	public final void setData(final List<ExtJSDirectRequest<JsonNode>> data) {
		this.data = data;
	}


	@Override
	public String toString() {
		return "WebSocketRequest [type=" + type + ", cmd=" + cmd + ", timeout=" + timeout + ", errMsg=" + errMsg
				+ ", errId=" + errId + ", data=" + data + "]";
	}

}
