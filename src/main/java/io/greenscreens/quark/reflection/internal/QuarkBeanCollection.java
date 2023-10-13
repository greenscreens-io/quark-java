/*
 * Copyright (C) 2015, 2023. Green Screens Ltd.
 */
package io.greenscreens.quark.reflection.internal;

import java.util.ArrayList;

import io.greenscreens.quark.reflection.IQuarkBean;

/**
 * A collection of mapped CDI beans with attached reflection
 * Used as a memory cache for fast retrieval.
 */
final class QuarkBeanCollection extends ArrayList<IQuarkBean> {

	private static final long serialVersionUID = 1L;
	
}
