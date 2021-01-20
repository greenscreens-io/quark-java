/*
 * Copyright (C) 2015, 2016  Green Screens Ltd.
 */
package io.greenscreens.quark;

import java.security.Provider;
import java.security.Security;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public enum SecurityProvider {
	;

	private static final Provider provider;

	static {
		provider = getProvider();
	}

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
	
	public static Provider get() {
		return provider;
	}
	
}
