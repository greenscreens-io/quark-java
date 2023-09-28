/*
 * Copyright (C) 2015, 2023 Green Screens Ltd.
 */
package io.greenscreens.quark.web.listener;

import java.util.Set;

import javax.enterprise.inject.Vetoed;
import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

/**
 * Programmatically initialize Quark engine web elements
 */
@Vetoed
public class QuarkContainerInitializer implements ServletContainerInitializer {

	@Override
	public void onStartup(Set<Class<?>> c, ServletContext ctx) throws ServletException {
		ctx.addListener(QuarkWebSessionListener.class);
	}

}
