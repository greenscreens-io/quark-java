/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 * 
 * https://www.greenscreens.io
 * 
 */
package io.greenscreens.quark;

import java.util.Calendar;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.configurator.BeanConfigurator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.greenscreens.quark.async.QuarkAsyncResponse;
import io.greenscreens.quark.cdi.BeanManagerUtil;
import io.greenscreens.quark.websocket.WebSocketEndpoint;
import io.greenscreens.quark.websocket.WebSocketService;
import io.greenscreens.quark.websocket.WebSocketSession;

public class QuarkExtension implements Extension {

	static final Logger LOG = LoggerFactory.getLogger(QuarkExtension.class);

	void beforeBeanDiscovery(@Observes BeforeBeanDiscovery bbd) {
		LOG.debug("beginning the scanning process");

		final int year = Calendar.getInstance().get(Calendar.YEAR);

		LOG.info("Green Screens Ltd., \u00a9 2016 - {}", year);
		LOG.info("Email: info@.greenscreens.io");
		LOG.info("Visit: http://www.greenscreens.io");
		
		QuarkSecurity.initialize();
	}

	<T> void processAnnotatedType(@Observes ProcessAnnotatedType<T> pat) {
		LOG.debug("scanning type: {}", pat.getAnnotatedType().getJavaClass().getName());
	}

	void afterBeanDiscovery(@Observes AfterBeanDiscovery event, BeanManager bm) {
		LOG.debug("finished the scanning process");
		LOG.info("Registering Quark Engine components...");
		if (bm.getBeans(BeanManagerUtil.class).isEmpty()) {
			register(event, bm, BeanManagerUtil.class).scope(ApplicationScoped.class);
		}
		if (bm.getBeans(QuarkProducer.class).isEmpty()) {
			register(event, bm, QuarkProducer.class).scope(ApplicationScoped.class);
		}
		if (bm.getBeans(WebSocketService.class).isEmpty()) {
			register(event, bm, WebSocketService.class);
		}
		if (bm.getBeans(WebSocketEndpoint.class).isEmpty()) {
			register(event, bm, WebSocketEndpoint.class);
		}
		if (bm.getBeans(WebSocketSession.class).isEmpty()) {
			register(event, bm, WebSocketSession.class)
			.createWith(e-> QuarkProducer.getSession());
		}
		if (bm.getBeans(QuarkAsyncResponse.class).isEmpty()) {
			register(event, bm, QuarkAsyncResponse.class)
			.createWith(e-> QuarkProducer.getAsync());
		}
	}

	private <T> BeanConfigurator<T> register(final AfterBeanDiscovery event, final BeanManager bm, final Class<T> clazz) {
		return event.addBean()
        .read(bm.createAnnotatedType(clazz))
        .beanClass(clazz);
	}
	
}
