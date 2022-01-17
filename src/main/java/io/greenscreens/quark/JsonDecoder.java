/*
 * Copyright (C) 2015, 2016  Green Screens Ltd.
 */
package io.greenscreens.quark;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.enterprise.inject.Vetoed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.TypeFactory;

/**
 * Generic JSON decoder used internally
 */
@Vetoed
public final class JsonDecoder<T> {

	private T object;

	private MappingIterator<T> objectList;

	private static final ObjectMapper OBJECT_MAPPER;

	static {
		OBJECT_MAPPER = new ObjectMapper();
		
		try {
			final Class<?> clazz = JsonDecoder.class.getClassLoader().loadClass("com.fasterxml.jackson.module.afterburner.AfterburnerModule");
			if (clazz != null) {
				final Constructor<?> constructor = clazz.getDeclaredConstructor();
				OBJECT_MAPPER.registerModule((Module) constructor.newInstance());	
			}
		} catch (Exception e) {
			final Logger log = LoggerFactory.getLogger(JsonDecoder.class);
			final String msg = QuarkUtil.toMessage(e);
			log.warn(msg);
			log.debug(msg, e);
		}		

		OBJECT_MAPPER.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
				.disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
				.disable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
				.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

		OBJECT_MAPPER.enable(SerializationFeature.WRITE_ENUMS_USING_INDEX);

	}

	/**
	 * New Decoder instance for JSON data
	 * 
	 * @param type - class to which to convert
	 * @param json - json data to convert to Java class instance
	 * @throws IOException
	 */
	public JsonDecoder(final Class<T> type, final String json) throws IOException {
		super();
		parse(type, json);
	}

	/**
	 * Does actual conversion from JSON string to Java class instance
	 * 
	 * @param type
	 * @param json
	 * @throws IOException
	 */
	private void parse(final Class<T> type, final String json) throws IOException {

		final JsonFactory factory = new JsonFactory();
		final JsonParser jp = factory.createParser(json);

		try {
			final JsonNode jn = OBJECT_MAPPER.readTree(jp);

			if (jn.isArray()) {
				final TypeFactory tf = TypeFactory.defaultInstance();
				final JavaType jt = tf.constructCollectionType(ArrayList.class, type);
				objectList = OBJECT_MAPPER.readValues(jp, jt);
			} else {
				object = OBJECT_MAPPER.treeToValue(jn, type);
			}
		} finally {
			jp.close();
		}

	}

	public static <T> T parse(final Class<T> type, final JsonNode node) {

		if (node == null)
			return null;

		if (node.isArray()) {
			final TypeFactory tf = TypeFactory.defaultInstance();
			final JavaType jt = tf.constructCollectionType(ArrayList.class, type);
			return OBJECT_MAPPER.convertValue(node, jt);
		} else {
			return OBJECT_MAPPER.convertValue(node, type);
		}
	}

	public static <T> T convert(final Class<T> type, final JsonNode object) throws JsonProcessingException {
		return object == null ? null : OBJECT_MAPPER.treeToValue(object, type);
	}

	public static <T> T convert(final JavaType type, final JsonNode object) throws IOException {
		if (object == null)	return null;
		return OBJECT_MAPPER.readerFor(type).readValue(object);
	}

	public static <T> ArrayNode convert(T [] args) {
		if (args == null)	return null;
		return OBJECT_MAPPER.valueToTree(args);
	}

    public static <T> List<T> convert(final Class<T> type, final ArrayNode node) throws IOException {
        final TypeFactory tf = TypeFactory.defaultInstance();
        final JavaType jt = tf.constructCollectionType(ArrayList.class, type);
        final ObjectReader reader = OBJECT_MAPPER.readerFor(jt);
        return reader.readValue(node);
    }

	/**
	 * Checks is converted JSON array or single object
	 * 
	 * @return true if it is not array
	 */
	public final boolean isSingle() {
		return object != null;
	}

	/**
	 * Returns JSON data converted Java class instance. If JSON data is array, this
	 * method will return null
	 * 
	 * @return class instance from defined class in constructor
	 */
	public final T getObject() {
		return object;
	}

	/**
	 * Returns JSON data converted Java class instance. If JSON data is object, this
	 * method will return null
	 * 
	 * @return class instance from defined class in constructor
	 */
	public final List<T> getObjectList() throws IOException {

		List<T> list = null;
		if (objectList != null) {
			list = objectList.readAll();
		} else if (object != null) {
			list = Arrays.asList(object);
		}

		return list;
	}

	/**
	 * Retrieves internal JSON parser engine
	 * 
	 * @return
	 */
	public static ObjectMapper getJSONEngine() {
		return OBJECT_MAPPER;
	}

	/**
	 * Parse json string to Json Object
	 * 
	 * @param data
	 * @return
	 * @throws JsonProcessingException
	 * @throws JsonMappingException
	 * @throws Exception
	 */
	public static JsonNode parse(final String data) throws JsonProcessingException {
		return data == null ? null : OBJECT_MAPPER.readTree(data);
	}

	@SuppressWarnings("unchecked")
	public static <K extends JsonNode> K parseType(final String data) throws JsonProcessingException {
		return data == null ? null : (K) OBJECT_MAPPER.readTree(data);
	}

	/**
	 * Convert object to json string
	 * 
	 * @param object
	 * @return
	 * @throws JsonProcessingException
	 * @throws Exception
	 */
	public static String stringify(final Object object) throws JsonProcessingException {
		return object == null ? null : OBJECT_MAPPER.writeValueAsString(object);
	}

	/**
	 * Check if node contains key
	 * 
	 * @param node
	 * @param key
	 * @return
	 */
	public static boolean hasKey(final JsonNode node, final String key) {

		if (node != null) {
			return node.has(key);
		}

		return false;
	}

	public static boolean isEmpty(final JsonNode node, final String key) {
		if (!hasKey(node, key)) return true;
		
		final JsonNode valueNode = node.get(key);
		
		switch (valueNode.getNodeType()) {
		case ARRAY:
			return ((ArrayNode)valueNode).size() == 0;
		case STRING:
			final String value = getStr(node, key);		
			return StringUtil.isEmpty(value);			
		case NULL:			
			return true;			
		default:
			break;
		} 
		
		return false;
	}

	public static boolean isArray(final JsonNode node, final String key) {

		if (hasKey(node, key)) {
			return node.get(key).isArray();
		}

		return false;
	}
	
	public static ArrayNode getArray(final JsonNode node, final String key) {

		if (isArray(node, key)) {
			return (ArrayNode) node.get(key);
		}

		return null;
	}
	
	/**
	 * Get property as Integer
	 * 
	 * @param node
	 * @param key
	 * @return
	 */
	public static int getInt(final JsonNode node, final String key) {

		int val = 0;

		if (hasKey(node, key)) {
			val = node.get(key).asInt(0);
		}

		return val;
	}

	/**
	 * Get property as Long
	 * 
	 * @param node
	 * @param key
	 * @return
	 */
	public static long getLong(final JsonNode node, final String key) {

		long val = 0;

		if (hasKey(node, key)) {
			val = node.get(key).asLong(0);
		}

		return val;
	}

	/**
	 * Get property as String
	 * 
	 * @param node
	 * @param key
	 * @return
	 */
	public static String getStr(final JsonNode node, final String key) {

		String val = null;

		if (hasKey(node, key)) {
			val = node.get(key).asText();
		} else {
			val = "";
		}

		return val;
	}

	/**
	 * Compare to object properties as int
	 * 
	 * @param node
	 * @param filter
	 * @param name
	 * @return
	 */
	public static boolean compareInt(final JsonNode node, final ObjectNode filter, final String name) {
		return getInt(node, name) == getInt(filter, name);
	}

	/**
	 * Compare to object properties as String
	 * 
	 * @param node
	 * @param filter
	 * @param name
	 * @return
	 */
	public static boolean compareStr(final JsonNode node, final ObjectNode filter, final String name) {
		return getStr(node, name).equals(getStr(filter, name));
	}

	/**
	 * Copy properties from node to node
	 * 
	 * @param from
	 * @param to
	 */
	public static void copy(final ObjectNode from, final ObjectNode to) {
		if (to != null && from != null) {
			to.setAll(from);
		}
	}

}
