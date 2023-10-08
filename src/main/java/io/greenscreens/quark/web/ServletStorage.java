/*
 * Copyright (C) 2015, 2023 Green Screens Ltd.
 */
package io.greenscreens.quark.web;

import java.util.Objects;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * Servlet container data storage for Context, Sesssion, Request 
 */
public enum ServletStorage {
	;
	
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

	public static <T> T put(final ServletContext context, final T value) {
		if (Objects.nonNull(context) && Objects.nonNull(value)) {
			return put(context, value.getClass().getCanonicalName(), value);
		}
		return null;
	}
		
	public static <T> T put(final ServletContext context, final Class<T> clazz, final T value) {
		if (Objects.nonNull(context) && Objects.nonNull(clazz)) {
			return put(context, clazz.getCanonicalName(), value);
		}
		return null;
	}
	
	public static <T> T put(final ServletContext context, final String key, final T value) {
		if (Objects.nonNull(context) && Objects.nonNull(key) && Objects.nonNull(value)) {
			context.setAttribute(key, value);
			return value; 
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
		if (Objects.nonNull(request) && Objects.nonNull(key) && Objects.nonNull(value)) {
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

	public static <T> T remove(final ServletContext context, final Class<T> clazz) {
		if (Objects.nonNull(context) && Objects.nonNull(clazz)) {
			return remove(context, clazz.getCanonicalName());
		}
		return null;
	}
	
	public static <T> T remove(final ServletContext context, final String key) {
		final T val = get(context, key);
		if (Objects.nonNull(context) && Objects.nonNull(key)) {
			context.removeAttribute(key);
		}
		return val;
	}
	
	public static <T> T remove(final HttpServletRequest request, final Class<T> clazz) {
		if (Objects.nonNull(request) && Objects.nonNull(clazz)) {
			return remove(request, clazz.getCanonicalName());
		}
		return null;
	}
	
	public static <T> T remove(final HttpServletRequest request, final String key) {
		final T val = get(request, key);
		if (Objects.nonNull(request) && Objects.nonNull(key)) {
			request.removeAttribute(key);
		}
		return val;
	}

	public static <T> T remove(final HttpSession session, final T value) {
		if (Objects.nonNull(session) && Objects.nonNull(value)) {
			return remove(session, value.getClass().getCanonicalName());
		}
		return null;
	}
	
	public static <T> T remove(final HttpSession session, final Class<T> clazz) {
		if (Objects.nonNull(session) && Objects.nonNull(clazz)) {
			return remove(session, clazz.getCanonicalName());
		}
		return null;
	}

	public static <T> T remove(final HttpSession session, final String key) {
		final T val = get(session, key);
		if (Objects.nonNull(session) && Objects.nonNull(key)) {
			session.removeAttribute(key);
		}
		return val;
	}
	
}
