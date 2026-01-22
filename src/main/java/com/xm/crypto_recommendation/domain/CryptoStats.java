package com.xm.crypto_recommendation.domain;

public record CryptoStats(
        String symbol,
        CryptoPricePoint oldest,
        CryptoPricePoint newest,
        CryptoPricePoint min,
        CryptoPricePoint max
) {
}