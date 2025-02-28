/*
 * Copyright (C) 2015, 2023 Green Screens Ltd.
 */
package io.greenscreens.quark.cdi;

import static java.lang.annotation.ElementType.PARAMETER;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Controller method parameter annotations used only if controller parameter is required value.
 * Quark engine will automatically respond to a requester with error.
 * Prevents entry into a method if value is null. 
 */
@Target({ PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface Required {

}
