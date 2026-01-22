package com.xm.crypto_recommendation.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Represents a supported cryptocurrency.
 *
 * <p>
 * Each crypto is identified by its symbol (e.g. BTC, ETH), which is
 * expected to be unique within the system.
 * </p>
 */
@Entity
@Table
public class Crypto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Cryptocurrency symbol (e.g. BTC, ETH).
     */
    @Column(nullable = false)
    private String symbol;

    protected Crypto() {
    }

    public Crypto(String symbol) {
        this.symbol = symbol;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
}
