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
import io.greenscreens.quark.ext.ExtJSResponse;
import io.greenscreens.quark.internal.QuarkErrors;
import io.greenscreens.quark.internal.QuarkHandlerUtil;
import io.greenscreens.quark.web.ServletUtils;

/**
 * Handles async controllers execution. Also, if timeouts, send an error response to requester.
 */
@Vetoed
final class QuarkAsyncListener implements AsyncListener {

	@Override
	public void onComplete(final AsyncEvent event) throws IOException {
		// not used
	}

	@Override
	public void onTimeout(final AsyncEvent event) throws IOException {
		final ServletResponse response = event.getAsyncContext().getResponse();
		if (!response.isCommitted()) {			
			final ServletRequest request = event.getAsyncContext().getRequest();
			final boolean compress = ServletUtils.supportGzip((HttpServletRequest) request);
			final ExtJSResponse result = QuarkHandlerUtil.getError(QuarkErrors.E7777);
			ServletUtils.sendResponse(ServletUtils.wrap(response), result, compress);			
		}
	}

	@Override
	public void onError(final AsyncEvent event) throws IOException {
		// not used
	}

	@Override
	public void onStartAsync(final AsyncEvent event) throws IOException {
		// not used
	}
	
	public static QuarkAsyncListener create() {
		return new QuarkAsyncListener();
	}
}
