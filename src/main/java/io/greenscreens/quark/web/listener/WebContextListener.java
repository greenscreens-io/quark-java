/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 * 
 * https://www.greenscreens.io
 * 
 */
package io.greenscreens.quark.web.listener;

import java.util.Calendar;

import javax.enterprise.event.Observes;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.greenscreens.quark.QuarkSecurity;
import io.greenscreens.quark.websocket.WebsocketEvent;

/**
 * HttpSession and Servlet context listener
 */
@WebListener
public final class WebContextListener implements ServletContextListener {

	private static final Logger LOG = LoggerFactory.getLogger(WebContextListener.class);

	private static ServletContext context;
	private static ValidatorFactory factory = null;

	public static ServletContext getContext() {
		return context;
	}

	public static ValidatorFactory getValidationFactory() {
		return factory;
	}

	/**
	 * 
	 */
	protected void onWebSocketEvent(@Observes final WebsocketEvent wsEvent) {
		// not used for now
	}

	/**
	 * Remove 5250 server configurations
	 */
	@Override
	public void contextDestroyed(final ServletContextEvent event) {
		if (factory != null) factory.close();
	}

	/**
	 * Load 5250 server configurations
	 */
	@Override
	public void contextInitialized(final ServletContextEvent event) {

		context = event.getServletContext();

		final int year = Calendar.getInstance().get(Calendar.YEAR);

		LOG.info("Green Screens Ltd., \u00a9 2016 - {}", year);
		LOG.info("Email: info@.greenscreens.io");
		LOG.info("Visit: http://www.greenscreens.io");

		QuarkSecurity.initialize();
		try {
			factory = Validation.buildDefaultValidatorFactory();
		} catch (Exception e) {
			LOG.warn(e.getMessage());
		}
	}

}
