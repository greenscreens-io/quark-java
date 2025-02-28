/*
 * Copyright (C) 2015, 2023 Green Screens Ltd.
 */
package io.greenscreens.quark.security.override;

import java.security.Provider;
import java.security.Security;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import jakarta.enterprise.inject.Vetoed;

/**
 * Helper class to initialize BoncyCastle encryption provider 
 * if not initialized already. 
 */
@Vetoed
public enum SecurityProvider {
	;

	public static final String PROVIDER_NAME = BouncyCastleProvider.PROVIDER_NAME;
	private static Provider provider;

	static {
		initialize();
	}

	public static void initialize() {
		if (Objects.nonNull(provider)) return;
		provider = getProvider();
	}
	
	private static Provider getProvider() {
		
		Provider provider = null;
		final List<Provider> providers = Arrays.asList(Security.getProviders());
		final Optional<Provider> optional = providers.stream()
				.filter(p -> p.getName().equals(PROVIDER_NAME))
				.findFirst();

		if (optional.isPresent()) {
			provider = optional.get();
			return provider;
		}
		
		provider = new BouncyCastleProvider();
		Security.removeProvider(PROVIDER_NAME);
		Security.insertProviderAt(provider, 1);
		
		return provider;
	}
	
	public static Provider get() {
		return provider;
	}
	
}
