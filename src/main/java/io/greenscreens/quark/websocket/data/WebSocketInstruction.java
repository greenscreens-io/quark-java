/*
 * Copyright (C) 2015, 2023 Green Screens Ltd.
 */
package io.greenscreens.quark.websocket.data;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * WebSocket return structure {type:'ws' , cmd : * , data : *}
 */
public enum WebSocketInstruction {

    WELCO("welco", true), 
    PING("ping", true),
    BYE("bye", true), 
    API("api", true),

    ERR("err", false),
    INS("ins", false), // internal instruction
    DATA("data", false),
    
    // OLD
    ENC("enc", false)
    ;

    private final String text;
    private final boolean simple;

    private WebSocketInstruction(final String text, final boolean simple) {
        this.text = text;
        this.simple = simple;
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
        return this.simple;
    }
}
