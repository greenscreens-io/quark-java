/*
 * Copyright (C) 2015, 2023 Green Screens Ltd.
 */
package io.greenscreens.quark.internal;

/**
 * Error list with codes and descriptions
 */
public enum QuarkErrors {

	E0000("E0000", "Invalid encryption data"), 
	E0001("E0001", "Requested Service not found"),
	E0002("E0002", "Incomming parameters are invalid"),
	
	E7777("E7777", "Request timeout!"),
	E9999("E9999", "General error");

	private String code;

	private String message;

	private QuarkErrors(String code, String message) {
		this.code = code;
		this.message = message;
	}

	public String getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}

	public String getString() {
		return String.format("%s : %s", code, message);
	}

}
