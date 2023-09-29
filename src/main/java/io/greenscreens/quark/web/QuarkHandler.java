/*
 * Copyright (C) 2015, 2023 Green Screens Ltd.
 */
package io.greenscreens.quark.web;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jakarta.enterprise.inject.Vetoed;
import jakarta.enterprise.inject.spi.AnnotatedMethod;
import jakarta.enterprise.inject.spi.AnnotatedParameter;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.servlet.AsyncContext;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

import io.greenscreens.quark.IQuarkKey;
import io.greenscreens.quark.JsonDecoder;
import io.greenscreens.quark.MIME;
import io.greenscreens.quark.QuarkEngine;
import io.greenscreens.quark.QuarkSecurity;
import io.greenscreens.quark.QuarkStream;
import io.greenscreens.quark.QuarkUtil;
import io.greenscreens.quark.ext.ExtJSDirectRequest;
import io.greenscreens.quark.ext.ExtJSDirectResponse;
import io.greenscreens.quark.ext.ExtJSProtected;
import io.greenscreens.quark.ext.ExtJSResponse;
import io.greenscreens.quark.ext.annotations.ExtJSDirect;
import io.greenscreens.quark.web.data.WebRequest;
import io.greenscreens.quark.websocket.WebSocketSession;
import io.greenscreens.quark.websocket.data.IWebSocketResponse;
import io.greenscreens.quark.websocket.data.WebSocketInstruction;
import io.greenscreens.quark.websocket.data.WebSocketResponse;

/**
 * Handle Quark request with support for WebSocket, HTTP, Async
 */
@Vetoed
public class QuarkHandler {

	private static final Logger LOG = LoggerFactory.getLogger(QuarkHandler.class);

	private final String uri;
	private ServletContext ctx;
	private WebSocketSession wsSession;
	private HttpServletRequest httpRequest;
	private HttpServletResponse httpResponse;

	private final boolean supportAsync;
	private final boolean requireSession;

	private boolean compress = false;
	private boolean sent = false;

	private IQuarkKey aes;
	
	private ExtJSDirectRequest<JsonNode> request;
	public ExtJSResponse response;

	public QuarkHandler(final WebSocketSession wsSession, final ExtJSDirectRequest<JsonNode> data, final String uri) {
		super();
		this.uri = uri;
		this.wsSession = wsSession;
		this.request = data;
		this.httpRequest = null;
		this.httpResponse = null;
		this.supportAsync = true;
		this.ctx = wsSession.getContext();
		this.requireSession = isSessionRequired();
		this.aes = getAes();
	}

	public QuarkHandler(final HttpServletRequest request, final HttpServletResponse response, final ExtJSDirectRequest<JsonNode> data, final String uri) {
		super();
		this.uri = uri;
		this.wsSession = null;
		this.request = data;
		this.httpRequest = request;
		this.httpResponse = response;
		this.supportAsync = request.isAsyncSupported();
		this.ctx = request.getServletContext();
		this.requireSession = isSessionRequired();
		this.httpResponse.setContentType("application/json");
		this.aes = getAes();
	}
		
	public WebSocketSession getWsSession() {
		return wsSession;
	}

	public HttpServletRequest getHttpRequest() {
		return httpRequest;
	}

	public HttpServletResponse getHttpResponse() {
		return httpResponse;
	}

	public boolean isSupportAsync() {
		return supportAsync;
	}

	public ExtJSDirectRequest<JsonNode> getRequest() {
		return request;
	}

	/**
	 * Check if session is required to call a Caller
	 * @return
	 */
	private boolean isSessionRequired() {
		
		Boolean state = ServletUtils.get(ctx, QuarkConstants.QUARK_SESSION);
		
		if (isWebSocket()) {
			state = getState(wsSession.get(QuarkConstants.QUARK_SESSION), state);
		} else {
			state = getState(ServletUtils.get(httpRequest, QuarkConstants.QUARK_SESSION), state);
		}

		return Objects.isNull(state) ? false: state;
	}

	private Boolean getState(Boolean state, Boolean def) {
		return Objects.isNull(state) ? def : state;		
	}
	
	/**
	 * Get web session from WebSocket or Servlet depending on request source.
	 * @return
	 */
	private HttpSession getSession() {
		if (isWebSocket()) {
			return wsSession.getHttpSession();
		}
		return httpRequest.getSession(requireSession);
	}

	/**
	 * Main processing
	 */
	public void call() {

		try {
			
			final boolean isDisabled = ServletUtils.isDisabled(ctx);
			if (isDisabled) {
				response = QuarkHandlerUtil.getError(QuarkErrors.E8888);
				send();
				return;
			}

			prepare();
			
			if (doProcess()) {
				send();
			}

		} catch (Exception e) {
			final String msg = QuarkUtil.toMessage(e);
			LOG.error(msg);
			LOG.debug(msg, e);
			response = new ExtJSResponse(e, msg);
			send();
		}

	}
	
	/**
	 * Help GC to release resources
	 */
	private void cleanup() {
		ctx = null;
		wsSession = null;
		httpRequest = null;
		httpResponse = null;
		request = null;
		response = null;
	}
	
	/**
	 * Prepare request from input
	 * @throws IOException
	 */
	private void prepare() throws IOException {
		
		if (QuarkEngine.TIMESTAMP > 0) {
			final long diff = QuarkUtil.timediff(request.getTs());
			if (diff > QuarkEngine.TIMESTAMP) {
				throw new IOException("Request timeout");
			}
		}

		if (Objects.isNull(wsSession)) {
			prepareHTTP();
		} else {
			prepareWS();
		}
	}
	
	/**
	 * Get request from WebSocket. Decrypt if encrypted.
	 * @throws IOException
	 */
	private void prepareWS() throws IOException {
		
		final List<JsonNode> data = request.getData();
		final int size = Objects.isNull(data) ? 0 : data.size();

		if (size == 0) {
			response = QuarkHandlerUtil.getError(QuarkErrors.E0000);
		}
	}
	
	/**
	 * Get request from Servlet. Decrypt if encrypted.
	 * @throws IOException
	 */
	private void prepareHTTP() throws IOException {
		
		if (Objects.nonNull(request)) return;
		
		final String val = QuarkUtil.normalize(httpRequest.getContentType());
		final MIME mime = MIME.toMime(val); 		
		
		String body = null;
		if (MIME.OCTET == mime) {
			ByteBuffer buffer = ServletUtils.getBodyAsBuffer(httpRequest);
			final int type = QuarkStream.type(buffer);
			compress = QuarkStream.isCompress(type);
			buffer = QuarkStream.unwrap(buffer, aes);
			body = new String(buffer.array(), StandardCharsets.UTF_8);
		} else {
			compress = ServletUtils.supportGzip(httpRequest);
			body = ServletUtils.getBodyAsString(httpRequest);			
		}

		final JsonNode jsonNode = JsonDecoder.parse(body);
		request = JsonDecoder.convert(WebRequest.class, jsonNode);

	}

	/**
	 * Wrap Controller response into Quark response structure which holds data
	 * about requester ID so that front end know which callback to call
	 * @return
	 */
	private ExtJSDirectResponse<JsonNode> getResult() {
		
		final ExtJSDirectResponse<JsonNode> directResponse = new ExtJSDirectResponse<>(request, response);
		
		if (response.isException()) {
			directResponse.setType(WebSocketInstruction.ERR.getText());
		}
		
		return directResponse;
	} 
	
	public AsyncContext getContext() {
		
		if (Objects.nonNull(httpRequest)) {
		
			if (httpRequest.isAsyncStarted()) {
				return httpRequest.getAsyncContext();
			}
			
			return httpRequest.startAsync(httpRequest, httpResponse);
		}
		
		return null;
	}
	
	public boolean send(final ExtJSResponse response) {
		this.response = response;
		return send();
	}
	
	/**
	 * Send response to requester. Determine is it for WebSocket or Servlet
	 * @return
	 */
	protected boolean send() {
		
		if (Objects.isNull(response)) return false;
		if (sent) return !sent;

		try {
			if (Objects.isNull(wsSession)) {
				sendHTTP();
			} else {
				sendWS();
			}			
			
			sent = true;
		} catch (Exception e) {
			final String msg = QuarkUtil.toMessage(e);
			LOG.error(msg);
			LOG.debug(msg, e);
			return false;
		} finally {
			cleanup();
		}

		return true;
	}
	
	/**
	 * Send response for WebSocket
	 */
	private void sendWS() {

		final ExtJSDirectResponse<JsonNode> result = getResult();	

		final List<ExtJSDirectResponse<?>> responseList = new ArrayList<>();
		responseList.add(result);
	
		final IWebSocketResponse wsResponse = WebSocketResponse.asData(responseList);	
		wsSession.sendResponse(wsResponse, true);
		
	}
	
	/**
	 * Send response for Servlet
	 * @throws IOException
	 */
	private void sendHTTP() throws IOException {
		
		final ExtJSDirectResponse<JsonNode> result = getResult();
	
		if (Objects.nonNull(aes)) {
			final String json = JsonDecoder.stringify(result);			
			final ByteBuffer buff = QuarkStream.wrap(json, aes, compress);
			ServletUtils.sendResponse(httpResponse, buff, false);				
		} else {		
			ServletUtils.sendResponse(httpResponse, result, compress);
		}
		
		if (httpRequest.isAsyncStarted()) {
			httpRequest.getAsyncContext().complete();
		}	
	}

	/**
	 * Prepare Controller - validate access, prepare parameters
	 * @throws IOException
	 */
	private boolean doProcess() throws IOException {

		final Bean<?> bean = QuarkHandlerUtil.findBean(request);
		final Class<?> beanClass = bean.getBeanClass();

		final AnnotatedType<?> annType = QuarkEngine.getBeanManager().createAnnotatedType(beanClass);
		final AnnotatedMethod<AnnotatedParameter<?>> selectedMethod = QuarkHandlerUtil.findMethod(request, annType);
		final ExtJSDirect direct = beanClass.getAnnotation(ExtJSDirect.class);

		boolean error = checkForError(selectedMethod, direct, uri);

		if (error) {
			response = QuarkHandlerUtil.getError(QuarkErrors.E0001);
		} else {

			final boolean isProtected = selectedMethod.isAnnotationPresent(ExtJSProtected.class);
			if (isProtected) {
				error = true;
				response = QuarkHandlerUtil.getError(QuarkErrors.E8888);
			} else {
				final List<AnnotatedParameter<AnnotatedParameter<?>>> paramList = selectedMethod.getParameters();
				final Object[] params = QuarkHandlerUtil.fillParams(request, paramList);

				error = QuarkHandlerUtil.isParametersInvalid(paramList, params);
				if (error) {
					response = QuarkHandlerUtil.getError(QuarkErrors.E0002);
				} else {
					final Method javaMethod = QuarkHandlerUtil.toMethod(selectedMethod);
					QuarkBeanCaller.get(this, bean, javaMethod, params).call();
				}
			}

		}
		
		return error;
	}

	
	/**
	 * Get encryption key 
	 * @param encrypt
	 * @return
	 * @throws IOException
	 */
	private IQuarkKey getAes() {
		if (Objects.nonNull(aes)) return aes;
		if (isWebSocket()) {
			aes = getAesWs();
		} else {
			aes = getAesWeb();
		}
		return aes;
	}

	/**
	 * Get encryption key for WebSocket
	 * @param encrypt
	 * @return
	 * @throws IOException
	 */
	private IQuarkKey getAesWs() {
		return wsSession.get(QuarkConstants.ENCRYPT_ENGINE);
	}

	/**
	 * Get encryption key for Servlet 
	 * @param encrypt
	 * @return
	 * @throws IOException
	 */
	private IQuarkKey getAesWeb() {

		final HttpSession session = getSession();
		IQuarkKey aesKey = ServletUtils.get(session, QuarkConstants.ENCRYPT_ENGINE);

		if (Objects.nonNull(aesKey)) return aesKey; 		
		final String publicKey = getPublicKey();			
		aesKey = QuarkSecurity.initWebKey(publicKey);
		
		ServletUtils.put(session, QuarkConstants.ENCRYPT_ENGINE, aesKey);

		return aesKey;

	}

	/**
	 * Get PubicKey from HTTP request
	 * @return
	 */
	private String getPublicKey() {
		String publicKey = httpRequest.getHeader(QuarkConstants.WEB_KEY);		
		if (QuarkUtil.isEmpty(publicKey)) {
			publicKey = ServletUtils.getCookie(httpRequest, QuarkConstants.WEB_KEY);
		}	
		return publicKey;
	}
	

	/**
	 * Validate access control. Controller defined path must match to the WebSocket or Servlet path.   
	 *
	 * @param selectedMethod
	 * @param direct
	 * @param uri
	 * @return
	 */
	private boolean checkForError(final AnnotatedMethod<?> selectedMethod, final ExtJSDirect direct, final String uri) {

		// check for path
		if (direct == null)
			return true;

		if (!QuarkHandlerUtil.checkPath(uri, direct.paths()))
			return true;

		if (requireSession && ! QuarkHandlerUtil.isValidHttpSession(getSession()))
			return true;

		return (selectedMethod == null);
	}
	
	/**
	 * Check if handler process request for WebSocket or Servlet
	 * @return
	 */
	private boolean isWebSocket() {
		return Objects.nonNull(wsSession);
	}

	/**
	 * Start processing for WebSocket
	 * @param wsSession
	 * @param data
	 * @param isBinary
	 * @param isEncrypted
	 */
	public static void call(final WebSocketSession wsSession, final ExtJSDirectRequest<JsonNode> data) {
		final String uri = wsSession.get(QuarkConstants.QUARK_PATH);
		call(wsSession, data, uri);
	}
	
	/**
	 * Start processing for WebSocket
	 * @param wsSession
	 * @param data
	 * @param uri
	 * @param isBinary
	 * @param isEncrypted
	 */
	public static void call(final WebSocketSession wsSession, final ExtJSDirectRequest<JsonNode> data, final String uri) {
		new QuarkHandler(wsSession, data, uri).call();
	}
	
	/**
	 * Start processing for Servlet
	 * @param request
	 * @param response
	 */
	public static void call(final HttpServletRequest request, final HttpServletResponse response) {
		call(request, response, null, request.getServletPath());
	}
		
	/**
	 * Start processing for Servlet
	 * @param request
	 * @param response
	 * @param data
	 * @param uri
	 * @param supportAsync
	 */
	public static void call(final HttpServletRequest request, final HttpServletResponse response, final ExtJSDirectRequest<JsonNode> data, final String uri) {
		new QuarkHandler(request, response, data, uri).call();
	}

}
