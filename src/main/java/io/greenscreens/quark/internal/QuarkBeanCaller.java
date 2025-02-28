/*
 * Copyright (C) 2015, 2023 Green Screens Ltd.
 */
package io.greenscreens.quark.internal;

import java.lang.invoke.MethodHandle;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.greenscreens.quark.QuarkProducer;
import io.greenscreens.quark.async.QuarkAsyncContext;
import io.greenscreens.quark.cdi.IDestructibleBeanInstance;
import io.greenscreens.quark.ext.ExtJSResponse;
import io.greenscreens.quark.reflection.IQuarkHandle;
import io.greenscreens.quark.util.QuarkUtil;
import io.greenscreens.quark.web.QuarkContext;
import jakarta.enterprise.inject.Vetoed;

/**
 * Class to execute controller bean 
 */
@Vetoed
public class QuarkBeanCaller implements Runnable {

    final static private Logger LOG = LoggerFactory.getLogger(QuarkBeanCaller.class); 
    
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
			di = beanHandle.instance();
			handler.response = call(di);
		} catch (Throwable e) {
			handler.response = new ExtJSResponse(e, QuarkUtil.toMessage(e));
			QuarkUtil.printError(e, LOG);
		} finally {
			release(di);
			handler.send();
			deattach();
		}

	}
	
	/**
	 * Attach WebSOcket or Servlet context to current thread before controller execution 
	 */
	protected void attach() {
					
		if (Objects.nonNull(handler.getWsSession())) {
			QuarkProducer.attachSession(handler.getWsSession());
		} else if (asAsync()) {
			QuarkProducer.attachAsync(new QuarkAsyncContext(handler));
		} else {
			QuarkProducer.attachRequest(QuarkContext.create(handler.getHttpRequest(), handler.getHttpResponse()));
		}

	}

	/**
	 * Release thread context
	 */
	protected void deattach() {
		
		if (Objects.nonNull(handler.getWsSession())) {
			QuarkProducer.releaseSession();			
		} else if (asAsync()) {
			QuarkProducer.releaseAsync();
		} else {
			QuarkProducer.releaseRequest();
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
	    Optional.ofNullable(bean).ifPresent(b -> b.release());
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
