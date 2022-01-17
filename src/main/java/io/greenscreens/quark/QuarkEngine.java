/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 * 
 * https://www.greenscreens.io
 * 
 */
package io.greenscreens.quark;

import java.lang.annotation.Annotation;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;

public enum QuarkEngine {
;
	/**
	 *  if GT 0, check system time difference for request  
	 */
	public static long TIMESTAMP = -1;

	public static BeanManager getBeanManager() {

		final CDI<Object> cdi = CDI.current();
		
		if (cdi != null) {
			return cdi.getBeanManager();
		}
		
		return null;
	}

	public static <T> T getBean(final Class<T> cls) {

		final CDI<Object> cdi = CDI.current();
		if (cdi != null) {
			final Instance<T> inst = cdi.select(cls);
			if (inst != null) {
				return inst.get();
			}
		}

		return null;
	}

	public static <T> T of(final Class<T> clazz, Annotation...annotations) {
		return CDI.current().select(clazz, annotations).get();
    }
	
}
