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

	private int handle;
	private String type;
	private String tid;
	private long ts;
	private List<T> data;
	
	public int getHandle() {
		return handle;
	}

	public void setHandle(final int handle) {
		this.handle = handle;
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
		return "ExtJSDirectRequest [handle=" + handle + ", type=" + type + ", tid=" + tid + ", ts=" + ts + "]";
	}

}
