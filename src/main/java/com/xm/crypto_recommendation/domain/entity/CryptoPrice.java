package com.xm.crypto_recommendation.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Represents a historical price point for a cryptocurrency.
 *
 * <p>
 * Each record stores the price of a crypto at a specific point in time.
 * Price data is immutable from a business perspective and is queried
 * extensively for statistical calculations.
 * </p>
 *
 * <p>
 * Database indexes are defined to optimize queries filtering by
 * cryptocurrency and ordering by timestamp or price.
 * </p>
 */
@Entity
@Table(
        name = "crypto_price",
        indexes = {
                @Index(name = "idx_crypto_timestamp", columnList = "crypto_id, timestamp"),
                @Index(name = "idx_crypto_price", columnList = "crypto_id, price")
        }
)
public class CryptoPrice {

    @Id
    @GeneratedValue
    private Long id;

    /**
     * Owning cryptocurrency.
     */
    @ManyToOne(optional = false)
    private Crypto crypto;

    /**
     * Timestamp of the price observation (UTC).
     */
    @Column(nullable = false)
    private Instant timestamp;

    /**
     * Price value at the given timestamp.
     */
    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal price;

    protected CryptoPrice() {
    }

    public CryptoPrice(Crypto crypto, Instant timestamp, BigDecimal price) {
        this.crypto = crypto;
        this.timestamp = timestamp;
        this.price = price;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Crypto getCrypto() {
        return crypto;
    }

    public void setCrypto(Crypto crypto) {
        this.crypto = crypto;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
