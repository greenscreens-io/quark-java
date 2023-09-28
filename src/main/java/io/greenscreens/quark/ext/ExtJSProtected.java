/*
 * Copyright (C) 2015, 2023 Green Screens Ltd.
 */
package io.greenscreens.quark.ext;

import static java.lang.annotation.ElementType.METHOD;

import java.lang.annotation.Target;

/**
 * Used for offline demo mode
 * If set on Controller method, call will be disabled
 */
@Target(METHOD)
public @interface ExtJSProtected {

}
