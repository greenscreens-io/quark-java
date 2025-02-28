/*
 * Copyright (C) 2015, 2024 Green Screens Ltd.
 */
package io.greenscreens.quark.util;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ConcurrentFuture<V> implements Future<V>, Comparable<ConcurrentFuture<V>> {

	private final Future<V> future;
	
	public ConcurrentFuture(final Future<V> future) {
		super();
		this.future = future;
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		return future.cancel(mayInterruptIfRunning);
	}

	@Override
	public V get() throws InterruptedException, ExecutionException {
		return future.get();
	}

	@Override
	public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		return future.get(timeout, unit);
	}

	@Override
	public boolean isCancelled() {
		return future.isCancelled();
	}

	@Override
	public boolean isDone() {
		return future.isDone();
	}

	@Override
	public int compareTo(ConcurrentFuture<V> o) {

		Future<V> obj1 = this.future;
		Future<V>  obj2 = null;
	    
	    if (o!=null) {
	    	obj2 = o.future;
	    }
	    
	    if (obj1 == null) {
	    	return -1;
	    }
	    if (obj2 == null) {
	    	return 1;
	    }

	    if (obj1.equals(obj2)) {
	        return 0;
	    }
	    return -1;
	}

	Future<V> unwrap() {
		return future;
	}
	
	public static <V> ConcurrentFuture<V> create(final Future<V> future) {	
		return new ConcurrentFuture<V>(future);
	}
}
