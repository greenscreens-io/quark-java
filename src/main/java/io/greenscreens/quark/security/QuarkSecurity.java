/*
 * Copyright (C) 2015, 2023. Green Screens Ltd.
 */
package io.greenscreens.quark.security;

import java.io.IOException;

import io.greenscreens.quark.security.override.Security;
import io.greenscreens.quark.security.override.SecurityProvider;

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

	public static IQuarkKey initWebKey(final String publicKey) {
		return QuarkKey.create(Security.initWebKey(publicKey));
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

    /**
     * Decode Quark Web API encrypted request
     * @param d - encrypted data 
     * @param k - encrypted AES key by RSA public key
     * @param crypt
     * @param webCryptoAPI - when HTTPS is used 
     * @return
     * @throws IOException
     */
    public static String decryptRequest(final String d, final String k, final IQuarkKey crypt) throws IOException {
        return Security.decrypt(d, k, ((QuarkKey)crypt).unwrap());
    }	
}
