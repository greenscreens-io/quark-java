/*
 * Copyright (C) 2015, 2022 Green Screens Ltd.
 */
package io.greenscreens.quark;

import java.io.IOException;

import io.greenscreens.quark.security.IAesKey;
import io.greenscreens.quark.security.Security;

/**
 * Mediator between 2 libs - quark and security
 */
public enum QuarkSecurity {
;

	/**
	 * Initialize security engine
	 */
	public static void initialize() {
		SecurityProvider.get();
		Security.generateRSAKeys();
	}

	/**
	 * Initialize AES from 2x16char hex values.
	 * Value is split into KEY and IV nda used to creta a key.
	 * @param k
	 * @param webCryptoAPI
	 * @return
	 * @throws IOException
	 */
	public static IAesKey initAES(final String k, final boolean webCryptoAPI) throws IOException {
		return Security.initAES(k, webCryptoAPI);
	}

	/**
	 * Decode Quark Web API encrypted request
	 * @param d - encrypted data 
	 * @param k - encrypted AES key by RSA public key
	 * @param crypt
	 * @param webCryptoAPI - when HTTPS is used 
	 * @return
	 * @throws IOException
	 */
	public static String decodeRequest(final String d, final String k, final IAesKey crypt, final boolean webCryptoAPI) throws IOException {
		return Security.decodeRequest(d, k, crypt, webCryptoAPI);
	}

	/**
	 * Generate random bytes of a given size
	 * @param blockSize
	 * @return
	 */
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
