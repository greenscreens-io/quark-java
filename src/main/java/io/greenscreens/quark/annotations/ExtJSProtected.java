/*
 * Copyright (C) 2015, 2022 Green Screens Ltd.
 */
package io.greenscreens.quark.annotations;

import static java.lang.annotation.ElementType.METHOD;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.greenscreens.quark.web.ServletUtils;

/**
 * Kill switch to disable sensitive functions on demand
 * If set on Controller method, call will be disabled when 
 * servletContext contains this class,
 * @see ServletUtils
 */
@Target(METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExtJSProtected {

}
