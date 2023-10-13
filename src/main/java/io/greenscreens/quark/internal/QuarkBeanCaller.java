/*
 * Copyright (C) 2015, 2023 Green Screens Ltd.
 */
package io.greenscreens.quark.internal;

import java.lang.ScopedValue.Carrier;
import java.lang.invoke.MethodHandle;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import io.greenscreens.quark.QuarkProducer;
import io.greenscreens.quark.async.QuarkAsyncContext;
import io.greenscreens.quark.cdi.IDestructibleBeanInstance;
import io.greenscreens.quark.ext.ExtJSResponse;
import io.greenscreens.quark.reflection.IQuarkHandle;
import io.greenscreens.quark.utils.QuarkUtil;
import io.greenscreens.quark.web.QuarkContext;
import jakarta.enterprise.inject.Vetoed;

/**
 * Class to execute controller bean 
 */
@Vetoed
public class QuarkBeanCaller implements Runnable {

	final IQuarkHandle beanHandle;
	final Object[] params;

	final QuarkHandler handler;
	final boolean isVoid;
	final boolean isAsync;

	public QuarkBeanCaller(final QuarkHandler handler, final IQuarkHandle handle, final Object[] params) {
		super();
		this.beanHandle = handle;
		this.handler = handler;
		this.params = params;
		this.isAsync = isAsync();
		this.isVoid = handle.isVoid();
	}
	
	private void callAsync(){
		handler.getContext();
		if (beanHandle.isVirtual()) {
			Thread.ofVirtual().name(beanHandle.toString()).start(this);
		} else {	
			CompletableFuture.runAsync(this);
		}
	}
	
	public void call() {
		if (isAsync) {
			callAsync();
		} else {
			run();
		}
	}

	@Override
	public void run() {
		
		final Carrier carrier = attach();
		carrier.run(() -> {
			IDestructibleBeanInstance<?> di = null;
			try {
				di = beanHandle.instance();
				handler.response = call(di);
			} catch (Throwable e) {
				handler.response = new ExtJSResponse(e, QuarkUtil.toMessage(e));
				QuarkUtil.printError(e);
			} finally {
				release(di);
				handler.send();		
			}			
		});

	}
	
	/**
	 * Attach WebSOcket or Servlet context to current thread before controller execution 
	 */
	protected Carrier attach() {
					
		if (Objects.nonNull(handler.getWsSession())) {
			return QuarkProducer.attachSession(handler.getWsSession());
		} else if (asAsync()) {
			return QuarkProducer.attachAsync(new QuarkAsyncContext(handler));
		} else {
			return QuarkProducer.attachRequest(QuarkContext.create(handler.getHttpRequest(), handler.getHttpResponse()));
		}

	}

	
	private boolean asAsync() {
		return beanHandle.isAsyncArgs() && isVoid;
	}
	
	private boolean isAsync() {
		return handler.isSupportAsync() && beanHandle.isAsync();
	}
	
	
	private MethodHandle methodHandle() throws NoSuchMethodException, IllegalAccessException {
		return beanHandle.methodHandle();
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
	 * @throws Throwable 
	 * @throws NoSuchMethodException 
	 */
	private ExtJSResponse call(final IDestructibleBeanInstance<?> bean) throws NoSuchMethodException, Throwable {
		final Object beanInstance = bean.getInstance();
		QuarkValidator.validateParameters(beanHandle, beanInstance, params); 
		final Object obj = methodHandle().invoke(beanInstance, params);
		if (asAsync()) return null;
		return QuarkHandlerUtil.toResponse(obj, beanHandle);		
	}
	
	/**
	 * Public controller initializer
	 * @param handler
	 * @param bean
	 * @param method
	 * @param params
	 * @return
	 */
	public static final QuarkBeanCaller get(final QuarkHandler handler, final IQuarkHandle handle, final Object[] params) {
		return new QuarkBeanCaller(handler, handle, params);
	}
	
}
