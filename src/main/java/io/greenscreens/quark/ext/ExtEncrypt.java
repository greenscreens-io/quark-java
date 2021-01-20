/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 * 
 * https://www.greenscreens.io
 * 
 */
package io.greenscreens.quark.ext;

import java.io.IOException;

import io.greenscreens.quark.IQuarkKey;
import io.greenscreens.quark.QuarkSecurity;
import io.greenscreens.quark.QuarkUtil;

public class ExtEncrypt {

	private String d;
	private String k;
	private String t;
	private int v;

	public String getD() {
		return d;
	}

	public void setD(final String d) {
		this.d = d;
	}

	public String getK() {
		return k;
	}

	public void setK(final String k) {
		this.k = k;
	}

	public int getV() {
		return v;
	}

	public void setV(final int v) {
		this.v = v;
	}

	public String getT() {
		return t;
	}

	public void setT(String t) {
		this.t = t;
	}

	public boolean isWebCryptoAPI() {
		return "1".equals(t);
	}

	public boolean isValid() {
		return !QuarkUtil.isEmpty(d) && !QuarkUtil.isEmpty(k);  
	}
	
	public IQuarkKey toKey() throws IOException {
		return QuarkSecurity.initAES(getK(), isWebCryptoAPI());
	}
	
	@Override
	public String toString() {
		return "ExtEncrypt [d=" + d + ", k=" + k + ", v=" + v + "]";
	}

}
