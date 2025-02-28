/*
 * Copyright (C) 2015, 2023. Green Screens Ltd.
 */
package io.greenscreens.quark.internal;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

import io.greenscreens.quark.ext.ExtJSDirectRequest;
import io.greenscreens.quark.ext.ExtJSObjectResponse;
import io.greenscreens.quark.ext.ExtJSResponse;
import io.greenscreens.quark.reflection.IQuarkHandle;
import io.greenscreens.quark.util.QuarkJson;
import io.greenscreens.quark.util.QuarkUtil;
import io.greenscreens.quark.util.ReflectionUtil;
import io.greenscreens.quark.web.QuarkCookieUtil;
import io.greenscreens.quark.web.ServletUtils;
import jakarta.enterprise.inject.spi.AnnotatedParameter;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;

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

                    final JsonNode jnode = (JsonNode) paramData;

                    final AnnotatedParameter<?> param = methodParams.get(i);
                    final Type type = param.getBaseType();
                    final  Class<?> jType = (Class<?>) type;

					if (ReflectionUtil.isParameterized(type)) {
						final ParameterizedType ptype = (ParameterizedType) type;
						final Type rtype = ptype.getRawType();
						
						if (ReflectionUtil.isCollection(rtype)) {
							arg = toCollection(ptype, jnode);
						} else {
							arg = QuarkJson.convert(jType, jnode);
						}

					} else {
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
     * Convert enum error to Error response for requester
     * @param error
     * @return
     */
    public static ExtJSResponse getError(final QuarkErrors error) {
        final ExtJSResponse response = new ExtJSResponse(false, error.getString());
        response.setCode(error.getCode());
        return response;
    }
    
    public static String getPublicKey(final HttpServletRequest request) {
        String publicKey = request.getHeader(QuarkConstants.WEB_KEY);       
        if (QuarkUtil.isEmpty(publicKey)) {
            publicKey = QuarkCookieUtil.getCookieValue(request, QuarkConstants.WEB_KEY);
        }   
        return publicKey;
    }

    /**
     * Check if Quark API processing is disabled
     * 
     * @param context
     * @return
     */
    public static boolean isDisabled(final ServletContext context) {
        return ServletUtils.isDisabled(context);
    }

    /**
     * Enable or disable Quark API Processsing
     * 
     * @param context
     * @param sts
     */
    public static void setDisabled(final ServletContext context, final boolean sts) {
        ServletUtils.setDisabled(context, sts);
    }
}
