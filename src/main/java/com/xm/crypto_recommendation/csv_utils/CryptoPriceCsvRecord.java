package com.xm.crypto_recommendation.csv_utils;

import java.math.BigDecimal;

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
