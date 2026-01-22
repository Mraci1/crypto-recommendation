package com.xm.crypto_recommendation.repository;

import com.xm.crypto_recommendation.domain.entity.Crypto;
import com.xm.crypto_recommendation.domain.entity.CryptoPrice;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface CryptoPriceRepository extends JpaRepository<CryptoPrice, Long> {

    List<CryptoPrice> findByCrypto(Crypto crypto);

    @Query("""
                SELECT cp
                FROM CryptoPrice cp
                WHERE cp.crypto = :crypto
                  AND cp.timestamp BETWEEN :from AND :to
                ORDER BY cp.price ASC, cp.timestamp ASC
            """)
    List<CryptoPrice> findMinPrice(
            @Param("crypto") Crypto crypto,
            @Param("from") Instant from,
            @Param("to") Instant to,
            Pageable pageable
    );

    @Query("""
                SELECT cp
                FROM CryptoPrice cp
                WHERE cp.crypto = :crypto
                  AND cp.timestamp BETWEEN :from AND :to
                ORDER BY cp.price DESC, cp.timestamp DESC
            """)
    List<CryptoPrice> findMaxPrice(
            @Param("crypto") Crypto crypto,
            @Param("from") Instant from,
            @Param("to") Instant to,
            Pageable pageable
    );

    @Query("""
                SELECT cp
                FROM CryptoPrice cp
                WHERE cp.crypto = :crypto
                  AND cp.timestamp BETWEEN :from AND :to
                ORDER BY cp.timestamp ASC
            """)
    List<CryptoPrice> findOldestPrice(
            @Param("crypto") Crypto crypto,
            @Param("from") Instant from,
            @Param("to") Instant to,
            Pageable pageable
    );

    @Query("""
                SELECT cp
                FROM CryptoPrice cp
                WHERE cp.crypto = :crypto
                  AND cp.timestamp BETWEEN :from AND :to
                ORDER BY cp.timestamp DESC
            """)
    List<CryptoPrice> findNewestPrice(
            @Param("crypto") Crypto crypto,
            @Param("from") Instant from,
            @Param("to") Instant to,
            Pageable pageable
    );

    @Query("""
                SELECT MIN(cp.timestamp)
                FROM CryptoPrice cp
                WHERE cp.crypto = :crypto
            """)
    Instant findMinTimestamp(@Param("crypto") Crypto crypto);

    @Query("""
                SELECT MAX(cp.timestamp)
                FROM CryptoPrice cp
                WHERE cp.crypto = :crypto
            """)
    Instant findMaxTimestamp(@Param("crypto") Crypto crypto);

}
