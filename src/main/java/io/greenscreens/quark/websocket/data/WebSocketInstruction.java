/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 * 
 * https://www.greenscreens.io
 * 
 */
package io.greenscreens.quark.websocket.data;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * WebSocket return structure {type:'ws' , cmd : * , data : *}
 */
public enum WebSocketInstruction {

    WELCO("welco"), 
    API("api"),
    BYE("bye"), 
    ERR("err"), 
    DATA("data"),
    ENC("enc"),
    INS("ins"), // internal instruction
    ECHO("echo"),
    PING("ping")
	;

	private final String text;

	private WebSocketInstruction(final String text) {
		this.text = text;
	}

	@JsonValue
	public String getText() {
		return text;
	}

	@Override
	public String toString() {
		return text;
	}

	// fix for eclipse compiler - switch issue
	public boolean isSimple() {
		return this == WELCO || this == API || this == ECHO || this == PING || this == BYE;
	}
}
