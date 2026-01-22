package com.xm.crypto_recommendation.domain.dto;

/**
 * DTO representing statistical data for a cryptocurrency.
 *
 * @param symbol The symbol of the cryptocurrency (e.g., BTC, ETH).
 * @param oldest The oldest price point recorded.
 * @param newest The most recent price point recorded.
 * @param min    The minimum price point recorded.
 * @param max    The maximum price point recorded.
 */
public record CryptoStats(
        String symbol,
        CryptoPricePoint oldest,
        CryptoPricePoint newest,
        CryptoPricePoint min,
        CryptoPricePoint max
) {
}