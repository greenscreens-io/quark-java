/*
 * Copyright (C) 2015, 2023 Green Screens Ltd.
 */
package io.greenscreens.quark.web.data;

import jakarta.enterprise.inject.Vetoed;

import com.fasterxml.jackson.databind.JsonNode;

import io.greenscreens.quark.ext.ExtJSDirectRequest;

/**
 * Class used to map JSON structure describing ExtJS websocket request.
 */
@Vetoed
public class WebRequest extends ExtJSDirectRequest<JsonNode> {

	public WebRequest() {
		super();
	}

}
