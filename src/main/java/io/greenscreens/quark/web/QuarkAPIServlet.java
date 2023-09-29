/*
 * Copyright (C) 2015, 2023 Green Screens Ltd.
 */
package io.greenscreens.quark.web;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.greenscreens.quark.QuarkUtil;
import io.greenscreens.quark.QuarkEngine;
import io.greenscreens.quark.QuarkStream;
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

	protected boolean isScript(final HttpServletRequest request) {
		final String path = request.getServletPath();
		return path.contains(".js");
	}

	protected boolean isModule(final HttpServletRequest request) {
		final String path = request.getServletPath();
		return path.contains(".mjs");
	}
	
	protected boolean isEngine(final HttpServletRequest request) {
		return isScript(request) || isModule(request);
	}
	
	protected void script(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
		response.setContentType("text/javascript");
		final ClassLoader loader = QuarkAPIServlet.class.getClassLoader();
		final boolean esm =  isModule(request);
		final String resource = getScript(esm);
		final InputStream inputStream = loader.getResourceAsStream(resource);
		if (Objects.nonNull(inputStream)) {
			QuarkStream.stream(inputStream, response.getOutputStream());
			inputStream.close();					
		}
	}

	protected void map(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
		response.setContentType("application/json");
		final ClassLoader loader = QuarkAPIServlet.class.getClassLoader();
		final String resource = getMap(request);
		final InputStream inputStream = loader.getResourceAsStream(resource);
		if (Objects.nonNull(inputStream)) {
			QuarkStream.stream(inputStream, response.getOutputStream());
			inputStream.close();					
		}
	}

	private String getMap(final HttpServletRequest req) {
		final String [] seg = req.getServletPath().split("/");
		return seg[seg.length-1];
	}

	private String getScript(final boolean esm) {
		return esm ? "io.greenscreens.quark.esm.min.js" : "io.greenscreens.quark.min.js";
	}
	
	/**
	 * Build default api list or filtered by path
	 * @param request
	 * @param response
	 * @param paths
	 */
	protected void build(final HttpServletRequest request, final HttpServletResponse response, final String[] paths) {
		final String challenge = QuarkUtil.normalize(request.getHeader("x-time"));
		final BeanManagerUtil bmu = QuarkEngine.getBean(BeanManagerUtil.class);
		final ArrayNode api = Objects.isNull(paths) ? bmu.getAPI() : bmu.build(paths);
		final ObjectNode root = QuarkUtil.buildAPI(api, challenge);
		final boolean compress = ServletUtils.supportGzip(request);
		ServletUtils.sendResponse(response, root, compress);
	}
	
	protected void services(final HttpServletRequest request, final HttpServletResponse response) {
		final List<String> list = QuarkEngine.getBean(BeanManagerUtil.class).services();
		final boolean compress = ServletUtils.supportGzip(request);
		ServletUtils.sendResponse(response, list, compress);
	}
	
	/**
	 * Post request will process non-encrypted / encrypted requests
	 */
	@Override
	protected void onPost(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
		QuarkHandler.call(request, response);
	}

	@Override
	protected void onDelete(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
		ServletUtils.remove(request.getSession(false), QuarkConstants.ENCRYPT_ENGINE);
		ServletUtils.writeResponse(response, "{}", false);
	}

}
