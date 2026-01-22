package com.xm.crypto_recommendation.service;

public class UnsupportedCryptoException extends RuntimeException {
    public UnsupportedCryptoException(String message) {
        super(message);
    }
}
