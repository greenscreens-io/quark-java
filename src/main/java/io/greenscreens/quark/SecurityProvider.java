/*
 * Copyright (C) 2015, 2022 Green Screens Ltd.
 */
package io.greenscreens.quark;

import java.security.Provider;
import java.security.Security;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 * Helper class to initialize BoncyCastle encryption provider 
 * if not initialized already. 
 */
public enum SecurityProvider {
	;

	private static final Provider provider;

	static {
		provider = getProvider();
	}

	/**
	 * Check if provider already registered.
	 * If not , register then return.
	 * @return
	 */
	private static Provider getProvider() {
		
		final Provider [] providers = Security.getProviders();
		if (providers.length > 0) {
			if (providers[0].getName().equals(BouncyCastleProvider.PROVIDER_NAME)) {
				return provider;
			}
		}
		
		Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
		final Provider provider = new BouncyCastleProvider();
		Security.insertProviderAt(provider, 1);

		return provider;
	}

	/**
	 * Retrieve security provider - BounvyCastle
	 * @return
	 */
	public static Provider get() {
		return provider;
	}
	
}
