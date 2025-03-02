/*
 * Copyright (C) 2015, 2023. Green Screens Ltd.
 */
package io.greenscreens.quark.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.greenscreens.quark.util.override.JsonDecoder;
import io.greenscreens.quark.util.override.JsonViews;

public enum QuarkJson {
;
	
    public static ObjectNode node() {
        return JsonDecoder.createObjectNode();
    }
    
	public static <T> ArrayNode convert(T [] args) {
		return JsonDecoder.convert(args);
	}
	
	public static <T> T convert(final Class<T> type, final String json) throws JsonProcessingException {
		return JsonDecoder.convert(type, json);
	}
	
	public static <T> T convert(final Class<T> type, final JsonNode object) throws JsonProcessingException {
		return JsonDecoder.convert(type, object);
	}
	
	public static String stringify(final Object object) throws JsonProcessingException {
		return JsonDecoder.stringify(object);
	}
	
	public static JsonNode parse(final String data) throws JsonProcessingException {
		return JsonDecoder.parse(data);
	}
	
	public static <T> T parse(final Class<T> type, final String json) throws JsonProcessingException {
		return JsonDecoder.parse(type, json);
	}
	
    public static JsonNode parseQuark(final String data) throws JsonProcessingException {
        return JsonDecoder.parseAs(data, JsonViews.Quark.class);
    }
    
	public static Collection<Object> toCollection(final ParameterizedType ptype, final JsonNode node) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, JsonProcessingException {

		final Type rtype = ptype.getRawType();
		Collection<Object> collection = null;
		Class<?> gen = (Class<?>) rtype;

		collection = ReflectionUtil.createListOfType(gen);
		
		if (node.isArray()) {
			ArrayNode anodes = (ArrayNode) node;
			
			for (JsonNode anode : anodes) {
				collection.add(convert(gen, anode));
			}
		} else {
			// ;
			collection.add(convert(gen, node));
		}

		return collection;

	}

    public static boolean hasKey(final JsonNode node, final String key) {
        return JsonDecoder.hasKey(node, key);
    }
    
    public static boolean isEmpty(final JsonNode node, final String key) {
        return JsonDecoder.isEmpty(node, key);
    }
    
    public static boolean isNonEmpty(final JsonNode node, final String key) {
        return JsonDecoder.isNonEmpty(node, key);
    }   	
}
