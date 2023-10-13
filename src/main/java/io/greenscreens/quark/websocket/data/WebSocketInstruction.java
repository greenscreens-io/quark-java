/*
 * Copyright (C) 2015, 2023. Green Screens Ltd.
 */
package io.greenscreens.quark.websocket.data;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * WebSocket return structure {type:'ws' , cmd : * , data : *}
 */
public enum WebSocketInstruction {

    WELCO("welco"), 
    PING("ping"),
    BYE("bye"), 
    API("api"),
    ERR("err"), 
    INS("ins"), // internal instruction
    DATA("data"),
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
		return this.ordinal() < ERR.ordinal();
	}
}
