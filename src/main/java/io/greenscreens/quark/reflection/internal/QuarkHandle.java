/*
 * Copyright (C) 2015, 2023. Green Screens Ltd.
 */
package io.greenscreens.quark.reflection.internal;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Set;

import io.greenscreens.quark.QuarkEngine;
import io.greenscreens.quark.cdi.BeanManagerUtil;
import io.greenscreens.quark.cdi.IDestructibleBeanInstance;
import io.greenscreens.quark.reflection.IQuarkHandle;
import io.greenscreens.quark.util.ReflectionUtil;
import jakarta.enterprise.inject.spi.AnnotatedMethod;
import jakarta.enterprise.inject.spi.AnnotatedParameter;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.Bean;

/**
 * Reference to reflective bean call
 */
final class QuarkHandle implements IQuarkHandle {

	final long id;
	final Bean<?> bean;
	final Method method;
	AnnotatedMethod<AnnotatedParameter<?>> annotatedMethod; 
	MethodHandle methodHandle;
	Boolean asyncResponder = null;
	
	public QuarkHandle(final Bean<?> bean, final Method method) {
		super();
		this.bean = bean;
		this.method = method;
		this.id = Integer.toUnsignedLong(method.toString().hashCode());
	}

	@Override
	public long id() {
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
			// Java 8
			//methodHandle = QuarkMapper.toHandle(method).asSpreader(Object[].class, method.getParameterCount());
			methodHandle = QuarkMapper.toHandle(method).asSpreader(1, Object[].class, method.getParameterCount());
		}
        if (Objects.isNull(methodHandle)) {
            throw new NoSuchMethodException("Method handle is null");
        }		
		return methodHandle;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })	
	@Override
	public AnnotatedMethod<AnnotatedParameter<?>> annotatedMethod(){
		if (Objects.isNull(annotatedMethod)) {
			final AnnotatedType annType = QuarkEngine.getBeanManager().createAnnotatedType(bean.getBeanClass());
			final Set<AnnotatedMethod<AnnotatedParameter<?>>> aMethods = annType.getMethods();
			annotatedMethod = aMethods.stream().filter(m -> m.getJavaMember().equals(method)).findFirst().orElse(null);
		}
		return annotatedMethod;
	}
	
	@Override
	public boolean isVoid() {
		return ReflectionUtil.isVoid(method);
	}
	
	@Override
	public boolean isAsync() {
		return ReflectionUtil.isAsync(method) || ReflectionUtil.isAsync(method.getDeclaringClass());
	}
	
	@Override
	public boolean isVirtual() {
		return ReflectionUtil.isVirtual(method) || ReflectionUtil.isVirtual(method.getDeclaringClass());
	}
		
	@Override
	public boolean isValidate() {
		return ReflectionUtil.isValidate(method);
	}
	
    @Override
    public boolean isProtected() {
        return ReflectionUtil.isProtected(method) || ReflectionUtil.isProtected(method.getDeclaringClass());
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

	@Override
	public String toString() {
		return String.format("%s@%s.%s", name(), bean.getBeanClass().getName(), method.getName());
	}
	
}
