package com.xm.crypto_recommendation.repository;

import com.xm.crypto_recommendation.domain.Crypto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CryptoRepository extends JpaRepository<Crypto, Long> {
    Optional<Crypto> findBySymbol(String symbol);
}
