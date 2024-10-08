/*
 * Copyright (C) 2015, 2023. Green Screens Ltd.
 */
package io.greenscreens.quark.web;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Generic filter that handle errors without sending full error stack trace to front
 */
public abstract class QuarkFilter extends HttpFilter {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Override
	protected void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException {
		try {
			onFilter(req, res, chain);
		} catch (Exception e) {
			ServletUtils.log(e, req, res);
		} finally {
			onFinish(req, res);
		}
	}
	
	protected void onFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException, ServletException {
		if (onFilter(req, res)) {
			chain.doFilter(req, res);		
		} else {
			ServletUtils.sendError(res, HttpServletResponse.SC_FORBIDDEN);
		}
	}

	protected abstract boolean onFilter(final HttpServletRequest req, final HttpServletResponse res) throws IOException;
	
	protected void onFinish(final HttpServletRequest req, final HttpServletResponse res) throws IOException {};

}
