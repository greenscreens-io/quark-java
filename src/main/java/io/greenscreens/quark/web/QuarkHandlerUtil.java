/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 * 
 * https://www.greenscreens.io
 */
package io.greenscreens.quark.web;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;


import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.servlet.http.HttpSession;
import javax.validation.ConstraintViolation;
import javax.validation.ElementKind;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import javax.validation.Path.Node;
import javax.validation.Path.ParameterNode;
import javax.validation.executable.ExecutableValidator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.greenscreens.quark.JsonDecoder;
import io.greenscreens.quark.QuarkEngine;
import io.greenscreens.quark.QuarkSecurity;
import io.greenscreens.quark.QuarkUtil;
import io.greenscreens.quark.cdi.Required;
import io.greenscreens.quark.ext.ExtJSDirectRequest;
import io.greenscreens.quark.ext.ExtJSObjectResponse;
import io.greenscreens.quark.ext.ExtJSResponse;
import io.greenscreens.quark.ext.annotations.ExtJSActionLiteral;
import io.greenscreens.quark.ext.annotations.ExtJSMethod;
import io.greenscreens.quark.ext.annotations.ExtName;
import io.greenscreens.quark.security.IAesKey;
import io.greenscreens.quark.websocket.data.WebSocketInstruction;


public enum QuarkHandlerUtil {
;
	
	private static final Logger LOG = LoggerFactory.getLogger(QuarkHandlerUtil.class);

	private static ValidatorFactory factory = null;
	
	public static Bean<?> findBean(final ExtJSDirectRequest<?> request) {
		final ExtJSActionLiteral literal = new ExtJSActionLiteral(request.getNamespace(), request.getAction());
		final Iterator<Bean<?>> it = QuarkEngine.getBeanManager().getBeans(Object.class, literal).iterator();
		return it.next();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <T> AnnotatedMethod<AnnotatedParameter<?>> findMethod(final ExtJSDirectRequest<T> request, final AnnotatedType annType) {

		AnnotatedMethod<AnnotatedParameter<?>> selectedMethod = null;
		final Set<AnnotatedMethod<AnnotatedParameter<?>>> aMethods = annType.getMethods();

		selectedMethod = aMethods.stream()
				.filter(aMethod -> aMethod.isAnnotationPresent(ExtJSMethod.class))
				.filter(aMethod -> {
					final ExtJSMethod annMethod = aMethod.getAnnotation(ExtJSMethod.class);
					return annMethod.value().equals(request.getMethod());
				})
				.filter(aMethod -> aMethod.getParameters().size() == request.getData().size())
				.findFirst()
				.orElse(null);

		return selectedMethod;
	}

	public static <T> Object[] fillParams(final ExtJSDirectRequest<T> request, final List<AnnotatedParameter<?>> methodParams)	throws IOException {

		final int paramSize = methodParams.size();
		final int incomingParamsSize = request.getData() == null ? 0 : request.getData().size();

		final ObjectMapper mapper = JsonDecoder.getJSONEngine();

		final Object[] params = new Object[paramSize];

		for (int i = 0; i < paramSize; i++) {

			if (i < incomingParamsSize) {

				final Object paramData = request.getData().get(i);

				if (isJsonNode(paramData)) {

					final JsonNode jnode = (JsonNode) paramData;

					Class<?> jType = null;

					AnnotatedParameter<?> param = methodParams.get(i);
					Type type = param.getBaseType();

					if (isParameterized(type)) {
						ParameterizedType ptype = (ParameterizedType) type;
						Type rtype = ptype.getRawType();
						
						if (isCollection(rtype)) {
							params[i] = toCollection(ptype, jnode);
						} else {
							jType = (Class<?>) param.getBaseType();
							params[i] = mapper.treeToValue(jnode, jType);
						}

					} else {
						jType = (Class<?>) param.getBaseType();
						params[i] = mapper.treeToValue(jnode, jType);
					}

				} else {
					params[i] = paramData;
				}
			}
		}

		return params;
	}

	public static boolean isJsonNode(final Object o) {
		return o instanceof JsonNode;
	}
	
	public static boolean isParameterized(final Type type) {
		return type instanceof ParameterizedType;
	}
	
	public static boolean isCollection(final Type type) {		
		return Collection.class.isAssignableFrom((Class<?>) type);
	}
	
	public static Collection<Object> toCollection(final ParameterizedType ptype, final JsonNode node) {

		final ObjectMapper mapper = JsonDecoder.getJSONEngine();
		final Type rtype = ptype.getRawType();
		Collection<Object> collection = null;
		Class<?> gen = null;

		try {

			collection = createListOfType((Class<?>) rtype);

			if (node.isArray()) {
				ArrayNode anodes = (ArrayNode) node;

				for (JsonNode anode : anodes) {
					collection.add(mapper.treeToValue(anode, gen));
				}
			} else {
				collection.add(mapper.treeToValue(node, gen));
			}

		} catch (Exception e) {
			final String msg = QuarkUtil.toMessage(e);
			LOG.error(msg);
			LOG.debug(msg, e);
		}

		return collection;

	}

	@SuppressWarnings("unchecked")
	public static <T> Collection<T> createListOfType(final Class<?> collection)
			throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {

		if (collection.isInterface()) {
			return new ArrayList<>();
		}

		if (Modifier.isAbstract(collection.getModifiers())) {
			return new ArrayList<>();
		}

		return (Collection<T>) collection.getDeclaredConstructor().newInstance();
	}

	/**
	 * Check for @Required annotation,in such case, parameter can't be null
	 * 
	 * @param paramList
	 * @param params
	 * @return
	 */
	public static boolean isParametersInvalid(final List<AnnotatedParameter<?>> paramList, final Object[] params) {

		boolean sts = false;
		Required req = null;
		int i = 0;

		for (AnnotatedParameter<?> param : paramList) {
			req = param.getAnnotation(Required.class);
			if (Objects.nonNull(req) && Objects.isNull(params[i])) {
				sts = true;
				break;
			}
			i++;
		}

		return sts;
	}
	

	/**
	 * Check if method is asynchronous
	 * @param method
	 * @return
	 */
	public static boolean isAsync(final Method method) {
		if (Objects.isNull(method)) return false; 
		final ExtJSMethod annMethod = method.getAnnotation(ExtJSMethod.class);
		if (Objects.isNull(annMethod)) return false;
		return annMethod.async();
	}
	
	/**
	 * Update access modifier for method
	 * @param method
	 * @return
	 */
	public static Method toMethod(final AnnotatedMethod<?> method) {
		
		if (Objects.isNull(method)) return null;
		
		final Method javaMethod = method.getJavaMember();

		if (javaMethod.isAccessible()) {
			// if (javaMethod.canAccess(beanInstance)) {
			javaMethod.setAccessible(true);
		}
		
		return javaMethod;
	}
	
	
	public static ExtJSResponse getError(final QuarkErrors error) {
		final ExtJSResponse response = new ExtJSResponse(false, error.getString());
		response.setCode(error.getCode());
		return response;
	}
	
	/**
	 * Convert CDI method response to Quark response
	 * @param obj
	 * @param clazz
	 * @return
	 */
	public static ExtJSResponse toResponse(final Object obj, final Method method) {

		final Class<?> clazz = method.getReturnType();
		ExtJSResponse response = null;
		if (isVoid(method)) {
			response = new ExtJSResponse(true, null);
		} else if (ExtJSResponse.class.isAssignableFrom(clazz)) {
			response = (ExtJSResponse) obj;
		} else {
			ExtJSObjectResponse<Object> objResponse = new ExtJSObjectResponse<>();
			objResponse.setData(obj);
			response = objResponse;
		}
		return response;
	}

	/**
	 * True is method does not have return type
	 * @param method
	 * @return
	 */
	public static boolean isVoid(final Method method) {
		final Class<?> clz = method.getReturnType();
		return (clz == void.class || clz == Void.class);		
	}
	
	/**
	 * True if method has return type
	 * @param method
	 * @return
	 */
	public static boolean hasRetVal(final Method method) {
		return !isVoid(method);
	}
	
	/**
	 * Print exception trace in a safe manner
	 * @param e
	 */
	public static void printError(final Exception e) {
		final String error = QuarkUtil.normalize(e.getMessage());
		final String msg = error.replace("\n", "; ");
		final String[] items = error.split("\n");
		for (String item : items) {
			LOG.error(item);
		}
		LOG.debug(msg, e);
	}
	
	/**
	 * Helper method to validate calling method arguments annotated with JSR-380
	 * 
	 * @param instance
	 * @param method
	 * @param params
	 * @throws Exception
	 */
	public static void validateParameters(final Object instance, final Method method, final Object[] params) throws IOException {

		final ExtJSMethod annMethod = method.getAnnotation(ExtJSMethod.class);
		if (!annMethod.validate()) {
			return;
		}

		if (Objects.isNull(factory)) {
			LOG.warn("Validation factory not initialized! Unable to validate Quark Engine call parameters.");
			return;
		}
		
		final ExecutableValidator validator = factory.getValidator().forExecutables();
		final Set<ConstraintViolation<Object>> violations = validator.validateParameters(instance, method, params);

		if (!violations.isEmpty()) {
			final String message = describeValidations(method, violations);
			throw new IOException(message);
		}

	}
	
	/**
	 * Describe validation errors
	 * @param method
	 * @param violations
	 * @return
	 */
	public static String describeValidations(final Method method, final Set<ConstraintViolation<Object>> violations) {
		
		final StringBuilder builder = new StringBuilder();
		
		for (ConstraintViolation<Object> violation : violations) {

			final Iterator<Node> it = violation.getPropertyPath().iterator();

			while (it.hasNext()) {
				final Node node = it.next();
				describeNode(method, builder, node);
			}

			builder.append(violation.getMessage());
			builder.append("\n");
		}
		
		return builder.toString().trim();
	}
	
	/**
	 * Describe validation error node
	 * @param method
	 * @param builder
	 * @param node
	 */
	public static void describeNode(final Method method, final StringBuilder builder, final Node node) {
		if (node.getKind() == ElementKind.PARAMETER) {						
			final ParameterNode pNode = (ParameterNode) node;
			final int index = pNode.getParameterIndex();
			final Parameter par = method.getParameters()[(int) index];
			final ExtName name = par.getAnnotation(ExtName.class);
			
			if (Objects.nonNull(name)) {
				builder.append(par.getAnnotation(ExtName.class).value());
				builder.append(" - ");
			}
		}

	}

	public static boolean checkPath(final String uri, final String[] paths) {

		boolean result = false;

		for (String path : paths) {

			if ("*".equals(path)) {
				result = true;
				break;
			}

			final int idx = uri.indexOf(path);
			if (idx == 0 || idx == 1) {
				result = true;
				break;
			}
		}

		return result;
	}

	public static boolean isValidHttpSession(final HttpSession session) {
		final String attr = ServletUtils.get(session, QuarkConstants.HTTP_SEESION_STATUS);
		return Boolean.TRUE.toString().equalsIgnoreCase(attr);
	}
	
	/**
	 * Encrypt response JSON into encrypted JSON format
	 * 
	 * @param request
	 * @param data
	 * @return
	 * @throws Exception
	 */
	public static final ObjectNode encrypt(final IAesKey key, final String data) throws IOException {

		if (Objects.isNull(key))
			return JsonNodeFactory.instance.objectNode();

		final byte[] iv = QuarkSecurity.getRandom(key.getBlockSize());
		final String enc = key.encrypt(data, iv);
		final ObjectNode node = JsonNodeFactory.instance.objectNode();
		node.put("iv", QuarkUtil.bytesToHex(iv));
		node.put("d", enc);
		node.put("cmd", WebSocketInstruction.ENC.toString());
		return node;
	}

	public static void releaseValidator() {
		if (Objects.nonNull(factory)) {
			factory.close();
			factory = null;
		}
	}
	
	public static void initValidator() {
		if (Objects.nonNull(factory)) return;
		try {
			factory = Validation.buildDefaultValidatorFactory();
		} catch (Exception e) {
			final String msg = QuarkUtil.toMessage(e);
			LOG.warn(msg);
			LOG.debug(msg, e);
		}
	}
}
