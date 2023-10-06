/*
 * Copyright (C) 2015, 2023 Green Screens Ltd.
 */
package io.greenscreens.quark.async;

import java.util.Objects;
import jakarta.servlet.AsyncContext;
import jakarta.servlet.AsyncListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import io.greenscreens.quark.ext.ExtJSResponse;
import io.greenscreens.quark.internal.QuarkHandler;

/**
 * Thread level context class holder for servlet parameters
 * Use it to inject into Controllers that runs in async mode when
 * Controllers needs access to HTTP request and HTTP response data.
 * Also, us it when async controller finish it's task to send data back to requester. 
 */
public final class QuarkAsyncContext {

	final QuarkHandler quarkHandler;
	final AsyncContext asyncContext;
	
	public QuarkAsyncContext(final QuarkHandler quarkHandler) {
		this.quarkHandler = quarkHandler;
		this.asyncContext = quarkHandler.getContext();
		if (Objects.nonNull(asyncContext)) {
			asyncContext.addListener(QuarkAsyncListener.create());
		}
	}
	
	public void addListener(final AsyncListener listener) {
		if (Objects.nonNull(asyncContext)) {
			asyncContext.addListener(listener);
		}
	}
	
	public void setTimeout(final long timeout) {
		if (Objects.nonNull(asyncContext)) {
			asyncContext.setTimeout(timeout);
		}
	}
	
	public boolean send(final ExtJSResponse value) {	
		return quarkHandler.send(value);
	}

	public HttpServletRequest getRequest() {
		if (Objects.isNull(asyncContext)) return null;
		return (HttpServletRequest) asyncContext.getRequest();
	}

	public HttpServletResponse getResponse() {
		if (Objects.isNull(asyncContext)) return null;
		return (HttpServletResponse) asyncContext.getResponse();
	}
}
