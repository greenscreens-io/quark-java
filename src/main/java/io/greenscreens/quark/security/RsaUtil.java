/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 * 
 * https://www.greenscreens.io
 * 
 */
package io.greenscreens.quark.security;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAKeyGenParameterSpec;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.Enumeration;
import java.util.regex.Pattern;

import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;

/**
 * RSA utility to work with public and private keys
 */
enum RsaUtil {
	;

	private static final int KEY_SIZE = 1024;
	private static final String PUBLIC = "PUBLIC KEY";
	private static final String PRIVATE = "PRIVATE KEY";

	private static final Pattern REGEX = Pattern.compile("(-{5}[A-Z ]*-{5})");
	private static final Pattern REGNL = Pattern.compile("\\s{2,}");

	static KeyFactory getKeyFactory() throws NoSuchAlgorithmException {
		return KeyFactory.getInstance("RSA");
	}

	static KeyPairGenerator getKeyPairGenerator() throws NoSuchAlgorithmException {
		return KeyPairGenerator.getInstance("RSA");
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
		return Base64.getDecoder().decode(data);
	}

	static PrivateKey getPrivateKey(final String key) throws NoSuchAlgorithmException, InvalidKeySpecException {

		final byte[] raw = convertFromPEM(key.getBytes());
		KeyFactory fact = getKeyFactory();
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(raw);
		PrivateKey priv = fact.generatePrivate(keySpec);
		Arrays.fill(raw, (byte) 0);
		return priv;
	}

	static PrivateKey getPrivateKey2(final String key) throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {
		final byte[] raw = convertFromPEM(key.getBytes());
		final ASN1Sequence primitive = (ASN1Sequence) ASN1Primitive.fromByteArray(raw);
		final Enumeration<?> e = primitive.getObjects();
		final BigInteger v = ((ASN1Integer) e.nextElement()).getValue();

		final int version = v.intValue();
		if (version != 0 && version != 1) {
			throw new IllegalArgumentException("wrong version for RSA private key");
		}

		/**
		 * In fact only modulus and private exponent are in use.
		 */
		final BigInteger modulus = ((ASN1Integer) e.nextElement()).getValue();

		// final BigInteger publicExponent = ((ASN1Integer) e.nextElement()).getValue();
		final BigInteger privateExponent = ((ASN1Integer) e.nextElement()).getValue();
		/*
		 * BigInteger prime1 = ((ASN1Integer) e.nextElement()).getValue(); BigInteger
		 * prime2 = ((ASN1Integer) e.nextElement()).getValue(); BigInteger exponent1 =
		 * ((ASN1Integer) e.nextElement()).getValue(); BigInteger exponent2 =
		 * ((ASN1Integer) e.nextElement()).getValue(); BigInteger coefficient =
		 * ((ASN1Integer) e.nextElement()).getValue();
		 */

		final RSAPrivateKeySpec keySpec = new RSAPrivateKeySpec(modulus, privateExponent);
		final KeyFactory kf = getKeyFactory();
		return kf.generatePrivate(keySpec);
	}

	static PublicKey getPublicKey(final String key) throws InvalidKeySpecException, NoSuchAlgorithmException {
		final byte[] raw = convertFromPEM(key.getBytes());
		final X509EncodedKeySpec spec = new X509EncodedKeySpec(raw);
		final KeyFactory kf = getKeyFactory();
		return kf.generatePublic(spec);
	}

	static KeyPair generateRSAKeyPair() throws  NoSuchAlgorithmException, InvalidAlgorithmParameterException {
		final SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
		final RSAKeyGenParameterSpec spec = new RSAKeyGenParameterSpec(KEY_SIZE, RSAKeyGenParameterSpec.F4);
		final KeyPairGenerator generator = getKeyPairGenerator();
		generator.initialize(spec, random);
		return generator.generateKeyPair();
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

}
