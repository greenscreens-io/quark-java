/*
 * Copyright (C) 2015, 2023 Green Screens Ltd.
 */
package io.greenscreens.quark.security;

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
		return Security.initWebKey(publicKey);
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
