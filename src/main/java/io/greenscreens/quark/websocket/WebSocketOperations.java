/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 * 
 * https://www.greenscreens.io
 */
package io.greenscreens.quark.websocket;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import io.greenscreens.quark.JsonDecoder;
import io.greenscreens.quark.QuarkEngine;
import io.greenscreens.quark.QuarkSecurity;
import io.greenscreens.quark.QuarkUtil;
import io.greenscreens.quark.cdi.BeanManagerUtil;
import io.greenscreens.quark.cdi.IDestructibleBeanInstance;
import io.greenscreens.quark.ext.ExtEncrypt;
import io.greenscreens.quark.ext.ExtJSDirectRequest;
import io.greenscreens.quark.ext.ExtJSDirectResponse;
import io.greenscreens.quark.ext.ExtJSProtected;
import io.greenscreens.quark.ext.ExtJSResponse;
import io.greenscreens.quark.ext.annotations.ExtJSDirect;
import io.greenscreens.quark.security.IAesKey;
import io.greenscreens.quark.web.QuarkConstants;
import io.greenscreens.quark.web.QuarkErrors;
import io.greenscreens.quark.web.QuarkHandlerUtil;
import io.greenscreens.quark.web.ServletUtils;
import io.greenscreens.quark.websocket.data.WebSocketInstruction;

/**
 * Attach Java class to remote call
 */
public final class WebSocketOperations<T> {

	private static final Logger LOG = LoggerFactory.getLogger(WebSocketOperations.class);

	private boolean requiredSession = false;

	public void setRequiredSession(boolean requiredSession) {
		this.requiredSession = requiredSession;
	}

	/**
	 * Decrypt RSA/AES data from web, but only if AES in session is not found If new
	 * AES, store to session
	 * 
	 * @param session
	 * @param encrypt
	 * @return
	 * @throws Exception
	 */
	private String decryptData(final WebSocketSession session, final ExtEncrypt encrypt) throws IOException {

		IAesKey crypt = session.get(QuarkConstants.HTTP_SEESION_ENCRYPT);
		String data = null;

		if (crypt == null) {
			crypt = QuarkSecurity.initAES(encrypt.getK(), encrypt.isWebCryptoAPI());
			session.set(QuarkConstants.HTTP_SEESION_ENCRYPT, crypt);
			data = crypt.decrypt(encrypt.getD());
		} else {
			data = QuarkSecurity.decodeRequest(encrypt.getD(), encrypt.getK(), crypt, encrypt.isWebCryptoAPI());
		}

		return data;
	}

	/**
	 * Decrypt encrypted JSON data and continue as normal
	 * 
	 * @param request
	 * @param session
	 * @param uri
	 * @return
	 */	
	public ExtJSDirectResponse<T> processEncrypted(final ExtJSDirectRequest<T> request, final WebSocketSession session, final String uri) {

		ExtJSDirectResponse<T> directResponse = null;
		ExtJSResponse response = null;
		boolean err = false;

		try {

			final List<T> data = request.getData();
			final int size = data == null ? 0 : data.size();

			if (size == 0) {
				response = new ExtJSResponse(false, QuarkErrors.E0000.getMessage());
				response.setCode(QuarkErrors.E0000.getCode());
			} else {

				final Object paramData = data.get(0);

				if (paramData instanceof JsonNode) {

					decodeData(data, session);					
					directResponse = process(request, session.getContext(), session.getHttpSession(), uri);

				} else {
					response = new ExtJSResponse(false, QuarkErrors.E0000.getMessage());
					response.setCode(QuarkErrors.E0000.getCode());
				}
			}

		} catch (Exception e) {

			err = true;

			final String msg = QuarkUtil.toMessage(e);
			LOG.error(msg);
			LOG.debug(msg, e);
			response = new ExtJSResponse(e, msg);

		} finally {

			if (directResponse == null) {
				directResponse = new ExtJSDirectResponse<>(request, response);
				if (err) {
					directResponse.setType(WebSocketInstruction.ERR.getText());
				}
			}
		}

		return directResponse;
	}

	@SuppressWarnings("unchecked")
	private void decodeData(final List<T> data, final WebSocketSession session) throws IOException {

		final Object paramData = data.get(0);

		data.clear();

		final JsonNode jnode = (JsonNode) paramData;
		final ObjectMapper mapper = JsonDecoder.getJSONEngine();
		final ExtEncrypt encrypt = mapper.treeToValue(jnode, ExtEncrypt.class);
		final String json = decryptData(session, encrypt);
		final JsonNode node = JsonDecoder.parse(json);

		if (node.isArray()) {

			final ArrayNode arr = (ArrayNode) node;
			final Iterator<JsonNode> it = arr.iterator();

			while (it.hasNext()) {
				data.add((T) it.next());
			}

		} else {
			data.add((T) node);
		}

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ExtJSDirectResponse<T> process(final ExtJSDirectRequest<T> request, final ServletContext ctx,  final HttpSession httpSession, final String uri) {

		
		final boolean isDisabled = ServletUtils.isDisabled(ctx);
		ExtJSDirectResponse<T> directResponse = null;
		ExtJSResponse response = null;
		boolean err = false;

		try {

			final Bean<?> bean = QuarkHandlerUtil.findBean(request);
			final Class<?> beanClass = bean.getBeanClass();

			final AnnotatedType<?> annType = QuarkEngine.getBeanManager().createAnnotatedType(beanClass);
			final AnnotatedMethod selectedMethod = QuarkHandlerUtil.findMethod(request, annType);
			final ExtJSDirect direct = beanClass.getAnnotation(ExtJSDirect.class);

			boolean error = checkForError(selectedMethod, direct, httpSession, uri);

			if (error) {
				response = QuarkHandlerUtil.getError(QuarkErrors.E0001);
			} else {
				
				final boolean isProtected = selectedMethod.isAnnotationPresent(ExtJSProtected.class);				
				if (isProtected && isDisabled) {
					response = QuarkHandlerUtil.getError(QuarkErrors.E8888);
				} else {
					final List<AnnotatedParameter<?>> paramList = selectedMethod.getParameters();
					final Object[] params = QuarkHandlerUtil.fillParams(request, paramList);

					error = QuarkHandlerUtil.isParametersInvalid(paramList, params);
					if (error) {
						response = QuarkHandlerUtil.getError(QuarkErrors.E0002);
					} else {
						final Method javaMethod = QuarkHandlerUtil.toMethod(selectedMethod);
						response = executeBean(bean, javaMethod, params);
					}					
				}
				
			}

		} catch (Exception e) {
			final String msg = QuarkUtil.toMessage(e);
			LOG.error(msg);
			LOG.debug(msg, e);
			response = new ExtJSResponse(e, msg);
			err = true;
		} finally {

			directResponse = new ExtJSDirectResponse<>(request, response);
			
			if (err) {
				directResponse.setType(WebSocketInstruction.ERR.getText());
			}
		}

		return directResponse;
	}

	/// PRIVATE SECTION

	private boolean checkForError(final AnnotatedMethod<?> selectedMethod, final ExtJSDirect direct, final HttpSession httpSession, final String uri) {

		// check for path
		if (direct == null)
			return true;

		if (!QuarkHandlerUtil.checkPath(uri, direct.paths()))
			return true;

		if (requiredSession && ! QuarkHandlerUtil.isValidHttpSession(httpSession))
			return true;

		return (selectedMethod == null);
	}

	private ExtJSResponse executeBean(final Bean<?> bean, final Method method, final Object[] params) {

		ExtJSResponse response = null;
		IDestructibleBeanInstance<?> di = null;

		try {

			di = QuarkEngine.of(BeanManagerUtil.class).getDestructibleBeanInstance(bean);
			final Object beanInstance = di.getInstance();
			QuarkHandlerUtil.validateParameters(beanInstance, method, params);

			Object obj = null;
			
			final boolean isAsync= QuarkHandlerUtil.isAsync(method);
			if (isAsync) {
				CompletableFuture.runAsync(()->{ 
					try {
						method.invoke(beanInstance, params);
					} catch (Exception e) {
						QuarkHandlerUtil.printError(e);
					}		
				});

			} else {
				obj = method.invoke(beanInstance, params);				
			}

			response = QuarkHandlerUtil.toResponse(obj, method);
			
		} catch (Exception e) {
			response = new ExtJSResponse(e, e.getMessage());
			QuarkHandlerUtil.printError(e);
		} finally {

			if (di != null) {
				di.release();
			}

		}

		return response;
	}
	

}
