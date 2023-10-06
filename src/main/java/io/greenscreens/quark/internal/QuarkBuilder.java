/*
 * Copyright (C) 2015, 2023 Green Screens Ltd.
 */
package io.greenscreens.quark.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.greenscreens.quark.reflection.IQuarkBean;
import io.greenscreens.quark.reflection.internal.QuarkMapper;
import io.greenscreens.quark.security.QuarkSecurity;
import io.greenscreens.quark.utils.QuarkJson;
import jakarta.enterprise.inject.spi.Bean;

/**
 * This is a cache of prepared reflected methods 
 */
public enum QuarkBuilder {
;

	public static final String KEY_ENC = "keyEnc";
	public static final String KEY_VER = "keyVer";		
	public static final String SIGNATURE = "signature";
	
	/**
	 * Create API response object
	 * 
	 * @param api
	 * @param challenge
	 */
	public static ObjectNode buildAPI(final ArrayNode api, final String challenge) {

		final ObjectNode root = JsonNodeFactory.instance.objectNode();
		root.set("api", api);

		final String keyEnc = QuarkSecurity.getPublic();
		final String keyVer = QuarkSecurity.getVerifier();
		final String signature = QuarkSecurity.signApiKey(challenge);

		root.put(KEY_ENC, keyEnc);
		root.put(KEY_VER, keyVer);		
		root.put(SIGNATURE, signature);
		
		return root;

	}

	/**
	 * Build meta structure for web
	 * 
	 * @return
	 */
	static public ArrayNode build(final String [] pathsFilter) {

		final ArrayNode root = JsonNodeFactory.instance.arrayNode();
		final Collection<IQuarkBean> handles = QuarkMapper.filter(pathsFilter);
		
		handles.forEach(h -> {
			
			final ObjectNode objectNode = JsonNodeFactory.instance.objectNode();
			final ArrayNode methodsNode = objectNode.putArray("methods");
			final JsonNode paths = QuarkJson.convert(h.extDirect().paths());
			
			objectNode.put("namespace", h.extAction().namespace());
			objectNode.put("action", h.extAction().action());
			objectNode.set("paths", paths);
			buildMethod(methodsNode, h.bean());		
			root.add(objectNode);
			
		});

		return root;

	}
	
	/**
	 * List of registered service paths
	 * @return
	 */
	static public List<String> services() {

		final List<String> list = new ArrayList<>();
		
		QuarkMapper.beans().forEach(h -> {
			for (String path : h.paths()) {
				if (!list.contains(path)) {
					list.add(path);
				}
			}			
		});

		return list;

	}
	
	/**
	 * Build exposed method list
	 * @param methodsNode
	 * @param clazz
	 */
	static void buildMethod(final ArrayNode methodsNode, final Bean<?> bean) {
		
		final Optional<IQuarkBean> beanHandle = QuarkMapper.find(bean);
		if (beanHandle.isEmpty()) return;
		
		beanHandle.get().handles().forEach(bh -> {
			final ObjectNode objNode = JsonNodeFactory.instance.objectNode();
			objNode.put("mid", bh.id());
			objNode.put("name", bh.name());
			objNode.put("len", bh.method().getParameterCount());
			if (bh.isAsync()) objNode.put("async", true);	
			methodsNode.add(objNode);
		});
		
	}
}
