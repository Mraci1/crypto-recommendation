package com.xm.crypto_recommendation.domain.dto;

import java.math.BigDecimal;

/**
 * DTO representing the normalized range of a cryptocurrency.
 *
 * @param symbol          the symbol of the cryptocurrency
 * @param normalizedRange the normalized range value
 */
public record CryptoNormalizedRange(
        String symbol,
        BigDecimal normalizedRange
) {
}