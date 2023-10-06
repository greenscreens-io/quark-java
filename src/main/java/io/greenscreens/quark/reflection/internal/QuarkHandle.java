/*
 * Copyright (C) 2015, 2023 Green Screens Ltd.
 */
package io.greenscreens.quark.reflection.internal;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import io.greenscreens.quark.QuarkEngine;
import io.greenscreens.quark.cdi.BeanManagerUtil;
import io.greenscreens.quark.cdi.IDestructibleBeanInstance;
import io.greenscreens.quark.reflection.IQuarkHandle;
import io.greenscreens.quark.utils.ReflectionUtil;
import jakarta.enterprise.inject.spi.AnnotatedMethod;
import jakarta.enterprise.inject.spi.AnnotatedParameter;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.Bean;

/**
 * Reference to reflective bean call
 */
final class QuarkHandle implements IQuarkHandle {

	private final static AtomicInteger counter = new AtomicInteger(); 
	
	final int id = counter.incrementAndGet();
	final Bean<?> bean;
	final Method method;
	AnnotatedMethod<AnnotatedParameter<?>> anothatedMethod; 
	MethodHandle methodHandle;
	Boolean asyncResponder = null;
	
	public QuarkHandle(final Bean<?> bean, final Method method) {
		super();
		this.bean = bean;
		this.method = method;
	}

	@Override
	public int id() {
		return id;
	}

	@Override
	public Bean<?> bean() {
		return bean;
	}

	@Override
	public Method method() {
		return method;
	}

	@Override
	public MethodHandle methodHandle() throws NoSuchMethodException, IllegalAccessException {
		if (Objects.isNull(methodHandle)) {
			methodHandle = QuarkMapper.toHandle(method);
		}
		return methodHandle;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })	
	@Override
	public AnnotatedMethod<AnnotatedParameter<?>> annotatedMethod(){
		if (Objects.isNull(anothatedMethod)) {
			final AnnotatedType annType = QuarkEngine.getBeanManager().createAnnotatedType(bean.getBeanClass());
			final Set<AnnotatedMethod<AnnotatedParameter<?>>> aMethods = annType.getMethods();
			anothatedMethod = aMethods.stream().filter(m -> m.getJavaMember().equals(method)).findFirst().orElse(null);
		}
		return anothatedMethod;
	}
	
	@Override
	public boolean isVoid() {
		return ReflectionUtil.isVoid(method);
	}
	
	@Override
	public boolean isAsync() {
		return ReflectionUtil.isAsync(method);
	}
		
	@Override
	public boolean isValidate() {
		return ReflectionUtil.isValidate(method);
	}
	
	@Override
	public String name() {
		return ReflectionUtil.mappedName(method);
	}
	
	@Override
	public String[] paths() {
		return ReflectionUtil.paths(bean.getBeanClass());
	}

	@Override
	public IDestructibleBeanInstance<?> instance() {
		return QuarkEngine.of(BeanManagerUtil.class).getDestructibleBeanInstance(bean());
	}
	
	@Override
	public boolean isAsyncArgs() {
		if (Objects.isNull(asyncResponder)) {
			asyncResponder = Boolean.valueOf(hasAsyncResponse()); 
		}
		return asyncResponder.booleanValue();
	}
	
	private boolean hasAsyncResponse() {
		if (!isAsync()) return false;
		return ReflectionUtil.isAsyncResponder(bean().getBeanClass().getDeclaredFields());
	}
	
}
