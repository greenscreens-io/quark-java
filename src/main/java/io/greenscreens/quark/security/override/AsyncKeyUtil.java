/*
 * Copyright (C) 2015, 2023. Green Screens Ltd.
 */
package io.greenscreens.quark.security.override;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;
import java.util.regex.Pattern;

import javax.crypto.spec.IvParameterSpec;

import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;

import io.greenscreens.quark.util.override.ByteUtil;

/**
 * RSA utility to work with public and private keys
 */
enum AsyncKeyUtil {
	;

	private static final String PUBLIC = "PUBLIC KEY";
	private static final String PRIVATE = "PRIVATE KEY";

	private static final Pattern REGEX = Pattern.compile("(-{5}[A-Z ]*-{5})");
	private static final Pattern REGNL = Pattern.compile("\\s{2,}");

	static KeyFactory getKeyFactory() throws NoSuchAlgorithmException, NoSuchProviderException {
		return SharedSecret.getKeyFactory();
	}

	/**
	 * Convert PEM multi line to single line
	 * 
	 * @param val
	 * @return
	 */
	static final String flatten(final String val) {
		if (val != null) {
			final String tmp = REGEX.matcher(val).replaceAll("");
			return REGNL.matcher(tmp).replaceAll("");
		} else {
			return val;
		}
	}

	/**
	 * Convert PEM file to decoded byte array Removes headers and new lines then
	 * decode from Base64
	 * 
	 * @param raw
	 * @return
	 */
	static byte[] convertFromPEM(final byte[] raw) {
		String data = new String(raw);
		final String[] lines = data.split("\\r?\\n");
		if (lines.length > 1) {
			lines[0] = "";
			lines[lines.length - 1] = "";
			data = String.join("", lines);
		}
		return Base64.getMimeDecoder().decode(data);
	}

	static PrivateKey getPrivateKey(final String key) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException {
		final byte[] raw = convertFromPEM(key.getBytes());
		final KeyFactory fact = getKeyFactory();
		final PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(raw);
		final PrivateKey priv = fact.generatePrivate(keySpec);
		Arrays.fill(raw, (byte) 0);
		return priv;
	}

	static PublicKey getPublicKey(final String key) throws InvalidKeySpecException, NoSuchAlgorithmException, NoSuchProviderException {
		final byte[] raw = convertFromPEM(key.getBytes());
		final X509EncodedKeySpec spec = new X509EncodedKeySpec(raw);
		final KeyFactory kf = getKeyFactory();
		return kf.generatePublic(spec);
	}

	static String getPem(final Key key, final String name) throws IOException {
		final StringWriter writer = new StringWriter();
		final PemWriter pemWriter = new PemWriter(writer);
		pemWriter.writeObject(new PemObject(name, key.getEncoded()));
		pemWriter.flush();
		pemWriter.close();
		return writer.toString();
	}

	static String toPublicPem(final Key key) throws IOException {
		return getPem(key, PUBLIC);
	}

	static String toPrivatePem(final Key key) throws IOException {
		return getPem(key, PRIVATE);
	}

	static String toPublicPem(final KeyPair key) throws IOException {
		return toPublicPem(key.getPublic());
	}

	static String toPrivatePem(final KeyPair key) throws IOException {
		return toPrivatePem(key.getPrivate());
	}

    public static IvParameterSpec toVector(final ByteBuffer iv, final IvParameterSpec def) {
        return toVector(ByteUtil.toBytes(iv), def);
    }

    public static IvParameterSpec toVector(final byte[] iv, final IvParameterSpec def) {
        return Objects.isNull(iv) ? def : new IvParameterSpec(iv);
    }
	
}
