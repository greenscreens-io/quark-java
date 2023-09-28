/*
 * Copyright (C) 2015, 2023 Green Screens Ltd.
 */
package io.greenscreens.quark.web.data;

import javax.enterprise.inject.Vetoed;

import com.fasterxml.jackson.databind.JsonNode;

import io.greenscreens.quark.ext.ExtJSDirectRequest;
import io.greenscreens.quark.ext.ExtJSDirectResponse;

/**
 * Object to be converted into JSON structure. {type :'ws' , sid : session_id ,
 * tid : transaction_id, timeout : 0 , ....}
 */
@Vetoed
public class WebResponse extends ExtJSDirectResponse<JsonNode> {

	public WebResponse() {
		super(null, null);
	}

	public WebResponse(ExtJSDirectRequest<JsonNode> request, Object response) {
		super(request, response);
	}

}
