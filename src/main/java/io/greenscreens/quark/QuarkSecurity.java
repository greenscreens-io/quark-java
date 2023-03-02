/*
 * Copyright (C) 2015, 2022 Green Screens Ltd.
 */
package io.greenscreens.quark;

import java.io.IOException;

import io.greenscreens.quark.security.IAesKey;
import io.greenscreens.quark.security.Security;
import io.greenscreens.quark.security.SecurityProvider;

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
		Security.generateAsyncKeys();
	}

	/**
	 * Initialize AES from 2x16char hex values.
	 * Value is split into KEY and IV and used to create a key.
	 * @param k
	 * @return
	 * @throws IOException
	 */
	public static IAesKey initAES(final String k) throws IOException {
		return Security.initAES(k);
	}

	/**
	 * Decode Quark Web API encrypted request
	 * @param d - encrypted data 
	 * @param k - encrypted AES key by RSA public key
	 * @param crypt
	 * @return
	 * @throws IOException
	 */
	public static String decodeRequest(final String d, final String k, final IAesKey crypt) throws IOException {
		return Security.decryptRequest(d, k, crypt);
	}

	/**
	 * Generate random bytes of a given size
	 * @param blockSize
	 * @return
	 */
	public static final byte[] getRandom(final int blockSize) {
		return Security.getRandom(blockSize);
	}
	
	public static String getPublic() {
		return Security.getPublicKey();
	}

	public static String getVerifier() {
		return Security.getVerifier();
	}
	
	/**
	 * Create digital signature for challenge 
	 * @param challenge
	 * @return
	 */
	public static String signApiKey(final String challenge) {
		final String keyEnc = Security.getPublicKey();
		final String keyVer = Security.getVerifier();
		final String data = String.format("%s%s%s", challenge, keyEnc, keyVer);
		return Security.sign(data, false);
	}

}
