/*
 * Copyright (C) 2015, 2023. Green Screens Ltd.
 */
package io.greenscreens.quark.utils;

import java.util.concurrent.ThreadFactory;
import jakarta.enterprise.inject.Vetoed;

/**
 * Helper class to crate named threads for easier monitoring.  
 */
@Vetoed
public class NamedThreadFactory {

	/**
	 * Crate a new named Thread factory with defined processor execution priority.
	 * @param name
	 * @param priority
	 * @return
	 */
	public static ThreadFactory get(final String name, final int priority) {
		return new NamedThread(name, priority, true);
	}

}
