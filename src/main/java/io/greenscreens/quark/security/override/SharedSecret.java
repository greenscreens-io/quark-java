/*
 * Copyright (C) 2015, 2023 Green Screens Ltd.
 */
package io.greenscreens.quark.security.override;

import java.math.BigInteger;
import java.security.PublicKey;
import java.security.PrivateKey;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.spec.ECGenParameterSpec;
import java.util.Base64;
import java.util.Base64.Decoder;

import javax.crypto.KeyAgreement;
import org.bouncycastle.jce.interfaces.ECPublicKey;
import org.bouncycastle.jce.interfaces.ECPrivateKey;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.jce.spec.ECPublicKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.greenscreens.quark.util.QuarkUtil;

import org.bouncycastle.jce.spec.ECPrivateKeySpec;

/**
 * ECDH key exhange engine
 */
enum SharedSecret {
;
	private static final Logger LOG = LoggerFactory.getLogger(SharedSecret.class);
	final protected static String ALGO = "ECDH"; 
	final protected static String CIPHER = "P-256"; // "prime256v1";
	
	static KeyFactory getKeyFactory() throws NoSuchAlgorithmException, NoSuchProviderException{
		return KeyFactory.getInstance(ALGO, SecurityProvider.PROVIDER_NAME);
	}
	
	static KeyPairGenerator getKeyPairGen() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException{
		final KeyPairGenerator kpgen = KeyPairGenerator.getInstance(ALGO, SecurityProvider.PROVIDER_NAME);
		kpgen.initialize(new ECGenParameterSpec(CIPHER), new SecureRandom());
		return kpgen;
	}
	
	public static byte[] fromPublicKey(final PublicKey key) throws Exception {
		final ECPublicKey eckey = (ECPublicKey) key;
		return eckey.getQ().getEncoded(true);
	}

	public static PublicKey toPublicKey(final byte[] data) throws Exception {
		final ECParameterSpec params = ECNamedCurveTable.getParameterSpec(CIPHER);
		final ECPublicKeySpec pubKey = new ECPublicKeySpec(params.getCurve().decodePoint(data), params);
		final KeyFactory kf = getKeyFactory();
		return kf.generatePublic(pubKey);
	}

	public static byte[] fromPrivateKey(final PrivateKey key) throws Exception {
		final ECPrivateKey eckey = (ECPrivateKey) key;
		return eckey.getD().toByteArray();
	}

	public static PrivateKey toPrivateKey(final byte[] data) throws Exception {
		final ECParameterSpec params = ECNamedCurveTable.getParameterSpec(CIPHER);
		final ECPrivateKeySpec prvkey = new ECPrivateKeySpec(new BigInteger(data), params);
		final KeyFactory kf = getKeyFactory();
		return kf.generatePrivate(prvkey);
	}

	public static byte[] doECDH(final byte[] dataPrv, final byte[] dataPub) throws Exception {
		return doECDH(toPrivateKey(dataPrv), toPublicKey(dataPub));
	}
	
	public static byte[] doECDH(final PrivateKey privateKey, final PublicKey publicKey) throws Exception {
		final KeyAgreement ka = KeyAgreement.getInstance(ALGO, SecurityProvider.PROVIDER_NAME);
		ka.init(privateKey);
		ka.doPhase(publicKey, true);
		return ka.generateSecret();
	}

	/**
	 * Generate shared secret
	 * 
	 * @param data ECDH public key from browser
	 * @param key ECDH server private key
	 * @return 32 byte (256bit) master key used for AES
	 */
	public static byte[] generate(final String data, final PrivateKey key, final boolean isHex) {
		if (isHex) {
			return generateHex(data, key);
		} else {
			return generateBase64(data, key);
		}
	}

	/**
	 * Decode from base64 string
	 * 
	 * @param data
	 * @param key
	 * @return
	 */
	private static byte[] generateBase64(final String data, final PrivateKey key) {
		final Decoder base64 = Base64.getDecoder();
		byte[] bin = base64.decode(data);
		return generate(bin, key);
	}

	/**
	 * Decode from base64 string
	 * 
	 * @param data
	 * @param key
	 * @return
	 */
	private static byte[] generateHex(final String data, final PrivateKey key) {
		byte[] bin = QuarkUtil.fromHexAsBytes(data);
		return generate(bin, key);
	}
	
	/**
	 * Decrypt data with private key and given mode
	 * 
	 * @param buffer
	 * @param key
	 * @param mode
	 * @return
	 */
	private static byte[] generate(final byte[] buffer, final PrivateKey key) {

		byte[] data = null;

		try {
			final PublicKey pk = SharedSecret.toPublicKey(buffer);
			data = SharedSecret.doECDH(key, pk);
		} catch (Exception e) {
			final String msg = QuarkUtil.toMessage(e);
			LOG.error(msg);
			LOG.debug(msg, e);
			data = new byte[0];
		}

		return data;
	}

}