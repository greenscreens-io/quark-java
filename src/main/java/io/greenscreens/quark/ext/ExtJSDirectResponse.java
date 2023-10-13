/*
 * Copyright (C) 2015, 2023. Green Screens Ltd.
 */
package io.greenscreens.quark.ext;

import java.util.Objects;
import jakarta.enterprise.inject.Vetoed;

/**
 * Internal Engine class which wraps requests/responses between web and server
 *
 * @param <T>
 */
@Vetoed
public class ExtJSDirectResponse<T> {

	private Long mid;
	private String type;
	private String tid = "-1";
	private Object result;

	public ExtJSDirectResponse(final ExtJSDirectRequest<T> request, final Object response) {
		super();

		this.result = response;

		if (Objects.nonNull(request)) {
			this.mid = request.getHandle();
			this.tid = request.getTid();
			this.type = request.getType();
		}

	}

	public Long getMid() {
		return mid;
	}

	public void setMid(Long mid) {
		this.mid = mid;
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
		return "ExtJSDirectResponse [mid=" + mid + ", type=" + type + ", tid=" + tid + ", result=" + result + "]";
	}

}
