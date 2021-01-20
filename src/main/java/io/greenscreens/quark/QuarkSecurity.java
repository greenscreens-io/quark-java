/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 * 
 * https://www.greenscreens.io
 * 
 */
package io.greenscreens.quark;

import java.io.IOException;
import io.greenscreens.quark.security.IAesKey;
import io.greenscreens.quark.security.Security;

/**
 * Mediator between 2 libs - quark and security
 *
 */
public enum QuarkSecurity {
;
	
	public static void initialize() {
		SecurityProvider.get();
		Security.generateRSAKeys(); 
	}

	public static IQuarkKey initAES(final String k, final boolean webCryptoAPI) throws IOException {
		return (IQuarkKey) Security.initAES(k, webCryptoAPI);
	}

	public static String decodeRequest(final String d, final String k, final IAesKey crypt, final boolean webCryptoAPI) throws IOException {
		return Security.decodeRequest(d, k, crypt, webCryptoAPI);
	}

	public static final byte[] getRandom(final int blockSize) {
		return Security.getRandom(blockSize);
	}
	
	public static String getRSAPublic(final boolean webCryptoAPI) {
		return Security.getRSAPublic(webCryptoAPI);
	}

	public static String getRSAVerifier(final boolean webCryptoAPI) {
		return Security.getRSAVerifier(webCryptoAPI);
	}
	
	/**
	 * Create digital signature for challenge 
	 * @param challenge
	 * @return
	 */
	public static String signApiKey(final String challenge) {
		final String keyEnc = Security.getRSAPublic(true);
		final String keyVer = Security.getRSAVerifier(true);
		final String data = String.format("%s%s%s", challenge, keyEnc, keyVer);
		return Security.sign(data, false, true);
	}

}
