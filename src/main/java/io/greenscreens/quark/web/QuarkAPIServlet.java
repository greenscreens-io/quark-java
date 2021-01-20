/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 * 
 * https://www.greenscreens.io
 * 
 */
package io.greenscreens.quark.web;

import java.io.IOException;
import java.util.Objects;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.greenscreens.quark.QuarkUtil;
import io.greenscreens.quark.cdi.BeanManagerUtil;

/**
 * Servlet to render API structure
 */
public class QuarkAPIServlet extends QuarkServlet {

	private static final long serialVersionUID = 1L;

	@Inject
	public BeanManagerUtil beanManagerUtil;

	public QuarkAPIServlet() {
		super();
	}

	/**
	 * GET request will export API and public key used for front initialization
	 */
	@Override
	protected void onGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		build(request,  response, null);
	}

	/**
	 * Build dfault api list or filtered by path
	 * @param request
	 * @param response
	 * @param paths
	 */
	protected void build(final HttpServletRequest request, final HttpServletResponse response, final String[] paths) {
		final String challenge = request.getHeader("x-time");
		final ArrayNode api = Objects.isNull(paths) ? beanManagerUtil.getAPI() : beanManagerUtil.build(paths);
		final ObjectNode root = QuarkUtil.buildAPI(api, challenge);
		ServletUtils.sendResponse(response, root);
	}
	
	/**
	 * Post request will process non-encrypted / encrypted requests
	 */
	@Override
	protected void onPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		QuarkHandler.process(request, response, false);
	}

	@Override
	protected void onDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
		ServletUtils.remove(request.getSession(false), QuarkConstants.HTTP_SEESION_ENCRYPT);
		ServletUtils.writeResponse(response, "{}");
	}

}
