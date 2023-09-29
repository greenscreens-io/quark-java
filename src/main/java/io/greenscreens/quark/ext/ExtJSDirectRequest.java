/*
 * Copyright (C) 2015, 2023 Green Screens Ltd.
 */
package io.greenscreens.quark.ext;

import java.util.List;
import jakarta.enterprise.inject.Vetoed;

/**
 * Class representing ExtJS Direct request. It is used for decoding received
 * JSON data from ExtJS into Java class instance
 * 
 * @param <T>
 */
@Vetoed
public class ExtJSDirectRequest<T> {

	private String action;
	private String method;
	private String namespace;
	private String type;
	private String tid;
	private long ts;
	private List<T> data;

	public final String getNamespace() {
		return namespace;
	}

	public final void setNamespace(final String namespace) {
		this.namespace = namespace;
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
	
	public long getTs() {
		return ts;
	}

	public void setTs(long ts) {
		this.ts = ts;
	}

	public final List<T> getData() {
		return data;
	}

	public final void setData(final List<T> data) {
		this.data = data;
	}

	public final T getDataByIndex(final int index) {

		T value = null;

		if (data != null && !data.isEmpty()) {
			value = data.get(index);
		}

		return value;
	}

	@Override
	public String toString() {
		return "ExtJSDirectRequest [action=" + action + ", method=" + method + ", namespace=" + namespace + ", type="
				+ type + ", tid=" + tid + ", data=" + data + "]";
	}

}
