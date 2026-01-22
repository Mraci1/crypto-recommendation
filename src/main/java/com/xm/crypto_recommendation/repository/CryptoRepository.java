package com.xm.crypto_recommendation.repository;

import com.xm.crypto_recommendation.domain.entity.Crypto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository for accessing cryptocurrency metadata.
 *
 * <p>
 * Provides lookup operations for supported cryptocurrencies based on
 * their unique symbol.
 * </p>
 */
public interface CryptoRepository extends JpaRepository<Crypto, Long> {
    /**
     * Finds a cryptocurrency by its symbol.
     *
     * @param symbol unique cryptocurrency symbol (e.g. BTC, ETH)
     * @return optional crypto entity if found
     */
    Optional<Crypto> findBySymbol(String symbol);
}
