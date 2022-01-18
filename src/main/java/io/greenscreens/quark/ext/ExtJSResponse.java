/*
 * Copyright (C) 2015, 2022 Green Screens Ltd.
 */
package io.greenscreens.quark.ext;

import java.io.Serializable;
import java.util.Objects;

import javax.enterprise.inject.Vetoed;

/**
 * ExtJs standard response structure used by other extended response classes
 * 
 * { "success": false, "msg": "", "error": "", "stack": "" }
 */
@Vetoed
public class ExtJSResponse implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final boolean EXPOSE_ERROR = false;

	public enum Type {
		INFO, WARN, ERROR, NOTIFY
	}

	private boolean success;
	private String msg;
	private String code;
	private Throwable exception;
	private Type type = Type.INFO;

	public ExtJSResponse(final boolean success) {
		super();
		this.success = success;
		this.msg = null;
	}

	public ExtJSResponse(final boolean success, final String message) {
		super();
		this.success = success;
		this.msg = message;
	}

	public ExtJSResponse(final Throwable exception, final String message) {
		setError(exception, message);
	}

	public ExtJSResponse() {
		super();
		this.success = false;
	}

	public final boolean isSuccess() {
		return success;
	}

	public final void setSuccess(final boolean success) {
		this.success = success;
	}

	public final String getMsg() {
		return msg;
	}

	public final void setMsg(final String msg) {
		setMsg(Type.INFO, msg);
	}

	public final void setMsg(final Type type, final String msg) {
		this.msg = msg;
		this.type = type;
	}

	public final Throwable getException() {
		return exception;
	}

	public final void setException(final Throwable exception) {

		if (exception == null) {
			return;
		}

		if (EXPOSE_ERROR) {
			this.exception = exception;

			if (exception instanceof RuntimeException && exception.getCause() != null) {
				this.exception = exception.getCause();
			} 
		}

	}

	public final void setError(final Throwable exception, final String message) {
		if (Objects.nonNull(message)) {
			success = false;
			msg = message;
		}
		// setException(exception);
	}

	public String getCode() {
		return code;
	}

	public void setCode(final String code) {
		this.code = code;
	}

	public boolean isException() {
		return exception != null;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "ExtJSResponse [success=" + success + ", msg=" + msg + ", code=" + code + 
				", exception=" + exception	+ "]";
	}

	public static class Builder {
		
		private boolean success;
		private String msg;
		private String code;
		private Type type = Type.INFO;
        
        public Builder setStatus(final boolean status) {
        	this.success = status;
        	return this;
        }

        public Builder setMessage(final String message) {
        	this.msg = message;
        	return this;
        }
        
        public Builder setCode(final String code) {
        	this.code = code;
        	return this;
        }

        public ExtJSResponse build() {
        	final ExtJSResponse resp = new ExtJSResponse(success, msg);
        	resp.setCode(code);
        	resp.setType(type);
        	//resp.setError(exception, msg);
        	return resp;
        }

        public static Builder create() {
        	return new Builder();
        }

	}

	public static void main(String[] args) {
		ExtJSResponse r = ExtJSResponse.Builder.create().setStatus(true).setMessage("").build();
		System.out.println(r.isSuccess());
	}
}
