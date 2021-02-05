package io.greenscreens.quark.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class QuarkRequest {

	private final HttpServletRequest request;	
	private final HttpServletResponse response;
	
	QuarkRequest(final HttpServletRequest request, final HttpServletResponse response) {
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

	public static QuarkRequest create(final HttpServletRequest request, final HttpServletResponse response) {
		return new QuarkRequest(request, response);
	} 
}
