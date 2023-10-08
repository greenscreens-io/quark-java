/*
 * Copyright (C) 2015, 2023 Green Screens Ltd.
 */
package io.greenscreens.quark.reflection;

import java.util.Collection;

import io.greenscreens.quark.annotations.ExtJSAction;
import io.greenscreens.quark.annotations.ExtJSDirect;
import io.greenscreens.quark.cdi.IDestructibleBeanInstance;
import jakarta.enterprise.inject.spi.Bean;

/**
 * An interface template to map CDI bean
 */
public interface IQuarkBean {

	IDestructibleBeanInstance<?> instance();

	ExtJSDirect extDirect();

	ExtJSAction extAction();

	Bean<?> bean();

	String[] paths();

	boolean accept(final String url);

	boolean accept(final Collection<String> url);

	Collection<IQuarkHandle> handles();
	
}
