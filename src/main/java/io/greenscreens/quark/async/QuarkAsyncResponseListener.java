/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 * 
 * https://www.greenscreens.io
 */
package io.greenscreens.quark.async;

import java.io.IOException;

import javax.enterprise.inject.Vetoed;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletResponse;

import io.greenscreens.quark.QuarkProducer;
import io.greenscreens.quark.ext.ExtJSResponse;
import io.greenscreens.quark.web.QuarkErrors;
import io.greenscreens.quark.web.QuarkHandlerUtil;
import io.greenscreens.quark.web.ServletUtils;

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
			final ExtJSResponse result = QuarkHandlerUtil.getError(QuarkErrors.E7777);
			ServletUtils.sendResponse(ServletUtils.wrap(response), result);			
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
