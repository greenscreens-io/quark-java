package io.greenscreens.quark;

import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import io.greenscreens.quark.async.QuarkAsyncResponse;
import io.greenscreens.quark.web.QuarkRequest;
import io.greenscreens.quark.websocket.WebSocketSession;

@ApplicationScoped
public class QuarkProducer {

	private static final ThreadLocal<WebSocketSession> websocketContextThreadLocal = new ThreadLocal<>();
	private static final ThreadLocal<QuarkAsyncResponse> asyncContextThreadLocal = new ThreadLocal<>();
	private static final ThreadLocal<QuarkRequest> httpThreadLocal = new ThreadLocal<>();

	@Produces
	public WebSocketSession sessionProducer() {
		return websocketContextThreadLocal.get();
	}

	@Produces
	public QuarkAsyncResponse asyncResponseProducer() {
		return asyncContextThreadLocal.get();
	}

	@Produces
	public QuarkRequest httpRequestProducer() {
		return httpThreadLocal.get();
	}

	public static void attachRequest(final QuarkRequest request) {
		if (Objects.nonNull(request)) {
			httpThreadLocal.set(request);
		}
	}

	public static void releaseRequest() {
		httpThreadLocal.remove();
	}
	
	public static QuarkRequest getRequest() {
		return httpThreadLocal.get();
	}	
	
	public static void attachSession(final WebSocketSession session) {
		if (Objects.nonNull(session)) {
			websocketContextThreadLocal.set(session);
		}
	}

	public static void releaseSession() {
		websocketContextThreadLocal.remove();
	}
	
	public static WebSocketSession getSession() {
		return websocketContextThreadLocal.get();
	}

	public static void attachAsync(final QuarkAsyncResponse session) {
		if (Objects.nonNull(session)) {
			asyncContextThreadLocal.set(session);
		}
	}
	
	public static void releaseAsync() {
		asyncContextThreadLocal.remove();
	}
	
	public static QuarkAsyncResponse getAsync() {
		return asyncContextThreadLocal.get();
	}
}
