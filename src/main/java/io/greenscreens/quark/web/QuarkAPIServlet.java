/*
 * Copyright (C) 2015, 2022 Green Screens Ltd.
 */
package io.greenscreens.quark.web;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.greenscreens.quark.QuarkUtil;
import io.greenscreens.quark.QuarkEngine;
import io.greenscreens.quark.cdi.BeanManagerUtil;

/**
 * Servlet to render API structure
 */
public class QuarkAPIServlet extends QuarkServlet {

	private static final long serialVersionUID = 1L;

	public QuarkAPIServlet() {
		super();
	}

	/**
	 * GET request will export API and public key used for front initialization
	 */
	@Override
	protected void onGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		build(request, response, null);
	}

	protected void script(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
		response.setContentType("text/javascript");
		final ClassLoader loader = QuarkAPIServlet.class.getClassLoader();
		final boolean esm =  "true".equalsIgnoreCase(request.getParameter("esm"));
		final String resource = getScript(esm);
		final InputStream inputStream = loader.getResourceAsStream(resource);
		if (Objects.nonNull(inputStream)) {
			ServletUtils.stream(inputStream, response.getOutputStream());
			inputStream.close();					
		}
	}

	protected void map(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
		response.setContentType("application/json");
		final ClassLoader loader = QuarkAPIServlet.class.getClassLoader();
		final String resource = getMap(request);
		final InputStream inputStream = loader.getResourceAsStream(resource);
		if (Objects.nonNull(inputStream)) {
			ServletUtils.stream(inputStream, response.getOutputStream());
			inputStream.close();					
		}
	}

	private String getMap(final HttpServletRequest req) {
		final String [] seg = req.getServletPath().split("/");
		return seg[seg.length-1];
	}

	private String getScript(final boolean esm) {
		return esm ? "quark.esm.min.js" : "quark.min.js";
	}
	
	/**
	 * Build default api list or filtered by path
	 * @param request
	 * @param response
	 * @param paths
	 */
	protected void build(final HttpServletRequest request, final HttpServletResponse response, final String[] paths) {
		final String challenge = request.getHeader("x-time");
		final BeanManagerUtil bmu = QuarkEngine.getBean(BeanManagerUtil.class);
		final ArrayNode api = Objects.isNull(paths) ? bmu.getAPI() : bmu.build(paths);
		final ObjectNode root = QuarkUtil.buildAPI(api, challenge);
		ServletUtils.sendResponse(response, root);
	}
	
	protected void services(final HttpServletResponse response) {
		final List<String> list = QuarkEngine.getBean(BeanManagerUtil.class).services();
		ServletUtils.sendResponse(response, list);
	}
	
	/**
	 * Post request will process non-encrypted / encrypted requests
	 */
	@Override
	protected void onPost(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
		QuarkHandler.call(request, response);
	}

	@Override
	protected void onDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
		ServletUtils.remove(request.getSession(false), QuarkConstants.ENCRYPT_ENGINE);
		ServletUtils.writeResponse(response, "{}");
	}

}
