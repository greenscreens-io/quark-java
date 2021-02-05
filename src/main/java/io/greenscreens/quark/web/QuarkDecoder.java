/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 * 
 * https://www.greenscreens.io
 * 
 */
package io.greenscreens.quark.web;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.greenscreens.quark.JsonDecoder;
import io.greenscreens.quark.QuarkSecurity;
import io.greenscreens.quark.QuarkUtil;
import io.greenscreens.quark.ext.ExtJSProtected;
import io.greenscreens.quark.security.IAesKey;


/**
 * Quark data format parser / decoder
 */
public enum QuarkDecoder {
	;
	
	private static final Logger LOG = LoggerFactory.getLogger(QuarkDecoder.class);

	private static boolean isWebCrypto(final HttpServletRequest req) {
		final String t = req.getParameter("t");
		return "1".equals(t);
	}

	/**
	 * Decode encrypted request from query parameters
	 * 
	 * @param req
	 * @return
	 * @throws IOException
	 */
	public static JsonNode decodeRaw(final HttpServletRequest req) {
		
		if ("GET".equalsIgnoreCase(req.getMethod())) {
			return decodeRawGet(req);
		} else if ("POST".equalsIgnoreCase(req.getMethod())) {
			return decodeRawPost(req);
		} 
		return null;
	}
	

	/**
	 * Decode encrypted request from raw strings
	 * 
	 * @param d
	 * @param k
	 * @return
	 * @throws IOException
	 */
	public static JsonNode decodeRaw(final String d, final String k, final boolean webCryptoAPI) {

		LOG.debug("decodeRaw d: {}, k: {}", d, k);

		JsonNode node = null;

		if (isEncrypted(d, k)) {

			try {
				final IAesKey crypt = QuarkSecurity.initAES(k, webCryptoAPI);
				final String data = crypt.decrypt(d);
				LOG.debug("decodeRaw decoded : {}", data);

				node = JsonDecoder.parse(data);

			} catch (Exception e) {
				final String msg = QuarkUtil.toMessage(e);
				LOG.error(msg);
				LOG.debug(msg, e);
			}

		}

		return node;
	}

	public static JsonNode decodeRawGet(final HttpServletRequest req) {
		final String d = req.getParameter("d");
		final String k = req.getParameter("k");
		final String v = req.getParameter("v");
		final boolean webCryptoAPI = isWebCrypto(req);
		final JsonNode node = decodeRaw(d, k, webCryptoAPI);
		if (node != null) ((ObjectNode) node).put("v", v);
		return node;

	}

	/**
	 * Get JSON d & k encrypted data
	 * 
	 * @param request
	 * @return
	 * @throws IOException
	 */
	public static JsonNode decodeRawPut(final MultipartMap map) {

		final String d = map.getParameter("d");
		final String k = map.getParameter("k");
		final String v = map.getParameter("v");
		final boolean webCryptoAPI = "1".equals(map.getParameter("t"));

		final JsonNode node = decodeRaw(d, k, webCryptoAPI);
		if (node != null) ((ObjectNode) node).put("v", v);
		return node;
	}

	/**
	 * Get JSON d & k encrypted data
	 * 
	 * @param request
	 * @return
	 * @throws IOException
	 */
	public static JsonNode decodeRawPost(final HttpServletRequest request) {

		JsonNode node = null;
		JsonNode node2 = null;

		try {

			// parse json text to encrypted json object
			node = ServletUtils.getPost(request);

			// get node encrypted values
			final String d = JsonDecoder.getStr(node, "d");
			final String k = JsonDecoder.getStr(node, "k");
			final boolean webCryptoAPI = JsonDecoder.getInt(node, "t") == 1;
			final int v = JsonDecoder.getInt(node, "v");
			
			// decode encrypted json
			node2 = decodeRaw(d, k, webCryptoAPI);
			if (node2 != null) node = node2;
			if (node != null) ((ObjectNode) node).put("v", v);
			
		} catch (Exception e) {
			final String msg = QuarkUtil.toMessage(e);
			LOG.error(msg);
			LOG.debug(msg, e);
		}

		return node;
	}

	/**
	 * Check security flag to disable protected controllers
	 * @param context
	 * @return
	 */
	public static boolean isDisabled(final ServletContext context) {
		final String key = ExtJSProtected.class.getCanonicalName();
		final Boolean o1 = ServletUtils.get(context, key);
		return  o1 == null || o1.booleanValue();
	}
	
	/**
	 * Set security flag to disable protected controllers
	 * @param context
	 * @param sts
	 */
	public static void setDisabled(final ServletContext context, final boolean sts) {
		final String key = ExtJSProtected.class.getCanonicalName();
		context.setAttribute(key, sts ? Boolean.TRUE : Boolean.FALSE);
	}
	
	public static boolean isEncrypted(final String d, final String k) {
		return !QuarkUtil.isEmpty(d) && !QuarkUtil.isEmpty(k);
	}
}
