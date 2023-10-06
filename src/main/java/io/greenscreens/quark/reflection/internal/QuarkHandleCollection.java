/*
 * Copyright (C) 2015, 2023 Green Screens Ltd.
 */
package io.greenscreens.quark.reflection.internal;

import java.util.concurrent.ConcurrentHashMap;

import io.greenscreens.quark.reflection.IQuarkHandle;

/**
 * A collection of mapped Bean MEthods with a reflection MethodHandle
 * Used as a cache for fast retrieval.
 */
final class QuarkHandleCollection extends ConcurrentHashMap<Integer, IQuarkHandle> {

	private static final long serialVersionUID = 1L;
	
}
