/*
 * Copyright (C) 2015, 2023. Green Screens Ltd.
 */
package io.greenscreens.quark.web;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

import io.greenscreens.quark.util.QuarkUtil;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public enum QuarkCookieUtil {
;

	public static Cookie getCookie(final ServletRequest req, final String name) {
		final HttpServletRequest httpReq = ServletUtils.wrap(req); 
		return getCookie(httpReq, name);
	}
	
	public static Cookie getCookie(final HttpServletRequest req, final String name) {
		final Cookie [] cookies = req.getCookies();
		if (Objects.isNull(cookies)) return null;
		for (Cookie cookie : cookies) {
			if (cookie.getName().equals(name)) return cookie;
		}
		return null;
	}
	
	public static String getCookieValue(final ServletRequest req, final String name) {
		final HttpServletRequest httpReq = ServletUtils.wrap(req);
		return getCookieValue(httpReq, name);
	}
	
	public static String getCookieValue(final HttpServletRequest req, final String name) {
		final Cookie cookie = getCookie(req, name);
		return Objects.isNull(cookie) ? null : cookie.getValue();
	}
	
	public static Cookie setCookie(final ServletResponse res, final String name, final String value) {
		HttpServletResponse httpRes = ServletUtils.wrap(res);
		return setCookie(httpRes, name, value);
	}
	
	public static Cookie setCookie(final HttpServletResponse res, final String name, final String value) {
		final Cookie cookie = new Cookie(name, value);
		res.addCookie(cookie);
		return cookie;
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
}
