/*
 * Copyright (C) 2015, 2023 Green Screens Ltd.
 */
package io.greenscreens.quark.reflection.internal;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import io.greenscreens.quark.annotations.ExtJSDirectLiteral;
import io.greenscreens.quark.reflection.IQuarkBean;
import io.greenscreens.quark.reflection.IQuarkHandle;
import jakarta.enterprise.inject.Vetoed;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;

/**
 * This is a cache of prepared reflected methods 
 */
@Vetoed
public enum QuarkMapper {
;
	private final static Lookup lookup;
	private final static QuarkHandleCollection methods = new QuarkHandleCollection();
	private final static QuarkBeanCollection handles = new QuarkBeanCollection();
	
	static {
		lookup = MethodHandles.lookup();
	}

	/**
	 * Called from CDI extensions after bean discovery.
	 * Engine take all Quark annotated classes and maps 
	 * their exposed methods with MethodHandler, a modern Java reflection API
	 * 
	 * Each method has its own QuarkHandler instance with unique ID used at the front end side
	 *   
	 * @param beanManager
	 */
	public static void scan(final BeanManager beanManager) {
		final ExtJSDirectLiteral type = new ExtJSDirectLiteral();
		final Set<Bean<?>> beans = beanManager.getBeans(Object.class, type);
		for (Bean<?> bean : beans) {
			final IQuarkBean handle = register(bean);
			if (!handle.handles().isEmpty()) handles.add(handle);
		}		
	}
	
	/**
	 * Find 
	 * @param handle
	 * @return
	 */
	public static IQuarkHandle get(final int handle) {
		return methods.get(handle);
	}
	
	/**
	 * Register CDI bean into Quark Engine. 
	 * Will scan all exposed methods and link them 
	 * with MethodHandler for reflective calls
	 * @param bean
	 * @return
	 */
	static IQuarkBean register(final Bean<?> bean) {		
		final QuarkBean beanHandle = new QuarkBean(bean);
		beanHandle.handles().forEach(h -> methods.put(h.id(), h));
		return beanHandle;
	}
	
	/**
	 * Convert java Method to MethodHandler
	 * @param method
	 * @return
	 * @throws NoSuchMethodException
	 * @throws IllegalAccessException
	 */
	static MethodHandle toHandle(final Method method) throws NoSuchMethodException, IllegalAccessException {
		return lookup.unreflect(method);
		/*
		final Class<?> clazz = method.getDeclaringClass();
		final Class<?> rType = method.getReturnType();
		final Class<?>[] pType = method.getParameterTypes();
		final MethodType mt = MethodType.methodType(rType, pType); 
		return lookup.findVirtual(clazz, method.getName(), mt);
		*/
	}

	/**
	 * Find Mapped bean in cache if already processed
	 * @param bean
	 * @return
	 */
	public static Optional<IQuarkBean> find(final Bean<?> bean) {
		return handles.stream().filter(b -> b.bean() == bean).findFirst();
	}
	
	public static Optional<IQuarkBean> find(final IQuarkHandle handle) {
		return find(handle.bean());
	}
	
	public static Optional<IQuarkBean> find(final int handle) {
		return find(get(handle));
	}
	
	/**
	 * Retrieve list of all mapped CDI beans
	 * @return
	 */
	public static Collection<IQuarkBean> beans() {
		return Collections.unmodifiableCollection(handles);
	}
	
	/**
	 * Filter mapped beans by access URI
	 * @param url
	 * @return
	 */
	public static Collection<IQuarkBean> filter(final Collection<String> uri) {
		if (Objects.isNull(uri) || uri.isEmpty()) return beans();
		return beans().stream().filter(b -> b.accept(uri)).collect(Collectors.toList());
	}
	
}
