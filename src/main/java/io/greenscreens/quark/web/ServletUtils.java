/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 * 
 * https://www.greenscreens.io
 * 
 */
package io.greenscreens.quark.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Objects;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.greenscreens.quark.JsonDecoder;
import io.greenscreens.quark.QuarkUtil;
import io.greenscreens.quark.ext.ExtJSProtected;

/**
 * General http request utils
 */
public enum ServletUtils {
	;
	
	private static final Logger LOG = LoggerFactory.getLogger(ServletUtils.class);

	private static final String IP_LOG_MSG = "Request from: {}";
	
	public static void log(final Exception e, final HttpServletRequest request, final HttpServletResponse response) {
		log(LOG, e, request, response, true);
	}
	
	public static void log(final Logger logger,final Exception e, final HttpServletRequest request, final HttpServletResponse response, final boolean details) {
		final String clientip = request.getRemoteAddr();
		final String message = QuarkUtil.toMessage(e);
		logger.error(IP_LOG_MSG, clientip);
		if (details) {
			logger.error(message, e);
		} else {
			logger.error(message);
		}
		logger.debug(message, e);
		ServletUtils.sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message);		
	}


	/**
	 * Convert post request data to string
	 * 
	 * @param request
	 * @return
	 * @throws IOException
	 */
	public static String getBodyAsString(final HttpServletRequest request) throws IOException {

		final StringBuilder sb = new StringBuilder();
		final BufferedReader reader = request.getReader();

		try {
			String line;
			while ((line = reader.readLine()) != null) {
				sb.append(line).append('\n');
			}
		} finally {
			reader.close();
		}

		return sb.toString();
	}

	/**
	 * Set browser no caching flags
	 */
	public static void setNoCache(final HttpServletResponse resp) {
		resp.setHeader("Cache-control", "no-cache, no-store");
		resp.setHeader("Pragma", "no-cache");
		resp.setHeader("Expires", "-1");
		resp.setHeader("X-Content-Type-Options", "'nosniff'");
	}

	/**
	 * Get JSON d & k encrypted data
	 * 
	 * @param request
	 * @return
	 * @throws IOException
	 */
	public static JsonNode getPost(final HttpServletRequest request) throws IOException {

		String json = null;
		JsonNode node = null;

		try {
			// read text from form post - must be json
			json = ServletUtils.getBodyAsString(request);

			// parse json text to encrypted json object
			node = JsonDecoder.parse(json);

		} catch (Exception e) {
			throw new IOException(e);
		}

		return node;
	}


	public static MultipartMap getPut(final HttpServletRequest request, final HttpServlet servlet) throws IOException {
		
		MultipartMap map = null;
		try {
			map = new MultipartMap(request, servlet);
		} catch (ServletException e) {
			throw new IOException(e);
		}
		return map;
	}

	/**
	 * Send public key and server timestamp
	 * 
	 * @param sts
	 * @param err
	 * @return
	 */
	public static ObjectNode getResponse() {
		return getResponse(true, null, null);
	}

	/**
	 * Create JSON error response in engine JSON format
	 * 
	 * @param error
	 * @return
	 */
	public static ObjectNode getResponse(final QuarkErrors error) {

		if (error == null) {
			return getResponse();
		}

		return getResponse(false, error.getString(), error.getCode());
	}

	/**
	 * Create JSON response in engine JSON format
	 * 
	 * @param sts
	 * @param error
	 * @return
	 */
	public static ObjectNode getResponse(final boolean sts, final String error) {
		return getResponse(sts, error, QuarkErrors.E9999.getCode());
	}

	/**
	 * Create JSON response in engine JSON format
	 * 
	 * @param sts
	 * @param err
	 * @param code
	 * @return
	 */
	public static ObjectNode getResponse(final boolean sts, final String err, final String code) {

		final JsonNodeFactory factory = JsonNodeFactory.instance;
		final ObjectNode root = factory.objectNode();

		root.put("success", sts);
		root.put("ver", 0);
		root.put("ts", System.currentTimeMillis());

		if (!sts) {
			root.put("error", err);
			root.put("code", code);
		}

		return root;
	}

	public static <T> void sendResponse(final HttpServletResponse response, final T obj) {
		
		String json = null;

		try {
			json = JsonDecoder.stringify(obj);
			response.setContentType("application/json");
			writeResponse(response, json);
		} catch (JsonProcessingException e) {
			LOG.error("Failed to encode messages as JSON: {}", json, e);
			final ObjectNode jsonObject = ServletUtils.getResponse(false, e.getMessage());
			sendResponse(response, jsonObject);
		}
	}

	/**
	 * Set json response data
	 */
	public static void sendResponse(final HttpServletResponse resp, final JsonNode json) {
		resp.setContentType("application/json;charset=utf-8");
		ServletUtils.writeResponse(resp, json.toString());
	}

	/**
	 * Send text data
	 * @param resp
	 * @param message
	 */
	public static void sendResponse(final HttpServletResponse resp, final String message) {
		resp.setContentType("text/plain;charset=utf-8");
		ServletUtils.writeResponse(resp, message);
	}

	/**
	 * Generic string write
	 * @param resp
	 * @param message
	 */
	public static void writeResponse(final HttpServletResponse resp, final String message) {
		
		try {
			if (resp.isCommitted()) return;
			final PrintWriter out = resp.getWriter();			
			out.print(QuarkUtil.normalize(message));
			out.flush();		
        } catch (IOException e) {
        	final String msg = QuarkUtil.toMessage(e);
			LOG.error(msg);
			LOG.debug(msg, e);
        }
	}

	public static long stream(final InputStream input, final OutputStream output) throws IOException {
	    try (
	        ReadableByteChannel inputChannel = Channels.newChannel(input);
	        WritableByteChannel outputChannel = Channels.newChannel(output);
	    ) {
	        ByteBuffer buffer = ByteBuffer.allocateDirect(10240);
	        long size = 0;

	        while (inputChannel.read(buffer) != -1) {
	            buffer.flip();
	            size += outputChannel.write(buffer);
	            buffer.clear();
	        }

	        return size;
	    }
	}
	
	/**
	 * Safe way to send error without re-throwing 
	 * @param response
	 * @param code
	 * @param message
	 */
	public static void sendError(final HttpServletResponse response, final int code, final String message) {
		try {
			if (!response.isCommitted()) {
				response.setStatus(code);
				final PrintWriter writer = response.getWriter();
				writer.write(QuarkUtil.normalize(message));
				writer.flush();
				//response.sendError(code, StringUtil.normalize(message));
			}
		} catch (IOException e) {
			final String msg = QuarkUtil.toMessage(e);
			LOG.error(msg);
			LOG.debug(msg, e);
		}
	}

	/**
	 * Safe way to send error without re-throwing
	 * @param response
	 * @param code
	 */
	public static void sendError(final HttpServletResponse response, final int code) {
		sendError(response, code, "");
	}


	/**
	 * Get upload file name
	 * 
	 * @param part
	 * @return
	 */
	public static String getFileName(final Part part) {

		if (part == null) {
			return null;
		}

		final String partHeader = part.getHeader("content-disposition");
		String fname = null;

		for (String content : partHeader.split(";")) {
			if (content.trim().startsWith("filename")) {
				fname = content.substring(content.indexOf('=') + 1).trim().replace("\"", "");
				// First fix stupid MSIE behaviour (it passes full client side path along
				// filename).
				fname = fname.substring(fname.lastIndexOf('/') + 1).substring(fname.lastIndexOf('\\') + 1);
				break;
			}
		}

		return fname;
	}

	/**
	 * Get file extension
	 * 
	 * @param file
	 * @return
	 */
	public static String getFileExt(final String file) {

		String ext = null;
		String[] segs = file.split("\\.");

		if (segs.length > 1) {
			ext = segs[segs.length - 1].toLowerCase();
		}

		return ext;
	}

	public static <T> T get(final HttpServletRequest request, final Class<T> clazz) {
		if (request != null && clazz != null) {
			return get(request, clazz.getCanonicalName());
		}
		return null;
	}

	public static <T> T get(final HttpSession session, final Class<T> clazz) {
		if (session != null && clazz != null) {
			return get(session, clazz.getCanonicalName());
		}
		return null;
	}
	
	public static <T> T get(final ServletContext context, final Class<T> clazz) {
		if (context != null && clazz != null) {
			return get(context, clazz.getCanonicalName());
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public static <T> T get(final HttpServletRequest request, final String key) {
		if (request != null && key!= null ) {
			return (T) request.getAttribute(key);
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T get(final HttpSession session, final String key) {
		if (session != null && key!= null) {
			return (T) session.getAttribute(key);
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T get(final ServletContext context, final String key) {
		if (context != null && key!= null) {
			return (T) context.getAttribute(key);
		}
		return null;
	}
	
	public static <T> T put(final HttpSession session, final String key, final T value) {
		if (session != null && key!= null && value != null) {
			session.setAttribute(key, value);
			return value; 
		}
		return null;
	}
	
	public static <T> T remove(final HttpSession session, final String key) {
		if (session != null && key!= null) {
			session.removeAttribute(key);
		}
		return null;
	}
	
	/**
	 * Check if Quark API processing is disabled
	 * @param context
	 * @return
	 */
	public static boolean isDisabled(final ServletContext context) {
		final String key = ExtJSProtected.class.getCanonicalName();
		final Boolean o1 = ServletUtils.get(context, key);
		return  Objects.nonNull(o1) && o1.booleanValue();
	}
	
	/**
	 * Enable or disable Quark API Processsing
	 * @param context
	 * @param sts
	 */
	public static void setDisabled(final ServletContext context, final boolean sts) {
		final String key = ExtJSProtected.class.getCanonicalName();
		context.setAttribute(key, sts ? Boolean.TRUE : Boolean.FALSE);
	}
}
