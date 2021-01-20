/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 * 
 * https://www.greenscreens.io
 */
package io.greenscreens.quark.web;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.function.Supplier;

import javax.enterprise.inject.spi.Bean;

import io.greenscreens.quark.QuarkEngine;
import io.greenscreens.quark.cdi.BeanManagerUtil;
import io.greenscreens.quark.cdi.IDestructibleBeanInstance;
import io.greenscreens.quark.ext.ExtJSResponse;
import io.greenscreens.quark.websocket.WebSocketEndpoint;

/**
 * Class to execute bean 
 */
public class QuarkBeanCaller implements Supplier<ExtJSResponse> {

	final Bean<?> bean;
	final Method method;
	final Object[] params;

	final QuarkHandler handler;

	public QuarkBeanCaller(final QuarkHandler handler, final Bean<?> bean, final Method method, final Object[] params) {
		super();
		this.handler = handler;
		this.bean = bean;
		this.method = method;
		this.params = params;
	}

	@Override
	public ExtJSResponse get() {
			
		IDestructibleBeanInstance<?> di = null;
		ExtJSResponse response = null;
		boolean isAsync = isAsync();
		
		try {
			if (isAsync) {
				WebSocketEndpoint.attach(handler.wsSession);
			}
			di = QuarkEngine.of(BeanManagerUtil.class).getDestructibleBeanInstance(bean);
			response = call(di);
		} catch (Exception e) {
			response = new ExtJSResponse(e, e.getMessage());
			QuarkHandlerUtil.printError(e);
		} finally {
			release(di);
			if (isAsync) {
				WebSocketEndpoint.release();
			}
		}

		return response;
	}
	
	private boolean isAsync() {
		final boolean isAsync = QuarkHandlerUtil.isAsync(method);
		return isAsync && handler.supportAsync;
	}

	private void release(final IDestructibleBeanInstance<?> bean){
		if (Objects.nonNull(bean)) bean.release();
	}
	
	private ExtJSResponse call(final IDestructibleBeanInstance<?> bean) throws IOException, IllegalAccessException, InvocationTargetException {
		final Object beanInstance = bean.getInstance();
		QuarkHandlerUtil.validateParameters(beanInstance, method, params);
		final Object obj = method.invoke(beanInstance, params);
		return QuarkHandlerUtil.toResponse(obj, method);		
	}
	
	public static final QuarkBeanCaller get(final QuarkHandler handler, final Bean<?> bean, final Method method, final Object[] params) {
		return new QuarkBeanCaller(handler, bean, method, params);
	}
	
}
