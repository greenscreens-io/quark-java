/*
 * Copyright (C) 2015, 2023. Green Screens Ltd.
 */
package io.greenscreens.quark.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * CDI injectable thread level request/response context.
 * Used only if Quark is used thorough servlet, not WebSocket 
 */
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
