/*
 * Copyright (C) 2015, 2023. Green Screens Ltd.
 */
package io.greenscreens.quark.reflection;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;

import io.greenscreens.quark.cdi.IDestructibleBeanInstance;
import jakarta.enterprise.inject.spi.AnnotatedMethod;
import jakarta.enterprise.inject.spi.AnnotatedParameter;
import jakarta.enterprise.inject.spi.Bean;

/**
 * Interface template for Bean-Meethod-Reflection mapping
 */
public interface IQuarkHandle {

	/**
	 * Unique method id, used at the front to reference a call
	 * @return
	 */
	long id();
	
	/**
	 * CDI Bean - owner of this mapping
	 * @return
	 */
	Bean<?> bean();
	
	/**
	 * Mapped bean method
	 * @return
	 */
	Method method();
	
	/**
	 * Virtual Method wrapper 
	 * @return
	 * @throws NoSuchMethodException
	 * @throws IllegalAccessException
	 */
	MethodHandle methodHandle() throws NoSuchMethodException, IllegalAccessException;
	
	/**
	 * Annotated method with parameters
	 * @return
	 */
	AnnotatedMethod<AnnotatedParameter<?>> annotatedMethod();
	
	/**
	 * Does called method return response 
	 * @return
	 */
	boolean isVoid();
	
	/**
	 * Does called method executes asynchronously 
	 * @return
	 */
	boolean isAsync();
	
	/**
	 * Does async code execute in virtual thread
	 * @return
	 */
	boolean isVirtual();

	/**
	 * Is any of method parameters async
	 * @return
	 */
	boolean isAsyncArgs();
	
	/**
	 * Should validate input parameters
	 * @return
	 */
	boolean isValidate();
	
	/**
	 * Check if access to the exposed methods are enabled
	 * @return
	 */
	boolean isProtected();
	
	/**
	 * Method mapped name for front
	 * @return
	 */
	String name();
	
	/**
	 * List of front URL paths allowed for a call
	 * @return
	 */
	String[] paths();
	
	/**
	 * Instance of the destructible bean
	 * @return
	 */
	IDestructibleBeanInstance<?> instance();
	
}
