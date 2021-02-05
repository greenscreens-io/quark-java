/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 * 
 * https://www.greenscreens.io
 */
package io.greenscreens.quark.async;

import java.io.IOException;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;

import io.greenscreens.quark.QuarkProducer;

final class QuarkAsyncResponseListener implements AsyncListener {

	@Override
	public void onComplete(AsyncEvent event) throws IOException {
		QuarkProducer.releaseAsync();
	}

	@Override
	public void onTimeout(AsyncEvent event) throws IOException {
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
