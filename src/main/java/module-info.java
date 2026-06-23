/*
 * Copyright (C) 2015, 2023 Green Screens Ltd.
 */
module io.greenscreens.quark{
	
	requires java.base;
	requires java.naming;
	requires jdk.unsupported;
	requires org.slf4j;
	requires org.bouncycastle.provider;
	requires com.fasterxml.jackson.databind;
	requires jakarta.cdi;
	requires jakarta.servlet;
	requires jakarta.validation;
	requires jakarta.websocket.client;
	requires jakarta.websocket;
	requires jakarta.concurrency;
    requires io.opentelemetry.api;

	exports io.greenscreens.quark;
	exports io.greenscreens.quark.annotations;
	exports io.greenscreens.quark.async;
	exports io.greenscreens.quark.cdi;
	exports io.greenscreens.quark.ext;
	exports io.greenscreens.quark.web;
	exports io.greenscreens.quark.util;
	exports io.greenscreens.quark.reflection;
	exports io.greenscreens.quark.web.data;
	exports io.greenscreens.quark.web.listener;
	exports io.greenscreens.quark.websocket;
	exports io.greenscreens.quark.websocket.data;
	exports io.greenscreens.quark.websocket.heartbeat;
}
