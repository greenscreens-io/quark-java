/*
 * Copyright (C) 2015, 2024 Green Screens Ltd.
 */
package io.greenscreens.quark.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import jakarta.enterprise.inject.Vetoed;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;

/**
 * The MultipartMap. It simulates the
 * <code>HttpServletRequest#getParameterXXX()</code> methods to ease the
 * processing in <code>@MultipartConfig</code> servlets. You can access the
 * normal request parameters by <code>{@link #getParameter(String)}</code> and
 * you can access multiple request parameter values by
 * <code>{@link #getParameterValues(String)}</code>.
 */
@Vetoed
public class MultipartMap extends HashMap<String, Object> {

	// Constants
	// ----------------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;

	public static final String ATTRIBUTE_NAME = "parts";
	private static final String CONTENT_DISPOSITION = "content-disposition";
	private static final String CONTENT_DISPOSITION_FILENAME = "filename";
	private static final String DEFAULT_ENCODING = "UTF-8";
	private static final int DEFAULT_BUFFER_SIZE = 10240; // 10KB.

	// Vars
	// ---------------------------------------------------------------------------------------

	private String encoding;

	// Constructors
	// -------------------------------------------------------------------------------

	/**
	 * Global constructor.
	 */
	public MultipartMap(final HttpServletRequest multipartRequest)	throws ServletException, IOException {
		
		multipartRequest.setAttribute(ATTRIBUTE_NAME, this);

		this.encoding = multipartRequest.getCharacterEncoding();
		if (this.encoding == null) {
			this.encoding = DEFAULT_ENCODING;
			multipartRequest.setCharacterEncoding(DEFAULT_ENCODING);
		}

		for (Part part : multipartRequest.getParts()) {
			Optional<String> filename = getFilename(part);
			if (filename.isPresent()) {
				processFilePart(part, filename.get());
			} else {
				processTextPart(part);
			}
		}
	}


	// Actions
	// ------------------------------------------------------------------------------------

	@Override
	public Object get(final Object key) {
		Object value = super.get(key);
		if (value instanceof String[]) {
			final String[] values = (String[]) value;
			return values.length == 1 ? values[0] : Arrays.asList(values);
		} else {
			return value; // Can be Pair<Part, File> or null.
		}
	}

	/**
	 * @see ServletRequest#getParameter(String)
	 */
	@SuppressWarnings("unchecked")
	public String getParameter(final String name) {
		final Object value = super.get(name);
		if (Objects.isNull(value)) return null;
		if (value instanceof Pair) {
			final Pair<Part, File> pair = (Pair<Part, File>) value;
			return pair.getSecond().getName();
		}
		final String[] values = (String[]) value;
		return values.length == 1 ? values[0] : null;
	}

	/**
	 * @see ServletRequest#getParameterValues(String)
	 */
	@SuppressWarnings("unchecked")
	public String[] getParameterValues(final String name) {
		final Object value = super.get(name);
		if (Objects.isNull(value)) return new String[]{};
		if (value instanceof Pair) {
			final Pair<Part, File> pair = (Pair<Part, File>) value;
			return new String[] { pair.getSecond().getName() };
		}
		return (String[]) value;
	}

	/**
	 * @see ServletRequest#getParameterNames()
	 */
	public Enumeration<String> getParameterNames() {
		return Collections.enumeration(keySet());
	}

	/**
	 * @see ServletRequest#getParameterMap()
	 */
	@SuppressWarnings("unchecked")
	public Map<String, String[]> getParameterMap() {
		Map<String, String[]> map = new HashMap<>();
		for (Entry<String, Object> entry : entrySet()) {
			final Object value = entry.getValue();
			if (value instanceof String[]) {
				map.put(entry.getKey(), (String[]) value);
			} else {
				final Pair<Part, File> pair = (Pair<Part, File>) value;
				map.put(entry.getKey(), new String[] {pair.getSecond().getName() });
			}
		}
		return map;
	}

	public Map<String, String> getMap() {
		Map<String, String> map = new HashMap<>();
		for (Entry<String, Object> entry : entrySet()) {			
			map.put(entry.getKey(), getParameter(entry.getKey()));
		}
		return map;
	}
	
	// Helpers
	// ------------------------------------------------------------------------------------

	@SuppressWarnings("unchecked")
	public Pair<Part, File> getFile(final String name) {
		final Object value = super.get(name);
		return value instanceof Pair ? (Pair<Part, File>) value : null;
	}

	public InputStream getFileContent(final String name) throws IOException {
		final Pair<Part, File> pair = getFile(name);
		return Objects.isNull(pair) ? null : pair.getFirst().getInputStream();
	}
	
	public boolean isFile(final String name) {
		final Object value = super.get(name);
		return value instanceof Pair;
	}
	
	/**
	 * Returns the filename from the content-disposition header of the given part.
	 */
	private Optional<String> getFilename(final Part part) {
		String val = null;
		for (String cd : part.getHeader(CONTENT_DISPOSITION).split(";")) {
			if (cd.trim().startsWith(CONTENT_DISPOSITION_FILENAME)) {
				val = cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
				if (val.isEmpty()) val = null;
			}
		}
		return Optional.ofNullable(val);
	}

	/**
	 * Returns the text value of the given part.
	 */
	private String getValue(final Part part) throws IOException {
		
		final StringBuilder value = new StringBuilder();
		final InputStream is = part.getInputStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(is, encoding));
		
		try {
			char[] buffer = new char[DEFAULT_BUFFER_SIZE];
			for (int length = 0; (length = reader.read(buffer)) > 0;) {
				value.append(buffer, 0, length);
			}	
		} finally {
			QuarkUtil.close(reader);
			QuarkUtil.close(is);
		}
		
		
		return value.toString();
	}

	/**
	 * Process given part as Text part.
	 */
	private void processTextPart(Part part) throws IOException {
		
		final String name = part.getName();
		final String[] values = (String[]) super.get(name);

		if (Objects.isNull(values)) {
			// Not in parameter map yet, so add as new value.
			put(name, new String[] { getValue(part) });
		} else {
			// Multiple field values, so add new value to existing array.
			int length = values.length;
			String[] newValues = new String[length + 1];
			System.arraycopy(values, 0, newValues, 0, length);
			newValues[length] = getValue(part);
			put(name, newValues);
		}
	}

	private void processFilePart(final Part part, final String filename) {
		final String name = part.getName();
		final Pair<Part, File> pair = Pair.create(part, new File(name));
		put(name, pair);
	}
	
}