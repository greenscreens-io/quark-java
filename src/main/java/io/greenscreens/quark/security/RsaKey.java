/*
 * Copyright (C) 2015, 2022 Green Screens Ltd.
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
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;

import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.greenscreens.quark.QuarkUtil;

/**
 * Helper class for handling RSA keys Support for Web Crypto API
 */
enum RsaKey {
	;

	private static final Logger LOG = LoggerFactory.getLogger(RsaKey.class);

	// 2048 not supported in legacy mode
	private static final int KEY_SIZE = 1024;

	private static KeyPair keyPairENCDEC = null;
	private static KeyPair keyPairVERSGN = null;
	private static String pemENCDEC = null;
	private static String pemVERSGN = null;
	private static String pemPrivENCDEC = null;

	private static final String WEB_MODE = "SHA384withECDSA";
	private static final String LEGACY_MODE = "SHA1withRSA";

	/**
	 * Initialize RSA key
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

		KeyPairGenerator gen = null;

		gen = KeyPairGenerator.getInstance("RSA", "BC");
		gen.initialize(KEY_SIZE);
		keyPairENCDEC = gen.generateKeyPair();

		initVerificator();

		pemENCDEC = RsaUtil.toPublicPem(keyPairENCDEC);
		pemPrivENCDEC = RsaUtil.toPrivatePem(keyPairENCDEC);
	}

	static void initVerificator()
			throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException, IOException {

		if (keyPairVERSGN == null) {
			final ECNamedCurveParameterSpec spec = ECNamedCurveTable.getParameterSpec("P-384");
			final KeyPairGenerator gen = KeyPairGenerator.getInstance("ECDSA", "BC");
			gen.initialize(spec);
			keyPairVERSGN = gen.generateKeyPair();
			pemVERSGN = RsaUtil.toPublicPem(keyPairVERSGN);
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
			pemENCDEC = RsaUtil.toPublicPem(keyPairENCDEC);
			pemPrivENCDEC = RsaUtil.toPrivatePem(keyPairENCDEC);
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
		if (flat) {
			return RsaUtil.flatten(pemENCDEC);
		}
		return pemENCDEC;
	}

	/**
	 * Get Private RSA key in PEM format
	 * 
	 * @param flat
	 * @return
	 */
	static String getPrivateEncoder(final boolean flat) {
		if (flat) {
			return RsaUtil.flatten(pemPrivENCDEC);
		}
		return pemPrivENCDEC;
	}

	/**
	 * Get Public RSA key in PEM format for Signing
	 * 
	 * @param flat
	 * @return
	 */
	static String getPublicVerifier(final boolean flat) {
		if (flat) {
			return RsaUtil.flatten(pemVERSGN);
		}
		return pemVERSGN;
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
		int len = Transcoder.getSignatureByteArrayLength(384);
		return Transcoder.transcodeSignatureToConcat(signedData, len);
	}

	/**
	 * Get signature instance based on support type
	 * 
	 * @param webCryptoAPI
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	static Signature getSignature(final boolean webCryptoAPI) throws NoSuchAlgorithmException {

		Signature signature = null;

		if (webCryptoAPI) {
			signature = Signature.getInstance(WEB_MODE);
		} else {
			signature = Signature.getInstance(LEGACY_MODE);
		}

		return signature;
	}

	/**
	 * Sign public key encrypt and public verify certificate with client challenge
	 * 
	 * @param challenge
	 * @param webCryptoAPI
	 * @return
	 * @throws NoSuchAlgorithmException 
	 * @throws SignatureException 
	 * @throws InvalidKeyException 
	 * @throws IOException 
	 * @throws Exception
	 */
	static byte[] signChallenge(final byte[] challenge, final boolean webCryptoAPI) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException, IOException {

		final Signature signature = getSignature(webCryptoAPI);
		signature.initSign(keyPairVERSGN.getPrivate());
		signature.update(challenge);

		if (webCryptoAPI) {
			signature.update(RsaUtil.flatten(pemENCDEC).getBytes());
			signature.update(RsaUtil.flatten(pemVERSGN).getBytes());
		} else {
			signature.update(pemENCDEC.getBytes());
			signature.update(pemVERSGN.getBytes());
		}

		final byte[] signedData = signature.sign();

		if (webCryptoAPI) {
			return signConvert(signedData);
		} else {
			return signedData;
		}
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
	 * @throws Exception
	 */
	static String signChallenge(final String data, final boolean isHex, final boolean webCryptoAPI) throws InvalidKeyException, NoSuchAlgorithmException, SignatureException, IOException {
		if (isHex) {
			return signHexChallenge(data, webCryptoAPI);
		} else {
			return signBase64Challenge(data, webCryptoAPI);
		}
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
	 * @throws Exception
	 */
	static String signHexChallenge(final String data, final boolean webCryptoAPI) throws InvalidKeyException, NoSuchAlgorithmException, SignatureException, IOException {
		final byte[] signature = signChallenge(data.getBytes(), webCryptoAPI);
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
	 * @throws Exception
	 */
	static String signBase64Challenge(final String data, final boolean webCryptoAPI) throws InvalidKeyException, NoSuchAlgorithmException, SignatureException, IOException  {
		final Encoder base64 = Base64.getEncoder();
		final byte[] signature = signChallenge(data.getBytes(), webCryptoAPI);
		return base64.encodeToString(signature);
	}

	/**
	 * Generic string sign
	 * 
	 * @param data
	 * @param webCryptoAPI
	 * @return
	 * @throws InvalidKeyException 
	 * @throws NoSuchAlgorithmException 
	 * @throws SignatureException 
	 * @throws IOException 
	 * @throws Exception
	 */
	static byte[] sign(final String data, final boolean webCryptoAPI) throws InvalidKeyException, NoSuchAlgorithmException, SignatureException, IOException  {

		final Signature signature = getSignature(webCryptoAPI);

		if (webCryptoAPI) {
			signature.initSign(keyPairVERSGN.getPrivate());
		} else {
			signature.initSign(keyPairENCDEC.getPrivate());
		}

		signature.update(data.getBytes());

		final byte[] signedData = signature.sign();

		if (webCryptoAPI) {
			return signConvert(signedData);
		} else {
			return signedData;
		}
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
	 * @throws Exception
	 */
	static String sign(final String data, final boolean isHex, final boolean webCryptoAPI) throws InvalidKeyException, NoSuchAlgorithmException, SignatureException, IOException {
		if (isHex) {
			return signHex(data, webCryptoAPI);
		} else {
			return signBase64(data, webCryptoAPI);
		}
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
	 * @throws Exception
	 */
	static String signHex(final String data, final boolean webCryptoAPI) throws InvalidKeyException, NoSuchAlgorithmException, SignatureException, IOException {
		final byte[] signature = sign(data, webCryptoAPI);
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
	 * @throws Exception
	 */
	static String signBase64(final String data, final boolean webCryptoAPI) throws InvalidKeyException, NoSuchAlgorithmException, SignatureException, IOException  {
		final Encoder base64 = Base64.getEncoder();
		final byte[] signature = sign(data, webCryptoAPI);
		return base64.encodeToString(signature);
	}

	/**
	 * Verify RSA signature on given data
	 * 
	 * @param data
	 * @param signature
	 * @param isHex
	 * @param webCryptoAPI
	 * @return
	 * @throws SignatureException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 * @throws Exception
	 */
	static boolean verify(final String data, final String signature, final boolean isHex, final boolean webCryptoAPI)
			throws InvalidKeyException, NoSuchAlgorithmException, SignatureException {
		if (isHex) {
			return verifyHex(data, signature, webCryptoAPI);
		} else {
			return verifyBase64(data, signature, webCryptoAPI);
		}
	}

	/**
	 * Verify RSA signature on given data in Hex String format
	 * 
	 * @param data
	 * @param signature
	 * @param webCryptoAPI
	 * @return
	 * @throws SignatureException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 * @throws Exception
	 */
	static boolean verifyHex(final String data, final String signature, final boolean webCryptoAPI)
			throws InvalidKeyException, NoSuchAlgorithmException, SignatureException {
		final byte[] dataBin = data.getBytes();
		final byte[] signatureBin = QuarkUtil.hexStringToByteArray(signature);
		return verify(dataBin, signatureBin, webCryptoAPI);
	}

	/**
	 * Verify RSA signature on given data in Base64 encoded format
	 * 
	 * @param data
	 * @param signature
	 * @param webCryptoAPI
	 * @return
	 * @throws SignatureException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 * @throws Exception
	 */
	static boolean verifyBase64(final String data, final String signature, final boolean webCryptoAPI)
			throws InvalidKeyException, NoSuchAlgorithmException, SignatureException {
		final Decoder base64 = Base64.getDecoder();
		final byte[] dataBin = data.getBytes();
		final byte[] signatureBin = base64.decode(signature);
		return verify(dataBin, signatureBin, webCryptoAPI);
	}

	/**
	 * Generic verify for bytes
	 * 
	 * @param data
	 * @param signature
	 * @param webCryptoAPI
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 * @throws SignatureException
	 * @throws Exception
	 */
	static boolean verify(byte[] data, byte[] signature, final boolean webCryptoAPI)
			throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {

		final Signature sig = getSignature(webCryptoAPI);

		if (webCryptoAPI) {
			sig.initVerify(keyPairVERSGN.getPublic());
		} else {
			sig.initVerify(keyPairENCDEC.getPublic());
		}

		sig.update(data);

		return sig.verify(signature);
	}

}
