/*
 * Copyright (C) 2015, 2023 Green Screens Ltd.
 */
package io.greenscreens.quark.async;

import java.io.IOException;

import jakarta.enterprise.inject.Vetoed;
import jakarta.servlet.AsyncEvent;
import jakarta.servlet.AsyncListener;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

import io.greenscreens.quark.QuarkProducer;
import io.greenscreens.quark.ext.ExtJSResponse;
import io.greenscreens.quark.web.QuarkErrors;
import io.greenscreens.quark.web.QuarkHandlerUtil;
import io.greenscreens.quark.web.ServletUtils;

/**
 * Handles async controllers execution. Also, if timeouts, send an error response to requester.
 */
@Vetoed
final class QuarkAsyncResponseListener implements AsyncListener {

	@Override
	public void onComplete(AsyncEvent event) throws IOException {
		QuarkProducer.releaseAsync();
	}

	@Override
	public void onTimeout(AsyncEvent event) throws IOException {
		final ServletResponse response = event.getAsyncContext().getResponse();
		if (!response.isCommitted()) {			
			final ServletRequest request = event.getAsyncContext().getRequest();
			final boolean compress = ServletUtils.supportGzip((HttpServletRequest) request);
			final ExtJSResponse result = QuarkHandlerUtil.getError(QuarkErrors.E7777);
			ServletUtils.sendResponse(ServletUtils.wrap(response), result, compress);			
		}
		QuarkProducer.releaseAsync();
	}

	@Override
	public void onError(AsyncEvent event) throws IOException {
		QuarkProducer.releaseAsync();
	}

	@Override
	public void onStartAsync(AsyncEvent event) throws IOException {
		// not used
	}
	
	public static QuarkAsyncResponseListener create() {
		return new QuarkAsyncResponseListener();
	}
}
