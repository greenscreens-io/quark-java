/*
 * Copyright (C) 2015, 2023 Green Screens Ltd.
 */
package io.greenscreens.quark.security;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Base64;
import java.util.Collections;
import java.util.Objects;

import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.greenscreens.quark.QuarkUtil;


/**
 * Helper class for handling RSA keys Support for Web Crypto API
 */
enum AsyncKey {
	;

	private static final Logger LOG = LoggerFactory.getLogger(AsyncKey.class);

	private static KeyPair keyPairENCDEC = null;
	private static KeyPair keyPairVERSGN = null;
	private static String pemENCDEC = null;
	private static String pemVERSGN = null;
	private static String pemPrivENCDEC = null;

	private static final String WEB_MODE = "SHA384withECDSA";

	/**
	 * Initialize Async key
	 */
	static void initialize() {
		try {
			init();
		} catch (Exception e) {
			final String msg = QuarkUtil.toMessage(e);
			LOG.error(msg);
			LOG.debug(msg, e);
		}
	}

	/**
	 * Internal init
	 * 
	 * @throws NoSuchProviderException
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 * @throws InvalidAlgorithmParameterException
	 * @throws Exception
	 */
	static void init()
			throws NoSuchAlgorithmException, NoSuchProviderException, IOException, InvalidAlgorithmParameterException {

		final KeyPairGenerator gen = SharedSecret.getKeyPairGen();
		keyPairENCDEC = gen.generateKeyPair();

		initVerificator();

		pemENCDEC = AsyncKeyUtil.toPublicPem(keyPairENCDEC);
		pemPrivENCDEC = AsyncKeyUtil.toPrivatePem(keyPairENCDEC);
	}

	static void initVerificator()
			throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException, IOException {

		if (Objects.isNull(keyPairVERSGN)) {
			final ECNamedCurveParameterSpec spec = ECNamedCurveTable.getParameterSpec("P-384");
			final KeyPairGenerator gen = KeyPairGenerator.getInstance("ECDSA", SecurityProvider.PROVIDER_NAME);
			gen.initialize(spec);
			keyPairVERSGN = gen.generateKeyPair();
			pemVERSGN = AsyncKeyUtil.toPublicPem(keyPairVERSGN);
		}
	}

	/**
	 * Set new keys (support for dynamic web encryption)
	 * 
	 * @param pubKey
	 * @param privKey
	 */
	static void setKeys(final PublicKey pubKey, final PrivateKey privKey) {

		try {
			keyPairENCDEC = new KeyPair(pubKey, privKey);
			pemENCDEC = AsyncKeyUtil.toPublicPem(keyPairENCDEC);
			pemPrivENCDEC = AsyncKeyUtil.toPrivatePem(keyPairENCDEC);
			initVerificator();
		} catch (Exception e) {
			final String msg = QuarkUtil.toMessage(e);
			LOG.error(msg);
			LOG.debug(msg, e);
		}
	}

	/**
	 * Expose private key
	 * 
	 * @return
	 */
	static PrivateKey getPrivateKey() {
		return keyPairENCDEC.getPrivate();
	}

	/**
	 * Expose public key
	 * 
	 * @return
	 */
	static PublicKey getPublicKey() {
		return keyPairENCDEC.getPublic();
	}

	/**
	 * Get Public RSA key in PEM format
	 * 
	 * @param flat
	 * @return
	 */
	static String getPublicEncoder(final boolean flat) {
		return flat ? AsyncKeyUtil.flatten(pemENCDEC) : pemENCDEC;
	}

	/**
	 * Get Private RSA key in PEM format
	 * 
	 * @param flat
	 * @return
	 */
	static String getPrivateEncoder(final boolean flat) {
		return flat ? AsyncKeyUtil.flatten(pemPrivENCDEC) : pemPrivENCDEC;
	}

	/**
	 * Get Public RSA key in PEM format for Signing
	 * 
	 * @param flat
	 * @return
	 */
	static String getPublicVerifier(final boolean flat) {
		return flat ? AsyncKeyUtil.flatten(pemVERSGN) : pemVERSGN;
	}

	/**
	 * Convert signature to format for Web Crypto API
	 * 
	 * @param signedData
	 * @return
	 * @throws IOException
	 * @throws Exception
	 */
	static byte[] signConvert(byte[] signedData) throws IOException {
		final int len = Transcoder.getSignatureByteArrayLength(384);
		return Transcoder.transcodeSignatureToConcat(signedData, len);
	}

	/**
	 * Get signature instance based on support type
	 * 
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchProviderException 
	 * @throws InvalidAlgorithmParameterException 
	 */
	static Signature getSignature() throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {
		return Signature.getInstance(WEB_MODE, SecurityProvider.PROVIDER_NAME);
	}

	/**
	 * Sign public key encrypt and public verify certificate with client challenge
	 * 
	 * @param challenge
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws SignatureException
	 * @throws InvalidKeyException
	 * @throws IOException
	 * @throws NoSuchProviderException 
	 * @throws InvalidAlgorithmParameterException 
	 * @throws Exception
	 */
	static byte[] signChallenge(final byte[] challenge)
			throws NoSuchAlgorithmException, SignatureException, InvalidKeyException, IOException, NoSuchProviderException, InvalidAlgorithmParameterException {

		final Signature signature = getSignature();
		signature.initSign(keyPairVERSGN.getPrivate());
		signature.update(challenge);
		signature.update(AsyncKeyUtil.flatten(pemENCDEC).getBytes());
		signature.update(AsyncKeyUtil.flatten(pemVERSGN).getBytes());

		final byte[] signedData = signature.sign();
		return signConvert(signedData);
	}

	/**
	 * Generic text sign
	 * 
	 * @param data
	 * @param isHex
	 * @param flat
	 * @return
	 * @throws IOException
	 * @throws SignatureException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 * @throws NoSuchProviderException 
	 * @throws InvalidAlgorithmParameterException 
	 * @throws Exception
	 */
	static String signChallenge(final String data, final boolean isHex)
			throws InvalidKeyException, NoSuchAlgorithmException, SignatureException, IOException, NoSuchProviderException, InvalidAlgorithmParameterException {
		return isHex ? signHexChallenge(data) : signBase64Challenge(data);
	}

	/**
	 * Generic text sign with Hex String output
	 * 
	 * @param data
	 * @param flat
	 * @return
	 * @throws IOException
	 * @throws SignatureException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 * @throws NoSuchProviderException 
	 * @throws InvalidAlgorithmParameterException 
	 * @throws Exception
	 */
	static String signHexChallenge(final String data)
			throws InvalidKeyException, NoSuchAlgorithmException, SignatureException, IOException, NoSuchProviderException, InvalidAlgorithmParameterException {
		final byte[] signature = signChallenge(data.getBytes());
		return QuarkUtil.bytesToHex(signature);
	}

	/**
	 * Generic text sign with Base64 String output
	 * 
	 * @param data
	 * @param flat
	 * @return
	 * @throws IOException
	 * @throws SignatureException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 * @throws NoSuchProviderException 
	 * @throws InvalidAlgorithmParameterException 
	 * @throws Exception
	 */
	static String signBase64Challenge(final String data)
			throws InvalidKeyException, NoSuchAlgorithmException, SignatureException, IOException, NoSuchProviderException, InvalidAlgorithmParameterException {

		final byte[] signature = signChallenge(data.getBytes());
		return Base64.getEncoder().encodeToString(signature);
	}

	/**
	 * Generic string sign
	 * 
	 * @param data
	 * @return
	 * @throws InvalidKeyException
	 * @throws NoSuchAlgorithmException
	 * @throws SignatureException
	 * @throws IOException
	 * @throws NoSuchProviderException 
	 * @throws InvalidAlgorithmParameterException 
	 * @throws Exception
	 */
	static byte[] sign(final String data)
			throws InvalidKeyException, NoSuchAlgorithmException, SignatureException, IOException, NoSuchProviderException, InvalidAlgorithmParameterException {

		final Signature signature = getSignature();
		signature.initSign(keyPairVERSGN.getPrivate());
		signature.update(data.getBytes());
		final byte[] signedData = signature.sign();
		return signConvert(signedData);
	}

	/**
	 * Generic text sign
	 * 
	 * @param data
	 * @param isHex
	 * @param flat
	 * @return
	 * @throws IOException
	 * @throws SignatureException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 * @throws NoSuchProviderException 
	 * @throws InvalidAlgorithmParameterException 
	 * @throws Exception
	 */
	static String sign(final String data, final boolean isHex)
			throws InvalidKeyException, NoSuchAlgorithmException, SignatureException, IOException, NoSuchProviderException, InvalidAlgorithmParameterException {
		return isHex ? signHex(data) : signBase64(data);
	}

	/**
	 * Generic text sign with Hex String output
	 * 
	 * @param data
	 * @param flat
	 * @return
	 * @throws IOException
	 * @throws SignatureException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 * @throws NoSuchProviderException 
	 * @throws InvalidAlgorithmParameterException 
	 * @throws Exception
	 */
	static String signHex(final String data)
			throws InvalidKeyException, NoSuchAlgorithmException, SignatureException, IOException, NoSuchProviderException, InvalidAlgorithmParameterException {
		final byte[] signature = sign(data);
		return QuarkUtil.bytesToHex(signature);
	}

	/**
	 * Generic text sign with Base64 String output
	 * 
	 * @param data
	 * @param flat
	 * @return
	 * @throws IOException
	 * @throws SignatureException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 * @throws NoSuchProviderException 
	 * @throws InvalidAlgorithmParameterException 
	 * @throws Exception
	 */
	static String signBase64(final String data)
			throws InvalidKeyException, NoSuchAlgorithmException, SignatureException, IOException, NoSuchProviderException, InvalidAlgorithmParameterException {
		final byte[] signature = sign(data);
		return Base64.getEncoder().encodeToString(signature);
	}

	/**
	 * Verify RSA signature on given data
	 * 
	 * @param data
	 * @param signature
	 * @param isHex
	 * @return
	 * @throws SignatureException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 * @throws NoSuchProviderException 
	 * @throws InvalidAlgorithmParameterException 
	 * @throws Exception
	 */
	static boolean verify(final String data, final String signature, final boolean isHex)
			throws InvalidKeyException, NoSuchAlgorithmException, SignatureException, NoSuchProviderException, InvalidAlgorithmParameterException {
		return isHex ? verifyHex(data, signature) : verifyBase64(data, signature);
	}

	/**
	 * Verify RSA signature on given data in Hex String format
	 * 
	 * @param data
	 * @param signature
	 * @return
	 * @throws SignatureException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 * @throws NoSuchProviderException 
	 * @throws InvalidAlgorithmParameterException 
	 * @throws Exception
	 */
	static boolean verifyHex(final String data, final String signature)
			throws InvalidKeyException, NoSuchAlgorithmException, SignatureException, NoSuchProviderException, InvalidAlgorithmParameterException {
		final byte[] dataBin = data.getBytes();
		final byte[] signatureBin = QuarkUtil.hexStringToByteArray(signature);
		return verify(dataBin, signatureBin);
	}

	/**
	 * Verify RSA signature on given data in Base64 encoded format
	 * 
	 * @param data
	 * @param signature
	 * @return
	 * @throws SignatureException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 * @throws NoSuchProviderException 
	 * @throws InvalidAlgorithmParameterException 
	 * @throws Exception
	 */
	static boolean verifyBase64(final String data, final String signature)
			throws InvalidKeyException, NoSuchAlgorithmException, SignatureException, NoSuchProviderException, InvalidAlgorithmParameterException {
		final byte[] dataBin = data.getBytes();
		final byte[] signatureBin = Base64.getDecoder().decode(signature);
		return verify(dataBin, signatureBin);
	}

	/**
	 * Generic verify for bytes
	 * 
	 * @param data
	 * @param signature
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 * @throws SignatureException
	 * @throws NoSuchProviderException 
	 * @throws InvalidAlgorithmParameterException 
	 * @throws Exception
	 */
	static boolean verify(byte[] data, byte[] signature)
			throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, NoSuchProviderException, InvalidAlgorithmParameterException {

		final Signature sig = getSignature();
		sig.initVerify(keyPairVERSGN.getPublic());
		sig.update(data);

		return sig.verify(signature);
	}

	static final void pro() {
		Collections.emptyList().stream().filter(s -> s.equals("")).findFirst();
	}

}