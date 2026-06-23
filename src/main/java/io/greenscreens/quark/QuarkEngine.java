/*
 * Copyright (C) 2015, 2023 Green Screens Ltd.
 */
package io.greenscreens.quark;

import java.lang.annotation.Annotation;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import jakarta.enterprise.concurrent.ManagedExecutorService;
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
	
	/**
	 * Internal flag to optimize calls
	 */
	private static AtomicBoolean ALTERNATIVE = new AtomicBoolean(false);
	
	/**
	 * Alternative to get ManagedExecutorService, required to run async tasks in CDI  
	 */
	public static AtomicReference<String> EXECUTOR_JNDI = new AtomicReference<String>("java:jboss/ee/concurrency/executor/default");

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

    public static <T> T of(final Class<T> clazz, final Annotation...annotations) {
        return (ManagedExecutorService.class.equals(clazz)) ? managedExecutorService(clazz) : CDI.current().select(clazz, annotations).get();
    }

    private static <T> T managedExecutorService(final Class<T> clazz) {
        if (ALTERNATIVE.get()) return managedExecutorServiceJndi(clazz);
        try {
            // 1. Try CDI lookup first
            return CDI.current().select(clazz).get();
        } catch (final Exception e) {
            // 2. Fallback to standard WildFly JNDI lookup for the default executor
            ALTERNATIVE.set(true);
            return managedExecutorServiceJndi(clazz);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T managedExecutorServiceJndi(final Class<T> clazz) {
        try {
            return (T) new InitialContext().lookup(EXECUTOR_JNDI.get());
        } catch (final NamingException ne) {
            throw new IllegalStateException("Could not resolve ManagedExecutorService via CDI or JNDI", ne);
        }

    }

    public static ManagedExecutorService getExecutorService() {
        return managedExecutorService(ManagedExecutorService.class);
    }
}
