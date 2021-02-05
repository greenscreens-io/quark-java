/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 * 
 * https://www.greenscreens.io
 * 
 */
package io.greenscreens.quark.web.listener;

import javax.enterprise.event.Observes;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import io.greenscreens.quark.web.QuarkHandlerUtil;
import io.greenscreens.quark.websocket.WebsocketEvent;

/**
 * Quark web app context listener
 */
public final class WebContextListener implements ServletContextListener {

	/**
	 * 
	 */
	protected void onWebSocketEvent(@Observes final WebsocketEvent wsEvent) {
		// not used for now
	}

	/**
	 * Unload Quark engine
	 */
	@Override
	public void contextDestroyed(final ServletContextEvent event) {
		QuarkHandlerUtil.releaseValidator();
	}

	/**
	 * Load Quark engine 
	 */
	@Override
	public void contextInitialized(final ServletContextEvent event) {
		QuarkHandlerUtil.initValidator();
	}

}
