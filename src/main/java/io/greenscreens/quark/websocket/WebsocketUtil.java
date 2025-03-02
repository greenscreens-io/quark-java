/*
 * Copyright (C) 2015, 2023 Green Screens Ltd.
 */
package io.greenscreens.quark.websocket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.node.ObjectNode;

import jakarta.websocket.DecodeException;
import jakarta.websocket.EncodeException;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.server.HandshakeRequest;
import io.greenscreens.quark.internal.QuarkConstants;
import io.greenscreens.quark.internal.QuarkDecoder;
import io.greenscreens.quark.security.IQuarkKey;
import io.greenscreens.quark.security.QuarkSecurity;
import io.greenscreens.quark.util.QuarkJson;
import io.greenscreens.quark.util.QuarkUtil;
import io.greenscreens.quark.web.QuarkCookieUtil;
import io.greenscreens.quark.websocket.data.IWebSocketResponse;
import io.greenscreens.quark.websocket.data.WebSocketInstruction;
import io.greenscreens.quark.websocket.data.WebSocketRequest;

/**
 * Internal encoder for WebSocket ExtJS response
 */
public enum WebsocketUtil {
    ;

    private static final Logger LOG = LoggerFactory.getLogger(WebsocketUtil.class); 
    
    final static IQuarkKey key(final EndpointConfig config) {
        return WebSocketStorage.get(config, QuarkConstants.ENCRYPT_ENGINE, null);
    }

    final static boolean isCompression(final EndpointConfig config) {
        return WebSocketStorage.get(config, QuarkConstants.QUARK_COMPRESSION, false);
    }

    final static WebSocketRequest decode(final ByteBuffer buffer) throws IOException {
        final String message = new String(buffer.array(), StandardCharsets.UTF_8);
        return decode(message);
    }

    final static WebSocketRequest decode(final String message) throws IOException {
        return QuarkJson.parse(WebSocketRequest.class, message);
    }
    
    static void decode(final WebSocketRequest request, final IQuarkKey crypt) throws DecodeException {
        try {
            QuarkDecoder.decode(request, crypt);
        } catch (IOException e) {
            final String msg = QuarkUtil.toMessage(e);
            LOG.error(msg);
            LOG.debug(msg, e);
            throw new DecodeException("", msg, e);
        }   
    }    

    /**
     * Encrypt message for websocket response
     * 
     * @param data
     * @return
     * @throws EncodeException
     */
    static String encode(final IWebSocketResponse data, final IQuarkKey key) throws EncodeException {

        String response = null;

        try {
            response = QuarkJson.stringify(data);
            response = encrypt(response, key);
        } catch (Exception e) {
            final String msg = QuarkUtil.toMessage(e);
            LOG.error(msg);
            LOG.debug(msg, e);
            throw new EncodeException(data, msg, e);
        }

        return QuarkUtil.normalize(response);
    }

    static boolean isJson(final String message) {

        boolean sts = false;

        if (QuarkUtil.nonEmpty(message)) {
            sts = message.trim().startsWith("{") && message.trim().endsWith("}");
        }

        return sts;
    }

    /**
     * Parse browser received cookie strings
     * 
     * @param cookies
     * @return
     */
    public static Map<String, String> parseCookies(final List<String> cookies) {
        return QuarkCookieUtil.parseCookies(cookies);
    }

    /**
     * Get request header from websocket
     * 
     * @param request
     * @param key
     * @return
     */
    public static Optional<String> findHeader(final HandshakeRequest request, final String key) {
        return firstList(request.getHeaders(), key);
    }

    /**
     * Generic method to find URL query parameter
     * 
     * @param request
     * @param name
     * @return
     */
    public static Optional<String> findQuery(final HandshakeRequest request, final String key) {
        return firstList(request.getParameterMap(), key);
    }

    static Optional<List<String>> parameterMap(final HandshakeRequest request, final String key) {
        return mapList(request.getParameterMap(), key);
    }

    static Optional<List<String>> mapList(final Map<String, List<String>> store, final String key) {
        return Optional.ofNullable(store).map(m -> m.get(key));
    }

    static Optional<String> firstList(final Map<String, List<String>> store, final String key) {
        return mapList(store, key).map(list -> firstList(list).orElse(null));
    }

    static Optional<String> firstList(final List<String> store) {
        return Optional.ofNullable(store).map(l -> l.stream()).map(l -> l.findFirst().orElse(null));
    }

    /**
     * Store current browser locale
     * 
     * Accept-Language:hr,en-US;q=0.8,en;q=0.6
     * 
     * @param request
     * @return
     */
    public static Locale getLocale(final HandshakeRequest request) {
        return WebsocketUtil.findHeader(request, "Accept-Language")
                .map(v -> v.split(";")[0])
                .map(v -> v.split(",")[0])
                .map(v -> new Locale(v))
                .orElse(Locale.ENGLISH);
    }

    /**
     * Encrypt data with AES for encrypted response
     * 
     * @param data
     * @param crypt
     * @return
     * @throws Exception
     */
    private static String encrypt(final String data, final IQuarkKey crypt) throws IOException {

        if (Objects.isNull(crypt) || !crypt.isValid()) return data;

        final byte[] iv = QuarkSecurity.getRandom(crypt.blockSize());
        final byte[] raw  = crypt.encrypt(data.getBytes(StandardCharsets.UTF_8), iv);
        final ObjectNode node = QuarkJson.node();
        node.put("iv", QuarkUtil.bytesToHex(iv));
        node.put("d", QuarkUtil.bytesToHex(raw));
        node.put("cmd", WebSocketInstruction.ENC.toString());
        return QuarkJson.stringify(node);
    }
    
}
