/*
 * Copyright (C) 2015, 2023. Green Screens Ltd.
 */
package io.greenscreens.quark.async;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.greenscreens.quark.util.QuarkUtil;
import io.greenscreens.quark.web.ServletUtils;
import jakarta.enterprise.inject.Vetoed;

/**
 * Async engine for Async Servlets - Controllers
 * For async controllers, instance is put in scheduled task for execution.
 */
@Vetoed
public final class QuarkAsyncEngine {

    static final int SC_TOO_MANY_REQUESTS = 429;    
    static final String E429 = "Too many requests! Access temporary denied.";
    
	private final BlockingQueue<Runnable> queue =  new LinkedBlockingQueue<>();
	private ThreadPoolExecutor service;
	private int queueSize;

	public QuarkAsyncEngine(final String name, final int parallelTasks, final int priority, final int timeoutMinutes) {
		super();
		create(name, parallelTasks, 0, priority, timeoutMinutes);
	}
	
	public QuarkAsyncEngine(final String name, final int parallelTasks, final int maxPool, final int priority, final int timeoutSeconds) {
		super();
		create(name, parallelTasks, maxPool, priority, timeoutSeconds);
	}

	void create(final String name, final int parallelTasks, final int maxPool, final int priority, final int timeoutMinutes) {
		this.queueSize = maxPool;		
		final ThreadFactory factory = QuarkUtil.getThreadFactory(name, priority);
		this.service = new ThreadPoolExecutor(1, parallelTasks, timeoutMinutes, TimeUnit.SECONDS, queue, factory);
		this.service.prestartCoreThread();
	}
	
	public boolean isActive() {
		return !(service.isTerminated() || service.isShutdown());
	}
	
	public void stop() {
		queue.clear();
		if (!isActive()) return;
		QuarkUtil.safeTerminate(service, false);
	}
	
    public boolean canRegister() {
        return isActive() && (queueSize == 0 || queue.size() <= queueSize);
    }	
	
	/**
	 * Register async request to processing queue
	 * 
	 * @param ctx
	 * @throws IOException
	 */
	public boolean register(final QuarkAsyncTask task) {
 
        final boolean state = canRegister();

        if (state) {
            service.execute(task);
        } else {
            ServletUtils.sendError(task.getContext().getResponse(), SC_TOO_MANY_REQUESTS, E429);
            task.close();
        }

        return state;

	}

	/**
	 * Unregister SSE web client for mobile auth from processing queue
	 * 
	 * @param ctx
	 */
	protected boolean unregister(final QuarkAsyncTask task) {
		return queue.remove(task);
	}

}
