package com.xm.crypto_recommendation.ingestion;

import java.math.BigDecimal;

/**
 * Represents a single row in a cryptocurrency price CSV file.
 *
 * <p>
 * This class is used exclusively during CSV ingestion and is mapped directly
 * from CSV rows using Jackson's CSV module.
 * </p>
 *
 * <p>
 * Expected CSV format:
 * <ul>
 *     <li>{@code timestamp} – epoch time in milliseconds</li>
 *     <li>{@code symbol} – cryptocurrency symbol (e.g. BTC)</li>
 *     <li>{@code price} – price value at the given timestamp</li>
 * </ul>
 * </p>
 */
public class CryptoPriceCsvRecord {

    private long timestamp;
    private String symbol;
    private BigDecimal price;

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
