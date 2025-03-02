/*
 * Copyright (C) 2015, 2023. Green Screens Ltd.
 */
package io.greenscreens.quark.internal;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

import io.greenscreens.quark.QuarkEngine;
import io.greenscreens.quark.ext.ExtJSDirectRequest;
import io.greenscreens.quark.ext.ExtJSDirectResponse;
import io.greenscreens.quark.ext.ExtJSResponse;
import io.greenscreens.quark.reflection.IQuarkHandle;
import io.greenscreens.quark.reflection.internal.QuarkMapper;
import io.greenscreens.quark.security.IQuarkKey;
import io.greenscreens.quark.security.QuarkSecurity;
import io.greenscreens.quark.stream.QuarkStream;
import io.greenscreens.quark.util.QuarkJson;
import io.greenscreens.quark.util.QuarkUtil;
import io.greenscreens.quark.util.ReflectionUtil;
import io.greenscreens.quark.util.override.MIME;
import io.greenscreens.quark.web.ServletStorage;
import io.greenscreens.quark.web.ServletUtils;
import io.greenscreens.quark.web.data.WebRequest;
import io.greenscreens.quark.websocket.WebSocketSession;
import io.greenscreens.quark.websocket.data.IWebSocketResponse;
import io.greenscreens.quark.websocket.data.WebSocketInstruction;
import io.greenscreens.quark.websocket.data.WebSocketResponse;
import jakarta.enterprise.inject.Vetoed;
import jakarta.enterprise.inject.spi.AnnotatedParameter;
import jakarta.servlet.AsyncContext;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

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

	private IQuarkKey quarkKey;
	
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
		this.quarkKey = getAes();
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
		this.quarkKey = getAes();
	}
		
	public ServletContext getServletContext() {
		return ctx;
	}

	public HttpSession getSession() {
		return isWebSocket() ? wsSession.getHttpSession() : httpRequest.getSession(requireSession);
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

    /**
     * Check if handler process request for WebSocket or Servlet
     * @return
     */
    private boolean isWebSocket() {
        return Objects.nonNull(wsSession);
    }
    
	public ExtJSDirectRequest<JsonNode> getRequest() {
		return request;
	}

	/**
	 * Check if session is required to call a Caller
	 * @return
	 */
	private boolean isSessionRequired() {
		
		Boolean state = ServletStorage.get(ctx, QuarkConstants.QUARK_SESSION);
		
		if (isWebSocket()) {
			state = getState(wsSession.get(QuarkConstants.QUARK_SESSION), state);
		} else {
			state = getState(ServletStorage.get(httpRequest, QuarkConstants.QUARK_SESSION), state);
		}

		return Objects.isNull(state) ? false: state;
	}

	private Boolean getState(final Boolean state, final Boolean def) {
		return Objects.isNull(state) ? def : state;		
	}
	
	/**
	 * Main processing
	 */
	public void call() {
		try {
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
			buffer = QuarkStream.unwrap(buffer, quarkKey);
			body = new String(buffer.array(), StandardCharsets.UTF_8);
		} else {
			compress = ServletUtils.supportGzip(httpRequest);
			body = ServletUtils.getBodyAsString(httpRequest);			
		}

		request = QuarkJson.convert(WebRequest.class, body);

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
	
    public boolean send(final Throwable exception) {
        final ExtJSResponse response = new ExtJSResponse(exception, QuarkUtil.toMessage(exception));
        return send(response);
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
	
		if (Objects.nonNull(quarkKey)) {
			final String json = QuarkJson.stringify(result);			
			final ByteBuffer buff = QuarkStream.wrap(json, quarkKey, compress, null);
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

		final IQuarkHandle handle = QuarkMapper.get(request.getHandle());
		boolean error = checkForError(handle, uri);
		if (error) {
			response = QuarkHandlerUtil.getError(QuarkErrors.E0001);
			return error;
		}
		
        // if method is protected and disabled for use, prevent call 
        if (handle.isProtected()) {
            if (ServletUtils.isDisabled(ctx)) {
                response = QuarkHandlerUtil.getError(QuarkErrors.E8888);
                return true;                
            }
        }
        
		final List<AnnotatedParameter<AnnotatedParameter<?>>> paramList = handle.annotatedMethod().getParameters();
		final Object[] params = QuarkHandlerUtil.fillParams(request, paramList);
		
		error = ReflectionUtil.isParametersInvalid(paramList, params);
		if (error) {
			response = QuarkHandlerUtil.getError(QuarkErrors.E0002);
		} else {
			QuarkBeanCaller.get(this, handle, params).call();
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
		if (Objects.nonNull(quarkKey)) return quarkKey;
	      return isWebSocket() ? getAesWs() : getAesWeb();
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
		IQuarkKey aesKey = ServletStorage.get(session, QuarkConstants.ENCRYPT_ENGINE);

		if (Objects.nonNull(aesKey)) return aesKey; 		
		final String publicKey = QuarkHandlerUtil.getPublicKey(httpRequest);			
		aesKey = QuarkSecurity.initWebKey(publicKey);
		
		ServletStorage.put(session, QuarkConstants.ENCRYPT_ENGINE, aesKey);

		return aesKey;

	}

	/**
	 * Validate access control. Controller defined path must match to the WebSocket or Servlet path.   
	 *
	 * @param selectedMethod
	 * @param direct
	 * @param uri
	 * @return
	 */
	private boolean checkForError(final IQuarkHandle handle, final String uri) {

		// check for path
		if (Objects.isNull(handle))
			return true;

		if (!QuarkMapper.find(handle).get().accept(uri))
			return true;

		if (requireSession && ! ServletUtils.isValidHttpSession(getSession()))
			return true;

		return Objects.isNull(handle.annotatedMethod());
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
