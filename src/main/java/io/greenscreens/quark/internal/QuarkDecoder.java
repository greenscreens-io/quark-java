/*
 * Copyright (C) 2015, 2024 Green Screens Ltd.
 */
package io.greenscreens.quark.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import io.greenscreens.quark.ext.ExtEncrypt;
import io.greenscreens.quark.ext.ExtJSDirectRequest;
import io.greenscreens.quark.security.IQuarkKey;
import io.greenscreens.quark.security.QuarkSecurity;
import io.greenscreens.quark.util.QuarkJson;
import io.greenscreens.quark.util.QuarkUtil;
import io.greenscreens.quark.websocket.data.WebSocketInstruction;
import io.greenscreens.quark.websocket.data.WebSocketRequest;

/**
 * Decode encrypted JsonBased WebRequest from "d-k-v" format.
 */
public enum QuarkDecoder {
	;

	private static final Logger LOG = LoggerFactory.getLogger(QuarkDecoder.class);

	public static void decode(final WebSocketRequest request, final IQuarkKey crypt) throws IOException {
	    if(WebSocketInstruction.ENC.equals(request.getCmd())) {
	        decodeDirect(request.getData(), crypt);
	    }
	}

	public static void decodeDirect(final List<ExtJSDirectRequest<JsonNode>> list, final IQuarkKey crypt) throws IOException {
		for (ExtJSDirectRequest<JsonNode> request : list) {
			decode(request, crypt);
		}
	}

	public static void decode(final ExtJSDirectRequest<JsonNode> request, final IQuarkKey crypt) throws IOException {
		final List<JsonNode> data = QuarkDecoder.decode(request.getData(), crypt);
		request.setData(data);
	}

	/**
	 * Decode encrypted request data
	 * 
	 * @param data
	 * @throws IOException
	 */
	public static List<JsonNode> decode(final List<JsonNode> data, final IQuarkKey crypt) throws IOException {
		final List<JsonNode> result = new ArrayList<JsonNode>();
		data.stream().map(node -> decode(node, crypt)).filter(node -> Objects.nonNull(node))
				.forEach(node -> populate(result, node));
		return result;
	}

	public static JsonNode decode(final String data, final IQuarkKey quarkKey) throws IOException {
        final JsonNode node = QuarkJson.parseQuark(data);
        return QuarkDecoder.decode(node, quarkKey);
	}
	
	public static JsonNode decode(final JsonNode data, final IQuarkKey crypt) {

		if (QuarkJson.isEmpty(data, "d") || QuarkJson.isEmpty(data, "k"))
			return data;

		try {
			final ExtEncrypt encrypt = QuarkJson.convert(ExtEncrypt.class, data);
			return decode(encrypt, crypt);
		} catch (IOException e) {
			final String msg = QuarkUtil.toMessage(e);
			LOG.error(msg);
		}

		return data;
	}

	public static JsonNode decode(final ExtEncrypt encrypt, final IQuarkKey crypt) throws IOException {

		if (encrypt.isValid()) {
			final String json = QuarkSecurity.decryptRequest(encrypt.getD(), encrypt.getK(), crypt);
			return QuarkJson.parseQuark(json);
		}
		return null;
	}
	
	static void populate(final List<JsonNode> result, JsonNode node) {
		if (node.isArray()) {

			final ArrayNode arr = (ArrayNode) node;
			final Iterator<JsonNode> it = arr.iterator();

			while (it.hasNext()) {
				result.add(it.next());
			}

		} else {
			result.add(node);
		}
	}
}
