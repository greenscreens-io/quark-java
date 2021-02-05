/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 * 
 * https://www.greenscreens.io
 */
package io.greenscreens.quark.web;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.servlet.AsyncContext;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.greenscreens.quark.QuarkEngine;
import io.greenscreens.quark.QuarkSecurity;
import io.greenscreens.quark.QuarkUtil;
import io.greenscreens.quark.ext.ExtEncrypt;
import io.greenscreens.quark.ext.ExtJSDirectRequest;
import io.greenscreens.quark.ext.ExtJSDirectResponse;
import io.greenscreens.quark.ext.ExtJSProtected;
import io.greenscreens.quark.ext.ExtJSResponse;
import io.greenscreens.quark.ext.annotations.ExtJSDirect;
import io.greenscreens.quark.web.data.WebRequest;
import io.greenscreens.quark.websocket.WebSocketSession;
import io.greenscreens.quark.websocket.data.IWebSocketResponse;
import io.greenscreens.quark.websocket.data.WebSocketInstruction;
import io.greenscreens.quark.websocket.data.WebSocketResponseFactory;
import io.greenscreens.quark.security.IAesKey;
import io.greenscreens.quark.JsonDecoder;

/**
 * Handle Quark request with support for WebSocket, HTTP, Async
 */
public class QuarkHandler {

	private static final Logger LOG = LoggerFactory.getLogger(QuarkHandler.class);

	final String uri;
	ServletContext ctx;
	WebSocketSession wsSession;
	HttpServletRequest httpRequest;
	HttpServletResponse httpResponse;

	final boolean supportAsync;
	final boolean isBinary;
	final boolean isEncrypted;
	final boolean requireSession;

	boolean sent = false;

	ExtJSDirectRequest<JsonNode> request;
	ExtJSResponse response;
	IAesKey crypt; 

	public QuarkHandler(final WebSocketSession wsSession, final ExtJSDirectRequest<JsonNode> data, final String uri, final boolean isBinary, final boolean isEncrypted) {
		super();
		this.uri = uri;
		this.isBinary = isBinary;
		this.isEncrypted = isEncrypted;
		this.wsSession = wsSession;
		this.request = data;
		this.httpRequest = null;
		this.httpResponse = null;
		this.supportAsync = true;
		this.ctx = wsSession.getContext();
		this.requireSession = isSessionRequired();
	}

	public QuarkHandler(final HttpServletRequest request, final HttpServletResponse response, final ExtJSDirectRequest<JsonNode> data, final String uri) {
		super();
		this.uri = uri;
		this.isEncrypted = false;
		this.isBinary = false;
		this.wsSession = null;
		this.request = data;
		this.httpRequest = request;
		this.httpResponse = response;
		this.supportAsync = request.isAsyncSupported();
		this.ctx = request.getServletContext();
		this.requireSession = isSessionRequired();
		this.httpResponse.setContentType("application/json");

	}
	
	/**
	 * Check if session is required to call a Caller
	 * @return
	 */
	private boolean isSessionRequired() {
		if (isWebSocket()) {
			final Boolean state = wsSession.get(QuarkConstants.WEBSOCKET_SESSION);
			if (Objects.isNull(state)) return false;
			return state;
		}
		return false;
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
		
		// to save processing
		if (!isEncrypted) {
			return;
		}
		
		final List<JsonNode> data = request.getData();
		final int size = data == null ? 0 : data.size();

		if (size == 0) {
			response = QuarkHandlerUtil.getError(QuarkErrors.E0000);
			return;
		} 
		
		final Object paramData = data.get(0);

		if (paramData instanceof JsonNode) {
			decodeData(data);
		}
	
	}
	
	/**
	 * Get request from Servlet. Decrypt if encrypted.
	 * @throws IOException
	 */
	private void prepareHTTP() throws IOException {
		
		if (Objects.nonNull(request)) return;
		
		final String body = ServletUtils.getBodyAsString(httpRequest);
		final ObjectNode node = JsonDecoder.parseType(body);

		final ObjectMapper mapper = JsonDecoder.getJSONEngine();
		final ExtEncrypt encrypt = mapper.treeToValue(node, ExtEncrypt.class);

		if (encrypt.isValid()) {
			final String json = decryptData(encrypt);
			final JsonNode jsonNode = JsonDecoder.parse(json);
			request = JsonDecoder.convert(WebRequest.class, jsonNode);
		} else {
			request = JsonDecoder.convert(WebRequest.class, node); 
		}
				
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
		
		final boolean isCompression = wsSession.get(QuarkConstants.WEBSOCKET_COMPRESSION);		
		final IWebSocketResponse wsResponse = WebSocketResponseFactory.createAsData(isBinary, isCompression);	
		wsResponse.setData(responseList);
		wsSession.sendResponse(wsResponse, true);
		
	}
	
	/**
	 * Send response for Servlet
	 * @throws IOException
	 */
	private void sendHTTP() throws IOException {
		
		final ExtJSDirectResponse<JsonNode> result = getResult();
		
		if (isEncrypted()) {
			final String json = JsonDecoder.stringify(result);
			final ObjectNode node = QuarkHandlerUtil.encrypt(crypt, json);
			ServletUtils.sendResponse(httpResponse, node);				
		} else {		
			ServletUtils.sendResponse(httpResponse, result);
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
		final AnnotatedMethod selectedMethod = QuarkHandlerUtil.findMethod(request, annType);
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
				final List<AnnotatedParameter<?>> paramList = selectedMethod.getParameters();
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
	private IAesKey getAes(final ExtEncrypt encrypt) throws IOException {
		if (isWebSocket()) {
			return getAesWs(encrypt);
		} else {
			return getAesWeb(encrypt);
		}
	}

	/**
	 * Get encryption key for WebSocket
	 * @param encrypt
	 * @return
	 * @throws IOException
	 */
	private IAesKey getAesWs(final ExtEncrypt encrypt) throws IOException {
		IAesKey aesKey = wsSession.get(QuarkConstants.HTTP_SEESION_ENCRYPT);
		if (Objects.isNull(aesKey)) {
			aesKey = encrypt.toKey();
			wsSession.set(QuarkConstants.HTTP_SEESION_ENCRYPT, aesKey);
		}
		return aesKey;
	}

	/**
	 * Get encryption key for Servlet 
	 * @param encrypt
	 * @return
	 * @throws IOException
	 */
	private IAesKey getAesWeb(final ExtEncrypt encrypt) throws IOException {
		final HttpSession session = getSession();
		IAesKey aesKey = ServletUtils.get(session, QuarkConstants.HTTP_SEESION_ENCRYPT);
		if (Objects.isNull(aesKey)) {
			aesKey = encrypt.toKey();
			ServletUtils.put(session, QuarkConstants.HTTP_SEESION_ENCRYPT, aesKey);
		}
		return aesKey;
	}

	/**
	 * Decode encrypted request data
	 * @param data
	 * @throws IOException
	 */
	private void decodeData(final List<JsonNode> data) throws IOException {

		final Object paramData = data.get(0);

		data.clear();

		final JsonNode jnode = (JsonNode) paramData;
		
		final ExtEncrypt encrypt = JsonDecoder.convert(ExtEncrypt.class, jnode);
		
		if (!encrypt.isValid()) return;
		
		final String json = decryptData(encrypt);
		final JsonNode node = JsonDecoder.parse(json);

		if (node.isArray()) {

			final ArrayNode arr = (ArrayNode) node;
			final Iterator<JsonNode> it = arr.iterator();

			while (it.hasNext()) {
				data.add(it.next());
			}

		} else {
			data.add(node);
		}

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
	private String decryptData(final ExtEncrypt encrypt) throws IOException {
		crypt = getAes(encrypt);
		return QuarkSecurity.decodeRequest(encrypt.getD(), encrypt.getK(), crypt, encrypt.isWebCryptoAPI());
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
	 * Check if received data is encrypted
	 * @return
	 */
	private boolean isEncrypted() {
		return Objects.nonNull(crypt);
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
	public static void call(final WebSocketSession wsSession, final ExtJSDirectRequest<JsonNode> data, final boolean isBinary, final boolean isEncrypted) {
		final String uri = wsSession.get(QuarkConstants.WEBSOCKET_PATH);
		call(wsSession, data, uri, isBinary, isEncrypted);
	}
	
	/**
	 * Start processing for WebSocket
	 * @param wsSession
	 * @param data
	 * @param uri
	 * @param isBinary
	 * @param isEncrypted
	 */
	public static void call(final WebSocketSession wsSession, final ExtJSDirectRequest<JsonNode> data, final String uri, final boolean isBinary, final boolean isEncrypted) {
		new QuarkHandler(wsSession, data, uri, isBinary, isEncrypted).call();
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
