/*
 * Copyright (C) 2015, 2022 Green Screens Ltd.
 */
package io.greenscreens.quark.websocket.data;

import java.io.Serializable;

import javax.enterprise.inject.Vetoed;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.greenscreens.quark.security.IAesKey;
import io.greenscreens.quark.web.QuarkConstants;

/**
 * Object to be converted into JSON structure. {type :'ws' , sid : session_id ,
 * tid : transaction_id, timeout : 0 , ....}
 */
@Vetoed
public class WebSocketResponseBinary implements IWebSocketResponse, Serializable {

	private static final long serialVersionUID = 1L;

	private String type = QuarkConstants.MESSAGE_TYPE;

	private final WebSocketInstruction cmd;

	private String errMsg;
	private int errId;
	private Object data;
	
	@JsonIgnore
	private transient IAesKey key;

	@JsonIgnore
	private transient boolean compression;
	
	public WebSocketResponseBinary(final WebSocketInstruction cmd) {
		this.cmd = cmd;
	}

	public WebSocketResponseBinary(final WebSocketInstruction cmd, final boolean compression) {
		this.cmd = cmd;
		this.compression = compression;
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
	public IAesKey getKey() {
		return key;
	}

	@Override
	public void setKey(final IAesKey key) {
		this.key = key;
	}
	
	public boolean isCompression() {
		return compression;
	}

	public void setCompression(boolean compression) {
		this.compression = compression;
	}

	@Override
	public String toString() {
		return "WebSocketResponse [type=" + type + ", cmd=" + cmd + ", errMsg=" + errMsg + ", errId=" + errId
				+ ", data=" + data + ", key=" + key + "]";
	}

}
