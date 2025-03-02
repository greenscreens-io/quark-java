/*
 * Copyright (C) 2015, 2023 Green Screens Ltd.
 */
package io.greenscreens.quark.util.override;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;

import jakarta.enterprise.inject.Vetoed;

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
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.TypeFactory;

import io.greenscreens.quark.util.QuarkUtil;

/**
 * Generic JSON decoder used internally
 */
@Vetoed
public final class JsonDecoder<T> {

	private T object;

	private MappingIterator<T> objectList;

	private static final ObjectMapper OBJECT_MAPPER;

	/**
	 * Initialize object mapper
	 */
	static {
		OBJECT_MAPPER = new ObjectMapper();
		
		// Use optimizer if available
		try {
			final Class<?> clazz = JsonDecoder.class.getClassLoader().loadClass("com.fasterxml.jackson.module.afterburner.AfterburnerModule");
			if (Objects.nonNull(clazz)) {
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
	
    public static ObjectWriter writerWith(final Class<?> view) {
        return OBJECT_MAPPER.writerWithView(view);
    }
    
    public static ObjectReader readerWith(final Class<?> view) {
        return OBJECT_MAPPER.readerWithView(view);
    }

    public static ObjectMapper mapper() {
        return OBJECT_MAPPER;
    }
    
    public static ObjectNode createObjectNode() {
        return OBJECT_MAPPER.createObjectNode();
    }
    
    public static ArrayNode createArrayNode() {
        return OBJECT_MAPPER.createArrayNode();
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
		doParse(type, json);
	}

	/**
	 * Does actual conversion from JSON string to Java class instance
	 * 
	 * @param type
	 * @param json
	 * @throws IOException
	 */
	private void doParse(final Class<T> type, final String json) throws IOException {

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

	public static <T> T parse(final Class<T> type, final String json) throws JsonProcessingException {
		return parse(type, parse(json));
	}
	
	/**
	 * Parse JsonNode into defined class instance
	 * @param <T>
	 * @param type
	 * @param node
	 * @return
	 */
	public static <T> T parse(final Class<T> type, final JsonNode node) {

		if (Objects.isNull(node)) return null;

		if (node.isArray()) {
			final TypeFactory tf = TypeFactory.defaultInstance();
			final JavaType jt = tf.constructCollectionType(ArrayList.class, type);
			return OBJECT_MAPPER.convertValue(node, jt);
		} else {
			return OBJECT_MAPPER.convertValue(node, type);
		}
	}

	/**
	 * Convert node object to given java class
	 * @param <T>
	 * @param type
	 * @param object
	 * @return
	 * @throws JsonProcessingException
	 */
	public static <T> T convert(final Class<T> type, final JsonNode object) throws JsonProcessingException {
		return Objects.isNull(object) ? null : OBJECT_MAPPER.treeToValue(object, type);
	}

	public static <T> T convert(final Class<T> type, final String json) throws JsonProcessingException {
		final JsonNode node = parse(json);
		return convert(type, node);
	}
	
	/**
	 * Convert node object to given java type
	 * @param <T>
	 * @param type
	 * @param object
	 * @return
	 * @throws IOException
	 */
	public static <T> T convert(final JavaType type, final JsonNode object) throws IOException {
		return Objects.isNull(object) ? null : OBJECT_MAPPER.readerFor(type).readValue(object);
	}

	/**
	 * Convert array of objects to array of json nodes
	 * @param <T>
	 * @param args
	 * @return
	 */
	public static <T> ArrayNode convert(T [] args) {
		return Objects.isNull(args) ? null : OBJECT_MAPPER.valueToTree(args);
	}

	/**
	 * Convert array of nodes to list of types
	 * @param <T>
	 * @param type
	 * @param node
	 * @return
	 * @throws IOException
	 */
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
		return Objects.isNull(object) ? null : object instanceof Enumeration<?>;
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
		if (Objects.nonNull(objectList)) {
			list = objectList.readAll();
		} else if (Objects.nonNull(object)) {
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
		return Objects.isNull(data) ? null : OBJECT_MAPPER.readTree(data);
	}

	@SuppressWarnings("unchecked")
	public static <K extends JsonNode> K parseType(final String data) throws JsonProcessingException {
		return Objects.isNull(data) ? null : (K) OBJECT_MAPPER.readTree(data);
	}

    public static JsonNode parseAs(final String data, final Class<?> view) throws JsonProcessingException {
        return data == null ? null : readerWith(view).readTree(data);
    }

    @SuppressWarnings("unchecked")
    public static <K extends JsonNode> K parseTypeAs(final String data, final Class<?> view) throws JsonProcessingException {
        return data == null ? null : (K) readerWith(view).readTree(data);
    }

    public static <T> T parseAs(final String data, final Class<T> clazz, final Class<?> view) throws JsonProcessingException {
        return convert(clazz, parseAs(data, view));
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
		return Objects.isNull(object) ? null : OBJECT_MAPPER.writeValueAsString(object);
	}

    /**
     * Stringify Object with specific view
     * @param object
     * @param view
     * @return
     * @throws JsonProcessingException
     */
    public static String stringifyAs(final Object object, final Class<?> view) throws JsonProcessingException {
        return Objects.isNull(object) ? null : writerWith(view).writeValueAsString(object);
    }
    
	/**
	 * Check if node contains key
	 * 
	 * @param node
	 * @param key
	 * @return
	 */
	public static boolean hasKey(final JsonNode node, final String key) {
		return Objects.isNull(node) ? false : node.has(key);	
	}

	/**
	 * Check if provided node is empty. Array size is 0 or Object does not contains properties.
	 * @param node
	 * @param key
	 * @return
	 */
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
	
	public static boolean isNonEmpty(final JsonNode node, final String key) {
	    return !isEmpty(node, key);
	}

	/**
	 * Check if node property is of type array
	 * @param node
	 * @param key
	 * @return
	 */
	public static boolean isArray(final JsonNode node, final String key) {
		return hasKey(node, key) ? node.get(key).isArray() : false;	
	}
	
	/**
	 * Return node property as array
	 * @param node
	 * @param key
	 * @return
	 */
	public static ArrayNode getArray(final JsonNode node, final String key) {
		return isArray(node, key) ? (ArrayNode) node.get(key) : null;
	}
	
	/**
	 * Get property as Integer
	 * 
	 * @param node
	 * @param key
	 * @return
	 */
	public static int getInt(final JsonNode node, final String key) {
		return hasKey(node, key) ? node.get(key).asInt(0) : 0;
	}

	/**
	 * Get property as Long
	 * 
	 * @param node
	 * @param key
	 * @return
	 */
	public static long getLong(final JsonNode node, final String key) {
		return hasKey(node, key)? node.get(key).asLong(0) : 0l;
	}

	/**
	 * Get property as String
	 * 
	 * @param node
	 * @param key
	 * @return
	 */
	public static String getStr(final JsonNode node, final String key) {
		return hasKey(node, key) ? node.get(key).asText() : "" ;
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
		if (Objects.nonNull(to) && Objects.nonNull(to)) {
			to.setAll(from);
		}
	}

}
