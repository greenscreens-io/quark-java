/*
 * Copyright (C) 2015, 2023 Green Screens Ltd.
 */
package io.greenscreens.quark.internal;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.greenscreens.quark.ext.ExtJSDirectRequest;
import io.greenscreens.quark.ext.ExtJSObjectResponse;
import io.greenscreens.quark.ext.ExtJSResponse;
import io.greenscreens.quark.reflection.IQuarkHandle;
import io.greenscreens.quark.utils.QuarkJson;
import io.greenscreens.quark.utils.QuarkUtil;
import io.greenscreens.quark.utils.ReflectionUtil;
import jakarta.enterprise.inject.spi.AnnotatedParameter;

/**
 * Internal reflection util that handle JSON t oControlelr mappings.  
 */
public enum QuarkHandlerUtil {
;
	
	private static final Logger LOG = LoggerFactory.getLogger(QuarkHandlerUtil.class);


	/**
	 * Map JSON arguments to Java Method parameters
	 * @param <T>
	 * @param request
	 * @param methodParams
	 * @return
	 * @throws IOException
	 */
	public static <T> Object[] fillParams(final ExtJSDirectRequest<T> request, final List<AnnotatedParameter<AnnotatedParameter<?>>> methodParams)	throws IOException {

		final int paramSize = methodParams.size();
		final int incomingParamsSize = request.getData() == null ? 0 : request.getData().size();

		final Object[] params = new Object[paramSize];

		for (int i = 0; i < paramSize; i++) {

			if (i < incomingParamsSize) {

				Object arg = null;
				final Object paramData = request.getData().get(i);

				if (ReflectionUtil.isJsonNode(paramData)) {

					final AnnotatedParameter<?> param = methodParams.get(i);
					final JsonNode jnode = (JsonNode) paramData;
					final Type type = param.getBaseType();

					Class<?> jType = null;
					if (ReflectionUtil.isParameterized(type)) {
						final ParameterizedType ptype = (ParameterizedType) type;
						final Type rtype = ptype.getRawType();
						
						if (ReflectionUtil.isCollection(rtype)) {
							arg = toCollection(ptype, jnode);
						} else {
							jType = (Class<?>) param.getBaseType();
							arg = QuarkJson.convert(jType, jnode);
						}

					} else {
						jType = (Class<?>) param.getBaseType();
						arg = QuarkJson.convert(jType, jnode);
					}

				} else {
					arg = paramData;
				}
				
				params[i] = arg;
			}
		}

		return params;
	}
	
	static Collection<Object> toCollection(final ParameterizedType ptype, final JsonNode node) {
		Collection<Object> collection = null;
		try {
			collection = QuarkJson.toCollection(ptype, node);
		} catch (Exception e) {
			final String msg = QuarkUtil.toMessage(e);
			LOG.error(msg);
			LOG.debug(msg, e);
		}
		return collection;
	}

	
	/**
	 * Convert enum error to Error response for requester
	 * @param error
	 * @return
	 */
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
	public static ExtJSResponse toResponse(final Object obj, final IQuarkHandle handle) {

		final Method method = handle.method();
		final Class<?> clazz = method.getReturnType();
		ExtJSResponse response = null;		

		if (handle.isVoid()) {
			response = new ExtJSResponse(true, null);
		} else if (ExtJSResponse.class.isAssignableFrom(clazz)) {
			response = (ExtJSResponse) obj;
		} else {
			final ExtJSObjectResponse<Object> objResponse = new ExtJSObjectResponse<>();
			objResponse.setSuccess(true);
			objResponse.setData(obj);
			response = objResponse;
		}
		return response;
	}
	
	/**
	 * Send public key and server timestamp
	 * 
	 * @param sts
	 * @param err
	 * @return
	 */
	public static ObjectNode getResponse() {
		return getResponse(true, null, null);
	}

	/**
	 * Create JSON error response in engine JSON format
	 * 
	 * @param error
	 * @return
	 */
	public static ObjectNode getResponse(final QuarkErrors error) {
		if (Objects.isNull(error)) return getResponse();
		return getResponse(false, error.getString(), error.getCode());
	}

	/**
	 * Create JSON response in engine JSON format
	 * 
	 * @param sts
	 * @param error
	 * @return
	 */
	public static ObjectNode getResponse(final boolean sts, final String error) {
		return getResponse(sts, error, QuarkErrors.E9999.getCode());
	}

	/**
	 * Create JSON response in engine JSON format
	 * 
	 * @param sts
	 * @param err
	 * @param code
	 * @return
	 */
	public static ObjectNode getResponse(final boolean sts, final String err, final String code) {

		final JsonNodeFactory factory = JsonNodeFactory.instance;
		final ObjectNode root = factory.objectNode();

		root.put("success", sts);
		root.put("ver", 0);
		root.put("ts", System.currentTimeMillis());

		if (!sts) {
			root.put("error", err);
			root.put("code", code);
		}

		return root;
	}
}
