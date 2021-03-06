/*
 * Copyright (C) 2015, 2016  Green Screens Ltd.
 */
package io.greenscreens.quark;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class NamedThreadFactory implements ThreadFactory {

	private static final AtomicInteger poolNumber = new AtomicInteger(1);
	private final AtomicInteger threadNumber = new AtomicInteger(1);
	private final String namePrefix;
	private final int priority;
	private final boolean daemon;
	
	public static NamedThreadFactory get(final String name, final int priority) {
		return new NamedThreadFactory(name, priority, true);
	}

	NamedThreadFactory(final String name, final int priority, final boolean daemon) {
		this.priority = priority;
		this.daemon = daemon;
		namePrefix = name + "-" + poolNumber.getAndIncrement() + "-thread-";
	}

	/**
	 * Create new names thread
	 */
	@Override
	public Thread newThread(final Runnable r) {

		final String tName = namePrefix + threadNumber.getAndIncrement();
		final Thread t = new Thread(r, tName);

		t.setDaemon(daemon);
		t.setPriority(priority);

		return t;
	}
}
