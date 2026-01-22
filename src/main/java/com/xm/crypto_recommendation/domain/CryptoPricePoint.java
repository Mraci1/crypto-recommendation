package com.xm.crypto_recommendation.domain;

import java.math.BigDecimal;
import java.time.Instant;

public record CryptoPricePoint(
        BigDecimal price,
        Instant timestamp
) {
}
