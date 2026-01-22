package com.xm.crypto_recommendation.domain.dto;

import java.math.BigDecimal;

public record CryptoNormalizedRange(
        String symbol,
        BigDecimal normalizedRange
) {
}