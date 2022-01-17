/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 * 
 * https://www.greenscreens.io
 * 
 */
package io.greenscreens.quark.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class QuarkContext {

	private final HttpServletRequest request;	
	private final HttpServletResponse response;
	
	QuarkContext(final HttpServletRequest request, final HttpServletResponse response) {
		super();
		this.request = request;
		this.response = response;
	}
	
	public HttpServletRequest getRequest() {
		return request;
	}

	public HttpServletResponse getResponse() {
		return response;
	}

	public static QuarkContext create(final HttpServletRequest request, final HttpServletResponse response) {
		return new QuarkContext(request, response);
	} 
}
