/*
 * Copyright (C) 2015, 2023 Green Screens Ltd.
 */
package io.greenscreens.quark.websocket;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import javax.enterprise.inject.Vetoed;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.greenscreens.quark.QuarkEngine;
import io.greenscreens.quark.QuarkUtil;
import io.greenscreens.quark.cdi.BeanManagerUtil;
import io.greenscreens.quark.cdi.IDestructibleBeanInstance;
import io.greenscreens.quark.ext.ExtJSDirectRequest;
import io.greenscreens.quark.ext.ExtJSDirectResponse;
import io.greenscreens.quark.ext.ExtJSProtected;
import io.greenscreens.quark.ext.ExtJSResponse;
import io.greenscreens.quark.ext.annotations.ExtJSDirect;
import io.greenscreens.quark.web.QuarkErrors;
import io.greenscreens.quark.web.QuarkHandlerUtil;
import io.greenscreens.quark.web.ServletUtils;
import io.greenscreens.quark.websocket.data.WebSocketInstruction;

/**
 * Attach Java class to remote call
 */
@Vetoed
public final class WebSocketOperations<T> {

	private static final Logger LOG = LoggerFactory.getLogger(WebSocketOperations.class);

	private boolean requiredSession = false;

	public void setRequiredSession(boolean requiredSession) {
		this.requiredSession = requiredSession;
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
		if (Objects.isNull(direct))
			return true;

		if (!QuarkHandlerUtil.checkPath(uri, direct.paths()))
			return true;

		if (requiredSession && ! QuarkHandlerUtil.isValidHttpSession(httpSession))
			return true;

		return Objects.isNull(selectedMethod);
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
			if (Objects.nonNull(di)) di.release();
		}

		return response;
	}

}
