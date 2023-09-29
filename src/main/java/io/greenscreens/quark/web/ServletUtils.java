/*
 * Copyright (C) 2015, 2023 Green Screens Ltd.
 */
package io.greenscreens.quark.web;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Scanner;
import java.util.zip.GZIPOutputStream;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.greenscreens.quark.ByteBufferInputStream;
import io.greenscreens.quark.JsonDecoder;
import io.greenscreens.quark.QuarkCompression;
import io.greenscreens.quark.QuarkDecompression;
import io.greenscreens.quark.QuarkStream;
import io.greenscreens.quark.QuarkUtil;
import io.greenscreens.quark.ext.ExtJSProtected;

/**
 * General http request utils
 */
public enum ServletUtils {
	;
	
	private static final Logger LOG = LoggerFactory.getLogger(ServletUtils.class);
	
	/**
	 * @see ServletUtil.log
	 * @param e
	 * @param request
	 * @param response
	 */
	public static void log(final Throwable e, final HttpServletRequest request, final HttpServletResponse response) {
		log(LOG, e, request, response, true);
	}
	
	/**
	 * Log and send error response back to requester
	 * @param logger
	 * @param e
	 * @param request
	 * @param response
	 * @param details
	 */
	public static void log(final Logger logger,final Throwable e, final HttpServletRequest request, final HttpServletResponse response, final boolean details) {

		final String message = QuarkUtil.toMessage(e);
		if (details) {
			logger.error(message, e);
		} else {
			logger.error(message);
		}
		logger.debug(message, e);
		ServletUtils.sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message);		
	}

	/**
	 * Close all session stored object with auto-closable interface
	 * @param session
	 * @throws IOException
	 */
	public static void closeAll(final HttpSession session) {
    	final Enumeration<String> keys = session.getAttributeNames();
    	while (keys.hasMoreElements()) {
    		Object obj = remove(session, keys.nextElement());
    		if (obj instanceof AutoCloseable) {
    			QuarkUtil.close((AutoCloseable) obj);
    		}
    	}
	}
	
	/**
	 * Convert post request data to string
	 * 
	 * @param request
	 * @return
	 * @throws IOException
	 */
	public static String getBodyAsString(final HttpServletRequest request) throws IOException {

		final boolean isCompress = supportGzip(request);
		final InputStream is = request.getInputStream();
		
		if (isCompress) {
			return QuarkDecompression.asString(is, true);
		} else {
			return QuarkStream.asString(is, true);
		}
	}

	public static ByteBuffer getBodyAsBuffer(final HttpServletRequest request) throws IOException {

		final boolean isCompress = supportGzip(request);
		final InputStream is = request.getInputStream();

		if (isCompress) {
			return QuarkDecompression.asBuffer(is, true);
		} else {
			return QuarkStream.asBuffer(is, true);
		}

	}
	
	
	public static boolean supportGzip(final HttpServletRequest request) {
		final String val = QuarkUtil.normalize(request.getHeader("Content-Encoding"));
		return val.indexOf("gzip") != -1;
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

	public static Part getPart(final HttpServletRequest request, final String name) throws IOException {
		Part part = null;
		try {
			part = request.getPart(name);
		} catch (ServletException e) {
			throw new IOException(e);
		}
		return part;
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

		if (Objects.isNull(error)) return getResponse();

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

	public static <T> void sendResponse(final HttpServletResponse response, final T obj, final boolean compress) {
		
		String json = null;

		try {
			json = JsonDecoder.stringify(obj);
			response.setContentType("application/json");
			writeResponse(response, json, compress);
		} catch (JsonProcessingException e) {
			LOG.error("Failed to encode messages as JSON: {}", json, e);
			final ObjectNode jsonObject = ServletUtils.getResponse(false, e.getMessage());
			sendResponse(response, jsonObject, compress);
		}
	}

	/**
	 * Set json response data
	 */
	public static void sendResponse(final HttpServletResponse resp, final JsonNode json, final boolean compress) {
		resp.setContentType("application/json;charset=utf-8");
		writeResponse(resp, json.toString(), compress);
	}

	/**
	 * Send text data
	 * @param resp
	 * @param message
	 */
	public static void sendResponse(final HttpServletResponse resp, final String message, final boolean compress) {
		resp.setContentType("text/plain;charset=utf-8");
		writeResponse(resp, message, compress);
	}

	public static void sendResponse(final HttpServletResponse resp, final ByteBuffer message, final boolean compress) {
		resp.setContentType("application/octet-stream");
		writeResponse(resp, message, compress);
	}

	public static void sendResponse(final HttpServletResponse resp, final byte[] message, final boolean compress) {
		resp.setContentType("application/octet-stream");
		writeResponse(resp, message, compress);
	}
	
	/**
	 * Generic string write
	 * @param resp
	 * @param message
	 */
	public static void writeResponse(final HttpServletResponse resp, final String message, final boolean compress) {
		
		try {
			if (resp.isCommitted()) return;
			PrintWriter out = null; 
					
			if (compress) {
				resp.setHeader("Content-Encoding", "gzip");
				final OutputStream outStream = resp.getOutputStream();
			    out = new PrintWriter(new GZIPOutputStream(outStream), false);
			} else {				
				resp.setContentLength(message.length());
				out = resp.getWriter();			
			}
			out.print(QuarkUtil.normalize(message));
			out.flush();
			out.close();
        } catch (IOException e) {
        	final String msg = QuarkUtil.toMessage(e);
			LOG.error(msg);
			LOG.debug(msg, e);
        }
	}

	public static void writeResponse(final HttpServletResponse resp, final ByteBuffer message, final boolean compress) {
		if (resp.isCommitted()) return;		
		final ByteBufferInputStream inStream = new ByteBufferInputStream(message);
		writeResponse(resp, inStream, message.remaining(), compress);
	}
	
	public static void writeResponse(final HttpServletResponse resp, final byte[] message, final boolean compress) {
		if (resp.isCommitted()) return;
		final ByteArrayInputStream inStream = new ByteArrayInputStream(message);
		writeResponse(resp, inStream, message.length, compress);
	}
	
	public static void writeResponse(final HttpServletResponse resp, final InputStream inStream, final int length, final boolean compress) {
		
		try {
			if (resp.isCommitted()) return;
					
			final OutputStream outStream = resp.getOutputStream();

			if (compress) {
				resp.setHeader("Content-Encoding", "gzip");
				QuarkCompression.stream(inStream, outStream, false);
			} else {				
				resp.setContentLength(length);
				QuarkStream.stream(inStream, outStream);
			}
			
			outStream.flush();
			outStream.close();
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
	 * @param message
	 */
	public static void sendError(final ServletResponse response, final int code, final String message) {
		sendError(wrap(response), code, message);
	}
	
	public static void sendError(final HttpServletResponse response, final int code, final String message) {
		if (!response.isCommitted()) {
			response.setStatus(code);
			writeResponse(response, message, false);
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

		if (Objects.isNull(part)) return null;

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
		if (Objects.nonNull(request) && Objects.nonNull(clazz)) {
			return get(request, clazz.getCanonicalName());
		}
		return null;
	}

	public static <T> T get(final HttpSession session, final Class<T> clazz) {
		if (Objects.nonNull(session) && Objects.nonNull(clazz)) {
			return get(session, clazz.getCanonicalName());
		}
		return null;
	}
	
	public static <T> T get(final ServletContext context, final Class<T> clazz) {
		if (Objects.nonNull(context) && Objects.nonNull(clazz)) {
			return get(context, clazz.getCanonicalName());
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public static <T> T get(final HttpServletRequest request, final String key) {
		if (Objects.nonNull(request) && Objects.nonNull(key)) {
			return (T) request.getAttribute(key);
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T get(final HttpSession session, final String key) {
		if (Objects.nonNull(session) && Objects.nonNull(key)) {
			return (T) session.getAttribute(key);
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T get(final ServletContext context, final String key) {
		if (Objects.nonNull(context) && Objects.nonNull(key)) {
			return (T) context.getAttribute(key);
		}
		return null;
	}

	public static <T> T put(final HttpServletRequest request, final T value) {
		if (Objects.nonNull(request) && Objects.nonNull(value)) {
			return put(request, value.getClass().getCanonicalName(), value);
		}
		return null;
	}
		
	public static <T> T put(final HttpServletRequest request, final Class<T> clazz, final T value) {
		if (Objects.nonNull(request) && Objects.nonNull(clazz)) {
			return put(request, clazz.getCanonicalName(), value);
		}
		return null;
	}
	
	public static <T> T put(final HttpServletRequest request, final String key, final T value) {
		if (request != null && key!= null && value != null) {
			request.setAttribute(key, value);
			return value; 
		}
		return null;
	}
	
	
	public static <T> T put(final HttpSession session, final T value) {
		if (Objects.nonNull(session) && Objects.nonNull(value)) {
			return put(session, value.getClass().getCanonicalName(), value);
		}
		return null;
	}
		
	public static <T> T put(final HttpSession session, final Class<T> clazz, final T value) {
		if (Objects.nonNull(session) && Objects.nonNull(clazz)) {
			return put(session, clazz.getCanonicalName(), value);
		}
		return null;
	}
	
	public static <T> T put(final HttpSession session, final String key, final T value) {
		if (Objects.nonNull(session) && Objects.nonNull(key) && Objects.nonNull(value)) {
			session.setAttribute(key, value);
			return value; 
		}
		return null;
	}

	/**
	 * Remove value from servlet context safe. Key is class canonical name.
	 * @param <T>
	 * @param context
	 * @param clazz
	 * @return
	 */
	public static <T> T remove(final ServletContext context, final Class<T> clazz) {
		if (Objects.nonNull(context) && Objects.nonNull(clazz)) {
			return remove(context, clazz.getCanonicalName());
		}
		return null;
	}
	
	/**
	 * Remove value from servlet context safe. 
	 * @param <T>
	 * @param context
	 * @param key
	 * @return
	 */
	public static <T> T remove(final ServletContext context, final String key) {
		final T val = get(context, key);
		if (Objects.nonNull(context) && Objects.nonNull(key)) {
			context.removeAttribute(key);
		}
		return val;
	}
	
	/**
	 * Remove value from request safe. Key is class canonical name.
	 * @param <T>
	 * @param request
	 * @param clazz
	 * @return
	 */
	public static <T> T remove(final HttpServletRequest request, final Class<T> clazz) {
		if (Objects.nonNull(request) && Objects.nonNull(clazz)) {
			return remove(request, clazz.getCanonicalName());
		}
		return null;
	}
	
	/**
	 * Remove value from request safe.
	 * @param <T>
	 * @param request
	 * @param key
	 * @return
	 */
	public static <T> T remove(final HttpServletRequest request, final String key) {
		final T val = get(request, key);
		if (Objects.nonNull(request) && Objects.nonNull(key)) {
			request.removeAttribute(key);
		}
		return val;
	}

	/**
	 * Remove value from session if session exists. Key is value class canonical name
	 * @param <T>
	 * @param session
	 * @param value
	 * @return
	 */
	public static <T> T remove(final HttpSession session, final T value) {
		if (Objects.nonNull(session) && Objects.nonNull(value)) {
			return remove(session, value.getClass().getCanonicalName());
		}
		return null;
	}
	
	/**
	 * Remove value from session if session exists. Key is class canonical name
	 * @param <T>
	 * @param session
	 * @param clazz
	 * @return
	 */
	public static <T> T remove(final HttpSession session, final Class<T> clazz) {
		if (Objects.nonNull(session) && Objects.nonNull(clazz)) {
			return remove(session, clazz.getCanonicalName());
		}
		return null;
	}
	
	/**
	 * Remove value from session if session exists 
	 * @param <T>
	 * @param session
	 * @param key
	 * @return
	 */
	public static <T> T remove(final HttpSession session, final String key) {
		final T val = get(session, key);
		if (Objects.nonNull(session) && Objects.nonNull(key)) {
			session.removeAttribute(key);
		}
		return val;
	}
	
	/**
	 * Invalidate session if exists
	 * @param request
	 * @return
	 */
	public static boolean invalidate(final HttpServletRequest request) {
		if (Objects.nonNull(request)) return invalidate(request.getSession(false));
		return false;
	}
	
	/**
	 * Invalidate session safe
	 * @param session
	 * @return
	 */
	public static boolean invalidate(final HttpSession session) {
		if (Objects.nonNull(session)) {
			session.invalidate();
			return true;
		}
		return false;
	}
	
	/**
	 * Check if Quark API processing is disabled
	 * @param context
	 * @return
	 */
	public static boolean isDisabled(final ServletContext context) {
		final String key = ExtJSProtected.class.getCanonicalName();
		final Boolean o1 = ServletUtils.get(context, key);
		return Objects.nonNull(o1) && o1.booleanValue();
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
	
	/**
	 * Check if request came from https protocol
	 * @param request
	 * @return
	 */
	public static boolean isSecure(final HttpServletRequest request) {
		return request.isSecure() || "https".equalsIgnoreCase(request.getHeader("X-Forwarded-Proto"));
	}
	
	/**
	 * List all HTTP headers into json
	 * @param request
	 * @return
	 */
	public static ObjectNode getHeaders(final HttpServletRequest request) {
		
		final JsonNodeFactory factory = JsonNodeFactory.instance;
		final ObjectNode root = factory.objectNode();
		
		Enumeration<String> values = null;
		String name = null;
		String value = null;
		final Enumeration<String> names = request.getHeaderNames();
		while (names.hasMoreElements()) {
			name = names.nextElement();
			if (name.equalsIgnoreCase("cookie")) continue;
			values = request.getHeaders(name);
			value = null;
			while (values.hasMoreElements()) {
				value = Objects.isNull(value) ? values.nextElement() : value.concat("; ").concat(values.nextElement());
			}
			root.put(name, value);
		}
		
		return root;
	}
	

	public static String getCookie(final HttpServletRequest request, final String key) {
		if (QuarkUtil.nonEmpty(key) && Objects.nonNull(request.getCookies())) {
			final List<Cookie> cookies = Arrays.asList(request.getCookies());
			final Optional<Cookie> cookie = cookies.stream().filter(c -> QuarkConstants.WEB_KEY.equals(c.getName())).findFirst();
			if (cookie.isPresent()) return cookie.get().getValue();
		}
		return null;
	}	

	/**
	 * Parse browser received cookie strings
	 * 
	 * @param cookies
	 * @return
	 */
	public static Map<String, String> parseCookies(final List<String> cookies) {


		if (Objects.isNull(cookies)) return Collections.emptyMap();

		final Map<String, String> map = new HashMap<>();
		Scanner scan = null;
		String[] pair = null;

		for (String cookie : cookies) {

			try {

				scan = new Scanner(cookie);
				scan.useDelimiter(";");

				while (scan.hasNext()) {
					pair = scan.next().split("=");
					if (pair.length > 1) {
						map.put(QuarkUtil.normalize(pair[0]), pair[1]);
					}
				}

			} finally {
				QuarkUtil.close(scan);
			}

		}

		return Collections.unmodifiableMap(map);
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends ServletRequest> T wrap(final ServletRequest request) {
		return (T) request;
	}

	@SuppressWarnings("unchecked")
	public static <T extends ServletResponse> T wrap(final ServletResponse response) {
		return (T) response;
	}
}
