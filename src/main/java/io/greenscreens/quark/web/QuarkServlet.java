/*
 * Copyright (C) 2015, 2023 Green Screens Ltd.
 */
package io.greenscreens.quark.web;

import java.io.IOException;
import java.util.Optional;

import io.greenscreens.quark.util.MultipartMap;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

/**
 * Generic servlet that handle errors without sending full error stack trace to the front
 */
public class QuarkServlet extends HttpServlet {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

    protected Optional<Part> getPart(final HttpServletRequest request, final String name) throws IOException {      
        return ServletUtils.getPart(request, name);
    }
    
    protected Optional<MultipartMap> getMultipartMap(final HttpServletRequest request) throws IOException {
        return ServletUtils.getPut(request);
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
