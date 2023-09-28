/*
 * Copyright (C) 2015, 2023 Green Screens Ltd.
 */
package io.greenscreens.quark.ext;

import java.util.Objects;

import javax.enterprise.inject.Vetoed;

/**
 * Internal Engine class which wraps requests/responses between web and server
 *
 * @param <T>
 */
@Vetoed
public class ExtJSDirectResponse<T> {

	private String action;
	private String method;
	private String type;
	private String tid = "-1";
	private Object result;

	public ExtJSDirectResponse(final ExtJSDirectRequest<T> request, final Object response) {
		super();

		this.result = response;

		if (Objects.nonNull(request)) {
			this.action = request.getAction();
			this.method = request.getMethod();
			this.tid = request.getTid();
			this.type = request.getType();
		}

	}

	public final String getAction() {
		return action;
	}

	public final void setAction(final String action) {
		this.action = action;
	}

	public final String getMethod() {
		return method;
	}

	public final void setMethod(final String method) {
		this.method = method;
	}

	public final String getType() {
		return type;
	}

	public final void setType(final String type) {
		this.type = type;
	}

	public final String getTid() {
		return tid;
	}

	public final void setTid(final String tid) {
		this.tid = tid;
	}

	public final Object getResult() {
		return result;
	}

	public final void setResult(final Object result) {
		this.result = result;
	}

	@Override
	public String toString() {
		return "ExtJSDirectResponse [action=" + action + ", method=" + method + ", type=" + type + ", tid=" + tid + ", result=" + result + "]";
	}

}
