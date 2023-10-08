/*
 * Copyright (C) 2015, 2023 Green Screens Ltd.
 */
package io.greenscreens.quark.reflection.internal;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import io.greenscreens.quark.QuarkEngine;
import io.greenscreens.quark.annotations.ExtJSAction;
import io.greenscreens.quark.annotations.ExtJSDirect;
import io.greenscreens.quark.annotations.ExtJSMethod;
import io.greenscreens.quark.cdi.BeanManagerUtil;
import io.greenscreens.quark.cdi.IDestructibleBeanInstance;
import io.greenscreens.quark.reflection.IQuarkBean;
import io.greenscreens.quark.reflection.IQuarkHandle;
import io.greenscreens.quark.utils.QuarkUtil;
import io.greenscreens.quark.utils.ReflectionUtil;
import jakarta.enterprise.inject.spi.Bean;

/**
 * Wrapper around CDI bean with modern reflection  
 */
final class QuarkBean implements IQuarkBean {

	private final Bean<?> bean;
	private final Collection<IQuarkHandle> handles;

	public QuarkBean(final Bean<?> bean) {
		super();
		this.bean = bean;
		handles = initialize();
	}

	private Collection<IQuarkHandle> initialize() {
		
		final Collection<QuarkHandle> collection = new ArrayList<>();		
		final Class<?> clazz = bean.getBeanClass();
		final Method[] methods = clazz.getMethods();
		
		for (Method method : methods ) {
			final ExtJSMethod annotation = method.getAnnotation(ExtJSMethod.class);
			if (Objects.nonNull(annotation)) {
				collection.add(new QuarkHandle(bean, method));
			}			
		}
		
		return Collections.unmodifiableCollection(collection);
	}

	@Override
	public ExtJSDirect extDirect() {
		return bean.getBeanClass().getAnnotation(ExtJSDirect.class);
	}
	
	@Override
	public ExtJSAction extAction() {
		return bean.getBeanClass().getAnnotation(ExtJSAction.class);
	}
	
	@Override
	public Bean<?> bean() {
		return bean;
	}

	@Override
	public String[] paths() {
		return ReflectionUtil.paths(bean.getBeanClass());
	}
	
	@Override
	public boolean accept(final String url) {
		if (QuarkUtil.isEmpty(url)) return false;
		final List<String> paths = Arrays.asList(paths());
		return paths.contains(url) || paths.contains("*");
	}
	
	@Override
	public boolean accept(final Collection<String> uri) {
		if (Objects.isNull(uri)) return false;
		final List<String> paths = new ArrayList<>(Arrays.asList(paths()));
		paths.retainAll(uri);
		return !paths.isEmpty();
	}
		
	@Override
	public Collection<IQuarkHandle> handles() {
		return handles;
	}

	@Override
	public IDestructibleBeanInstance<?> instance() {
		return QuarkEngine.of(BeanManagerUtil.class).getDestructibleBeanInstance(bean());
	}

}
