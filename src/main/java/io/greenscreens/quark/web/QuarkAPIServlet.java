/*
 * Copyright (C) 2015, 2023. Green Screens Ltd.
 */
package io.greenscreens.quark.web;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.greenscreens.quark.QuarkEngine;
import io.greenscreens.quark.cdi.BeanManagerUtil;
import io.greenscreens.quark.internal.QuarkBuilder;
import io.greenscreens.quark.internal.QuarkConstants;
import io.greenscreens.quark.internal.QuarkHandler;
import io.greenscreens.quark.internal.QuarkHandlerUtil;
import io.greenscreens.quark.security.IQuarkKey;
import io.greenscreens.quark.security.QuarkSecurity;
import io.greenscreens.quark.stream.QuarkStream;
import io.greenscreens.quark.utils.QuarkJson;
import io.greenscreens.quark.utils.QuarkUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Servlet to render API structure
 */
public class QuarkAPIServlet extends QuarkServlet {

	private static final long serialVersionUID = 1L;

	public QuarkAPIServlet() {
		super();
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
	 * @throws IOException 
	 */
	protected void build(final HttpServletRequest request, final HttpServletResponse response, final Collection<String> uri) throws IOException {
		final ArrayNode api = Objects.isNull(uri) || uri.isEmpty() ? QuarkEngine.getBean(BeanManagerUtil.class).getAPI() : QuarkBuilder.build(uri);
		final String challenge = QuarkUtil.normalize(request.getHeader(QuarkConstants.CHALLENGE));
		final ObjectNode root = QuarkBuilder.buildAPI(api, challenge);
		final boolean compress = ServletUtils.supportGzip(request);
		// ServletUtils.sendResponse(response, root, compress);
		
		final String publicKey = QuarkHandlerUtil.getPublicKey(request);			
		final IQuarkKey aes = QuarkSecurity.initWebKey(publicKey);
		final String json = QuarkJson.stringify(root);			
		final ByteBuffer buff = QuarkStream.wrap(json, aes, compress, root);
		ServletUtils.sendResponse(response, buff, false);
	}
	
	protected void build(final HttpServletRequest request, final HttpServletResponse response, final String uri) throws IOException {
		build(request, response, Arrays.asList(uri));
	}
	
	protected void services(final HttpServletRequest request, final HttpServletResponse response) {
		final List<String> list = QuarkBuilder.services();
		final boolean compress = ServletUtils.supportGzip(request);
		ServletUtils.sendResponse(response, list, compress);
	}

	/**
	 * GET request will export API and public key used for front initialization
	 */
	@Override
	protected void onGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		build(request, response, Collections.emptyList());
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
		ServletStorage.remove(request.getSession(false), QuarkConstants.ENCRYPT_ENGINE);
		ServletUtils.writeResponse(response, "{}", false);
	}

}
