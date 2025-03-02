/*
 * Copyright (C) 2015, 2024 Green Screens Ltd.
 */
package io.greenscreens.quark.security;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Objects;

import io.greenscreens.quark.security.override.IAesKey;

final class QuarkKey implements IQuarkKey {

    final private IAesKey key;
        
    QuarkKey(final IAesKey key) {
        super();
        this.key = key;
    }

    IAesKey unwrap() {
        return key;
    }

    @Override
    public int blockSize() {
        return key.getBlockSize();
    }

    @Override
    public boolean isValid() {
        return Objects.nonNull(key) && key.isValid();
    }

    @Override
    public byte[] encrypt(final byte[] data, final byte[] iv) throws IOException {
        return key.encrypt(data, iv);
    }

    @Override
    public byte[] decrypt(final byte[] data, final byte[] iv) throws IOException {
        return key.decrypt(data, iv);
    }

    @Override
    public ByteBuffer encrypt(final ByteBuffer data, final ByteBuffer iv) throws IOException {
        return key.encrypt(data, iv);
    }

    @Override
    public ByteBuffer decrypt(final ByteBuffer data, final ByteBuffer iv) throws IOException {
        return key.decrypt(data, iv);
    }

    public static IQuarkKey create(final IAesKey key){
        return new QuarkKey(key);
    }


}
