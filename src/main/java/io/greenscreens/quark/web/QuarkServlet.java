/*
 * Copyright (C) 2015, 2016  Green Screens Ltd.
 */
package io.greenscreens.quark.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

/**
 * Generic servlet that handle errors without sending full error stack trace to front
 */
public class QuarkServlet extends HttpServlet {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected Part getPart(final HttpServletRequest request, final String name) throws IOException {		
		return ServletUtils.getPart(request, name);
	}
	
	protected MultipartMap getMultipartMap(final HttpServletRequest request) throws IOException {
		return ServletUtils.getPut(request, this);
	}
	
	protected void updateHeaders(final HttpServletResponse response) {
		response.setHeader("X-Content-Type-Options", "'nosniff'");
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		updateHeaders(resp);
		super.service(req, resp);
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) {

		try {
			onGet(request, response);
		} catch (Throwable e) {
			ServletUtils.log(e, request, response);
		}

	}

	@Override
	protected void doPut(HttpServletRequest request, HttpServletResponse response) {

		try {
			onPut(request, response);
		} catch (Throwable e) {
			ServletUtils.log(e, request, response);
		}

	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) {

		try {
			onPost(request, response);
		} catch (Throwable e) {
			ServletUtils.log(e, request, response);
		}
		
	}

	@Override
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) {

		try {
			onDelete(request, response);
		} catch (Throwable e) {
			ServletUtils.log(e, request, response);
		}
		
	}

	protected void onDelete(HttpServletRequest request, HttpServletResponse response)  throws IOException, ServletException {
		super.doDelete(request, response);
	}

	protected void onPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		super.doPost(request, response);
	}

	protected void onPut(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		super.doPut(request, response);		
	}

	protected void onGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		super.doGet(request, response);
	}

}
