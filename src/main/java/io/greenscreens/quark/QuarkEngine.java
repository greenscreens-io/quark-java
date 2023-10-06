/*
 * Copyright (C) 2015, 2023 Green Screens Ltd.
 */
package io.greenscreens.quark;

import java.lang.annotation.Annotation;
import java.util.Objects;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.CDI;

/**
 * Base core of Quark engine, used to instantiate controllers within CDI engine.
 * On every HTTP/WebSocket request,  JSON data is decoded and matched to proper
 * Controller class. IF class is found, it is loaded by this engine into CDI context.   
 */
public enum QuarkEngine {
;
	/**
	 *  if GT 0, check system time difference for request  
	 */
	public static long TIMESTAMP = -1;

	public static BeanManager getBeanManager() {

		final CDI<Object> cdi = CDI.current();
		
		if (Objects.nonNull(cdi)) {
			return cdi.getBeanManager();
		}
		
		return null;
	}

	public static <T> T getBean(final Class<T> cls) {

		final CDI<Object> cdi = CDI.current();
		if (Objects.nonNull(cdi)) {
			final Instance<T> inst = cdi.select(cls);
			if (Objects.nonNull(inst)) {
				return inst.get();
			}
		}

		return null;
	}

	public static <T> T of(final Class<T> clazz, Annotation...annotations) {
		return CDI.current().select(clazz, annotations).get();
    }
	
}
