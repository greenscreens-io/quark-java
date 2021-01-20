/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 *
 * https://www.greenscreens.io
 *
 */
package io.greenscreens.quark;

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

import io.greenscreens.quark.cdi.BeanManagerUtil;
import io.greenscreens.quark.websocket.WebSocketEndpoint;
import io.greenscreens.quark.websocket.WebSocketService;
import io.greenscreens.quark.websocket.WebSocketSession;

/**
 * Auto loading module for web deployment
 *
 */
public class QuarkExtension implements Extension {

	static final Logger LOG = LoggerFactory.getLogger(QuarkExtension.class);

	void beforeBeanDiscovery(@Observes BeforeBeanDiscovery bbd) {
		LOG.debug("beginning the scanning process");
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
		if (bm.getBeans(WebSocketService.class).isEmpty()) {
			register(event, bm, WebSocketService.class);
		}
		if (bm.getBeans(WebSocketEndpoint.class).isEmpty()) {
			register(event, bm, WebSocketEndpoint.class);
		}
		if (bm.getBeans(WebSocketSession.class).isEmpty()) {
			register(event, bm, WebSocketSession.class)
			.createWith(e-> WebSocketEndpoint.get());
		}
	}

	private <T> BeanConfigurator<T> register(final AfterBeanDiscovery event, final BeanManager bm, final Class<T> clazz) {
		return event.addBean()
        .read(bm.createAnnotatedType(clazz))
        .beanClass(clazz);
	}
}
