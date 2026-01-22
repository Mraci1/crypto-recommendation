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

    @ManyToOne(optional = false)
    private Crypto crypto;

    @Column(nullable = false)
    private Instant timestamp;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal price;

    public CryptoPrice() {
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
