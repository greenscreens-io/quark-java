/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 * 
 * https://www.greenscreens.io
 * 
 */
package io.greenscreens.quark.web;

/**
 * Constants used by web to store session values.
 */
public enum QuarkConstants {
    ;
    
	public static final String HTTP_SEESION_STATUS   = "io.greenscreens.quark.status";
   
    public static final String ENCRYPT_ENGINE 		 = "io.greenscreens.quark.encrypt_engine";
    public static final String ENCRYPT_CHANNEL  	 = "io.greenscreens.quark.encrypt_channel";
    
    public static final String QUARK_SESSION     	 = "io.greenscreens.quark.session";
    public static final String QUARK_PATH        	 = "io.greenscreens.quark.path";
    public static final String QUARK_CHALLENGE		 = "io.greenscreens.quark.challenge";
    public static final String QUARK_COMPRESSION 	 = "io.greenscreens.quark.compression";
    public static final String QUARK_SUBPROTOCOL 	 = "ws4is";
    
    public static final String MESSAGE_TYPE 		 = "ws";
    
	public static final String LOG_BROADCAST_INJECT = "Websocket broadcast event not injected in callback.";
	public static final String LOG_RSA_ERROR = "Decryption error. Dynamic ecryption mode does not allow url reuse.";
	public static final String LOG_URL_OVERLOAD = "URL request is too long";

		
}
