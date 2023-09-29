/*
 * Copyright (C) 2015, 2023 Green Screens Ltd.
 */
package io.greenscreens.quark.ext.annotations;

import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.enterprise.util.Nonbinding;
import jakarta.inject.Qualifier;

/**
 * Used to link rest / websocket service to Controller Also used by descriptor
 * service to ba able to generate ExtJS dynamic code.
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ TYPE })
public @interface ExtJSDirect {

	/**
	 * if * then all matched; if not set then ignored and not generally available if
	 * set, must match corresponding UriInfo from rest service
	 * 
	 * @return
	 */
	@Nonbinding
	String[] paths();
}
