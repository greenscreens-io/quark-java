/*
 * Copyright (C) 2015, 2022 Green Screens Ltd.
 */
package io.greenscreens.quark.web;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import javax.enterprise.inject.Vetoed;
import javax.enterprise.inject.spi.Bean;
import javax.inject.Inject;

import io.greenscreens.quark.QuarkEngine;
import io.greenscreens.quark.QuarkProducer;
import io.greenscreens.quark.QuarkUtil;
import io.greenscreens.quark.async.QuarkAsyncContext;
import io.greenscreens.quark.cdi.BeanManagerUtil;
import io.greenscreens.quark.cdi.IDestructibleBeanInstance;
import io.greenscreens.quark.ext.ExtJSResponse;

/**
 * Class to execute controller bean 
 */
@Vetoed
public class QuarkBeanCaller implements Runnable {

	final Bean<?> bean;
	final Method method;
	final Object[] params;

	final QuarkHandler handler;
	final boolean isVoid;
	final boolean hasAsyncReponse;
	final boolean isAsync;
	

	public QuarkBeanCaller(final QuarkHandler handler, final Bean<?> bean, final Method method, final Object[] params) {
		super();
		this.handler = handler;
		this.bean = bean;
		this.method = method;
		this.params = params;
		this.isAsync = isAsync();
		this.isVoid = QuarkHandlerUtil.isVoid(method);
		this.hasAsyncReponse = hasAsyncResponse();
	}
	
	public void call() {
		
		if (isAsync) {
			handler.getContext();
			CompletableFuture.runAsync(this);
		} else {
			run();
		}
	}

	@Override
	public void run() {
			
		IDestructibleBeanInstance<?> di = null;
		
		try {
			attach();
			di = QuarkEngine.of(BeanManagerUtil.class).getDestructibleBeanInstance(bean);
			handler.response = call(di);
		} catch (Exception e) {
			handler.response = new ExtJSResponse(e, QuarkUtil.toMessage(e));
			QuarkHandlerUtil.printError(e);
		} finally {
			release(di);
			handler.send();
			deattach();
		}

	}
	
	/**
	 * Attach WebSOcket or Servlet context to current thread before controlelr execution 
	 */
	protected void attach() {
		
		if (!isAsync) return; 
			
		if (Objects.nonNull(handler.wsSession)) {
			QuarkProducer.attachSession(handler.wsSession);
		} else {
			QuarkProducer.attachRequest(QuarkContext.create(handler.httpRequest, handler.httpResponse));
		}

		if (hasAsyncReponse && isVoid) {
			QuarkProducer.attachAsync(new QuarkAsyncContext(handler));
		}

	}

	/**
	 * Release thread context
	 */
	protected void deattach() {
		
		if (!isAsync) return;
		
		if (Objects.nonNull(handler.wsSession)) {
			QuarkProducer.releaseSession();			
		} else {
			QuarkProducer.releaseRequest();
		}

	}

	private boolean hasAsyncResponse() {
		if (!isAsync) return false;
		boolean sts = false;
		final Field [] fields =  bean.getBeanClass().getDeclaredFields();
		for (Field field : fields) {
			sts = isAsyncResponder(field);
			if (sts) break;
		}

		return sts;
	}
	
	private boolean isAsyncResponder(final Field field) {
		return field.isAnnotationPresent(Inject.class) && field.getType() == QuarkAsyncContext.class;		
	}
	
	private boolean isAsync() {
		return handler.supportAsync && QuarkHandlerUtil.isAsync(method);
	}

	/**
	 * Safe controller bean destruction
	 * @param bean
	 */
	private void release(final IDestructibleBeanInstance<?> bean) {
		if (Objects.nonNull(bean)) bean.release();
	}
	
	/**
	 * Execute Controller bean and get response for requester
	 * @param bean
	 * @return
	 * @throws IOException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	private ExtJSResponse call(final IDestructibleBeanInstance<?> bean) throws IOException, IllegalAccessException, InvocationTargetException {
		final Object beanInstance = bean.getInstance();
		QuarkHandlerUtil.validateParameters(beanInstance, method, params);
		final Object obj = method.invoke(beanInstance, params);
		if (hasAsyncReponse && isVoid) return null;
		return QuarkHandlerUtil.toResponse(obj, method);		
	}
	
	/**
	 * Public controller initializer
	 * @param handler
	 * @param bean
	 * @param method
	 * @param params
	 * @return
	 */
	public static final QuarkBeanCaller get(final QuarkHandler handler, final Bean<?> bean, final Method method, final Object[] params) {
		return new QuarkBeanCaller(handler, bean, method, params);
	}
	
}
