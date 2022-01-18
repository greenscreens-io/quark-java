/*
 * Copyright (C) 2015, 2022 Green Screens Ltd.
 */
package io.greenscreens.quark.async;

import java.io.IOException;

import javax.enterprise.inject.Vetoed;
import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;

/**
 * Task for executing async controllers
 */
@Vetoed
public abstract class QuarkAsyncTask implements Runnable, AsyncListener {

	protected final AsyncContext ctx;
	private boolean invalidated = false;
	
	public QuarkAsyncTask(final AsyncContext ctx) {
		super();
		this.ctx = ctx;
		ctx.addListener(this);
	}

	public AsyncContext getContext() {
		return ctx;
	}

	@Override
	public void onComplete(AsyncEvent event) throws IOException {
		invalidated = true;
	}

	@Override
	public void onTimeout(AsyncEvent event) throws IOException {
		invalidated = true;
	}

	@Override
	public void onError(AsyncEvent event) throws IOException {
		invalidated = true;
	}

	@Override
	public void onStartAsync(AsyncEvent event) throws IOException {
	
	}

	@Override
	public void run() {
	
		try {
			if (!invalidated && !ctx.getResponse().isCommitted()) {
				onExecute();
			} 
		} finally { 
			close();
		}
		
	}
	
	public void close() {
		ctx.complete();
		invalidated = true;
	}

	protected abstract void onExecute();

	
}
