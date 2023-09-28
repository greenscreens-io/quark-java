/*
 * Copyright (C) 2015, 2023 Green Screens Ltd.
 */
package io.greenscreens.quark;

import java.util.stream.Stream;


/**
 * Application used MIME types
 */
public enum MIME {

	OCTET("application/octet-stream", "bin"), 
	JSON("application/json; charset=UTF-8", "json"),
	TEXT("text/plain; charset=UTF-8", "txt")
	; 
	
	private final String mime;
	private final String ext;

	private MIME(final String mime, final String ext) {
		this.mime = mime;
		this.ext = ext;
	}
	
	public String value() {
		return mime;
	}
	
	public String extension() {
		return ext;
	}
	
	public static MIME toMime(final String value) {
		final String tmp = StringUtil.normalize(value).toLowerCase();
		return Stream.of(MIME.values())				
				.filter(m -> m.mime.toLowerCase().startsWith(tmp))
				.findFirst()
				.orElse(OCTET);
	}
	
}
