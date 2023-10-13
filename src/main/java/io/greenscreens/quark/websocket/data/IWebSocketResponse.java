/*
 * Copyright (C) 2015, 2023. Green Screens Ltd.
 */
package io.greenscreens.quark.websocket.data;

/**
 * Interface that defines response format
 */
public interface IWebSocketResponse {

	String getType();

	void setType(String type);

	String getErrMsg();

	void setErrMsg(String errMsg);

	int getErrId();

	void setErrId(int errId);

	Object getData();

	void setData(Object data);

	WebSocketInstruction getCmd();
	
}