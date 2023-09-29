/*
 * Copyright (C) 2015, 2023 Green Screens Ltd.
 */
package io.greenscreens.quark;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Vetoed;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.BeforeBeanDiscovery;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessAnnotatedType;
import jakarta.enterprise.inject.spi.configurator.BeanConfigurator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.greenscreens.quark.async.QuarkAsyncContext;
import io.greenscreens.quark.cdi.BeanManagerUtil;
import io.greenscreens.quark.web.QuarkContext;
import io.greenscreens.quark.websocket.WebSocketEndpoint;
import io.greenscreens.quark.websocket.WebSocketService;
import io.greenscreens.quark.websocket.WebSocketSession;

/**
 * Custom CDI startup initializator to register injectable resources 
 */
@Vetoed
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
			.createWith(e-> QuarkProducer.getWebSocketSession());
		}
		if (bm.getBeans(QuarkAsyncContext.class).isEmpty()) {
			register(event, bm, QuarkAsyncContext.class)
			.createWith(e-> QuarkProducer.getQuarkAsyncContext());
		}
		if (bm.getBeans(QuarkContext.class).isEmpty()) {
			register(event, bm, QuarkContext.class)
			.createWith(e-> QuarkProducer.getQuarkContext());
		}
	}

	private <T> BeanConfigurator<T> register(final AfterBeanDiscovery event, final BeanManager bm, final Class<T> clazz) {
		return event.addBean()
        .read(bm.createAnnotatedType(clazz))
        .beanClass(clazz);
	}
	
}
