package com.xm.crypto_recommendation.domain.dto;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * DTO representing a cryptocurrency price point at a specific timestamp.
 *
 * @param price     The price of the cryptocurrency.
 * @param timestamp The timestamp when the price was recorded.
 */
public record CryptoPricePoint(
        BigDecimal price,
        Instant timestamp
) {
}
