/*
 * Copyright (C) 2015, 2023 Green Screens Ltd.
 */
package io.greenscreens.quark.ext.annotations;

import jakarta.enterprise.inject.Vetoed;
import jakarta.enterprise.util.AnnotationLiteral;

/**
 * Internally used annotation wrapper used by CDI to find targeted beans
 */
@SuppressWarnings("all")
@Vetoed
public class ExtJSDirectLiteral extends AnnotationLiteral<ExtJSDirect> implements ExtJSDirect {

	private static final long serialVersionUID = 1L;

	String[] paths = {};

	public ExtJSDirectLiteral(String[] paths) {
		super();
		this.paths = paths;
	}

	public String[] paths() {
		return paths;
	}

}
