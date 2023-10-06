/*
 * Copyright (C) 2015, 2023 Green Screens Ltd.
 */
package io.greenscreens.quark.cdi;

import java.util.Objects;

import com.fasterxml.jackson.databind.node.ArrayNode;

import io.greenscreens.quark.QuarkEngine;
import io.greenscreens.quark.internal.QuarkBuilder;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;

/**
 * Singleton class used to find CDI bean and wraps it into destructable
 * instance. It is used as an internal bean finder.
 */
@ApplicationScoped
public class BeanManagerUtil {

	private ArrayNode api;
	
	@PostConstruct
	public void init() {
		getAPI();
	}
	
	/**
	 * Retrieve engine meta structure for web
	 * 
	 * @return
	 */
	public ArrayNode getAPI() {
		if (Objects.isNull(api)) {
			api = QuarkBuilder.build(null);
		}
		return api;
	}
	/** 
	 * Wraps CDI bean into custom destructible instance
	 * 
	 * @param bean
	 * @return
	 */
	public <T> IDestructibleBeanInstance<T> getDestructibleBeanInstance(final Bean<T> bean) {

		IDestructibleBeanInstance<T> result = null;

		if (Objects.nonNull(bean)) {

			final CreationalContext<T> creationalContext = QuarkEngine.getBeanManager().createCreationalContext(bean);

			if (Objects.nonNull(creationalContext)) {
				final T instance = bean.create(creationalContext);
				result = new DestructibleBeanInstance<>(instance, bean, creationalContext);
			}

		}

		return result;
	}


}
