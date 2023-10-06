/*
 * Copyright (C) 2015, 2023 Green Screens Ltd.
 */
module io.greenscreens.quark{
	
	requires java.base;
	requires transitive org.slf4j;
	requires transitive org.bouncycastle.provider;
	requires transitive com.fasterxml.jackson.databind;
	requires transitive jakarta.cdi;
	requires transitive jakarta.servlet;
	requires transitive jakarta.validation;
	requires transitive jakarta.websocket.client;
	requires transitive jakarta.websocket;
		
	exports io.greenscreens.quark;
	exports io.greenscreens.quark.annotations;
	exports io.greenscreens.quark.async;
	exports io.greenscreens.quark.cdi;
	exports io.greenscreens.quark.ext;
	exports io.greenscreens.quark.web;
	exports io.greenscreens.quark.reflection;
	exports io.greenscreens.quark.web.data;
	exports io.greenscreens.quark.web.listener;
	exports io.greenscreens.quark.websocket;
	exports io.greenscreens.quark.websocket.data;
	exports io.greenscreens.quark.websocket.heartbeat;
}
