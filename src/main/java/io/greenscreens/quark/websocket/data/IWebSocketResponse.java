/*
 * Copyright (C) 2015, 2022 Green Screens Ltd.
 */
package io.greenscreens.quark.websocket.data;

import io.greenscreens.quark.security.IAesKey;

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

	IAesKey getKey();

	void setKey(IAesKey key);
	
}