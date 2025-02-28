/*
 * Copyright (C) 2015, 2023. Green Screens Ltd.
 */
package io.greenscreens.quark.annotations;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.enterprise.util.Nonbinding;

/**
 * Used to define remotely available action method. Name must match name from
 * incoming ExtJS.Direct call
 */
@Target({ METHOD, TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ExtJSAsync {
	
	/**
	 * Run async code in virtual thread 
	 * @return
	 */
	@Nonbinding
	boolean virtual() default true;

}
