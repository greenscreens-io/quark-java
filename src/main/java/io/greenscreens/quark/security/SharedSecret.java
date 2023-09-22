package io.greenscreens.quark.security;

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
import java.util.Collections;
import java.util.Base64.Decoder;

import javax.crypto.KeyAgreement;
import org.bouncycastle.jce.interfaces.ECPublicKey;
import org.bouncycastle.jce.interfaces.ECPrivateKey;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.jce.spec.ECPublicKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.greenscreens.quark.QuarkUtil;

import org.bouncycastle.jce.spec.ECPrivateKeySpec;

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
	
	/**
	 * SPKI (PEM encoded) always start with 0x30;
	 * RAW start with 00, 02, 03, 04
	 * @param data
	 * @return
	 */
    private static boolean isSPKI(final byte[] data) {
    	return ByteUtil.nonEmpty(data, 66) && data[0] == (byte)0x30;
    }
	
	/**
	 * Load SPKI key format
	 * @param key
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 * @throws NoSuchProviderException
	 */
	private static PublicKey importPublicKeySPKI(final byte[] key) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException {
    	final EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(key);
        final KeyFactory kf = KeyFactory.getInstance(ALGO, SecurityProvider.PROVIDER_NAME);
        final PublicKey pub = kf.generatePublic(publicKeySpec);
        return pub;
    }

    /**
     * Load SPKI key format
     * @param key
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     * @throws NoSuchProviderException
     */
    private static PrivateKey importPrivateKeySPKI(final byte[] key) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException {
    	final PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(key);
        final KeyFactory kf = KeyFactory.getInstance(ALGO, SecurityProvider.PROVIDER_NAME);
        final PrivateKey privateKey = kf.generatePrivate(privateKeySpec);
        return privateKey;
    }
    
	/**
	 * Load RAW key format
	 * @param data
	 * @return
	 * @throws Exception
	 */
    private static PublicKey importPublicKeyRAW(final byte[] data) throws Exception {
		final ECParameterSpec params = ECNamedCurveTable.getParameterSpec(CIPHER);
		final ECPublicKeySpec pubKey = new ECPublicKeySpec(params.getCurve().decodePoint(data), params);
		final KeyFactory kf = getKeyFactory();
		return kf.generatePublic(pubKey);
	}

	/**
	 * Load RAW key format
	 * @param data
	 * @return
	 * @throws Exception
	 */
	private static PrivateKey importPrivateKeyRAW(final byte[] data) throws Exception {
		final ECParameterSpec params = ECNamedCurveTable.getParameterSpec(CIPHER);
		final ECPrivateKeySpec prvkey = new ECPrivateKeySpec(new BigInteger(data), params);
		final KeyFactory kf = getKeyFactory();
		return kf.generatePrivate(prvkey);
	}
    
	/**
	 * Load RAW key format
	 * @param data
	 * @return
	 * @throws Exception
	 */
	public static PublicKey importPublicKey(final byte[] data) throws Exception {
		return isSPKI(data) ? importPublicKeySPKI(data) : importPublicKeyRAW(data);
	}

	/**
	 * Load RAW key format
	 * @param data
	 * @return
	 * @throws Exception
	 */
	public static PrivateKey importPrivateKey(final byte[] data) throws Exception {
		return isSPKI(data) ? importPrivateKeySPKI(data) : importPrivateKeyRAW(data);
	}
    
    /**
     * Export key in RAW format
     * @param key
     * @return
     * @throws Exception
     */
	public static byte[] exportPublicKey(final PublicKey key) throws Exception {
		final ECPublicKey eckey = (ECPublicKey) key;
		return eckey.getQ().getEncoded(true);
	}

	/**
	 * Export key in RAW format
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public static byte[] exportPrivateKey(final PrivateKey key) throws Exception {
		final ECPrivateKey eckey = (ECPrivateKey) key;
		return eckey.getD().toByteArray();
	}

	public static byte[] doECDH(final byte[] dataPrv, final byte[] dataPub) throws Exception {
		return doECDH(importPrivateKey(dataPrv), importPublicKey(dataPub));
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
		byte[] bin = QuarkUtil.hexStringToByteArray(data);
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
			final PublicKey pk = SharedSecret.importPublicKey(buffer);
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