/*
 * Copyright (C) 2015, 2022 Green Screens Ltd.
 */
package io.greenscreens.quark.ext;

public class ExtJSError extends RuntimeException {

	private static final long serialVersionUID = 1L;

	final String code;
	
	public ExtJSError(final String message, final String code) {
		super(message);
		this.code = code;
	}
	
	public static ExtJSError create(final String message, final String code) {
		return new ExtJSError(message, code); 
	}
}
