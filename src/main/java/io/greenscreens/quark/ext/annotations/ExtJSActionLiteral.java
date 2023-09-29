/*
 * Copyright (C) 2015, 2023 Green Screens Ltd.
 */
package io.greenscreens.quark.ext.annotations;

import jakarta.enterprise.inject.Vetoed;
import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.enterprise.util.Nonbinding;

/**
 * Internally used annotation wrapper used by CDI to find targeted bean methods
 */
@SuppressWarnings("all")
@Vetoed
public final class ExtJSActionLiteral extends AnnotationLiteral<ExtJSAction> implements ExtJSAction {

	private static final long serialVersionUID = 1L;

	private final String namespace;
	private final String action;

	public ExtJSActionLiteral(final String namespace, final String action) {
		super();
		this.action = action;
		this.namespace = namespace;
	}

	@Override
	@Nonbinding
	public String action() {
		return action;
	}

	@Override
	@Nonbinding
	public String namespace() {
		return namespace;
	}

}
