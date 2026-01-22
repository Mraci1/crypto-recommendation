package com.xm.crypto_recommendation.repository;

import com.xm.crypto_recommendation.domain.entity.Crypto;
import com.xm.crypto_recommendation.domain.entity.CryptoPrice;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

/**
 * Repository for accessing cryptocurrency price data.
 *
 * <p>
 * This repository exposes optimized queries for retrieving price data
 * ordered by value or timestamp within a given time range.
 * </p>
 *
 * <p>
 * All range-based queries accept a {@link Pageable} parameter to efficiently
 * limit result size at the database level (e.g. retrieving min/max values
 * without loading full result sets into memory).
 * </p>
 */
public interface CryptoPriceRepository extends JpaRepository<CryptoPrice, Long> {

    /**
     * Retrieves price entries ordered by ascending price within a time range.
     *
     * <p>
     * Typically used to obtain the minimum price in the given range by
     * requesting a single result via {@link Pageable}.
     * </p>
     */
    @Query("""
                SELECT cp
                FROM CryptoPrice cp
                WHERE cp.crypto = :crypto
                  AND cp.timestamp BETWEEN :from AND :to
                ORDER BY cp.price ASC, cp.timestamp ASC
            """)
    List<CryptoPrice> findPricesInRangeOrderedByPriceAsc(
            @Param("crypto") Crypto crypto,
            @Param("from") Instant from,
            @Param("to") Instant to,
            Pageable pageable
    );

    /**
     * Retrieves price entries ordered by descending price within a time range.
     *
     * <p>
     * Typically used to obtain the maximum price in the given range by
     * requesting a single result via {@link Pageable}.
     * </p>
     */
    @Query("""
                SELECT cp
                FROM CryptoPrice cp
                WHERE cp.crypto = :crypto
                  AND cp.timestamp BETWEEN :from AND :to
                ORDER BY cp.price DESC, cp.timestamp DESC
            """)
    List<CryptoPrice> findPricesInRangeOrderedByPriceDesc(
            @Param("crypto") Crypto crypto,
            @Param("from") Instant from,
            @Param("to") Instant to,
            Pageable pageable
    );

    /**
     * Retrieves price entries ordered by ascending timestamp within a time range.
     *
     * <p>
     * Typically used to obtain the oldest price entry in the given range.
     * </p>
     */
    @Query("""
                SELECT cp
                FROM CryptoPrice cp
                WHERE cp.crypto = :crypto
                  AND cp.timestamp BETWEEN :from AND :to
                ORDER BY cp.timestamp ASC
            """)
    List<CryptoPrice> findPricesInRangeOrderedByTimestampAsc(
            @Param("crypto") Crypto crypto,
            @Param("from") Instant from,
            @Param("to") Instant to,
            Pageable pageable
    );

    /**
     * Retrieves price entries ordered by descending timestamp within a time range.
     *
     * <p>
     * Typically used to obtain the newest price entry in the given range.
     * </p>
     */
    @Query("""
                SELECT cp
                FROM CryptoPrice cp
                WHERE cp.crypto = :crypto
                  AND cp.timestamp BETWEEN :from AND :to
                ORDER BY cp.timestamp DESC
            """)
    List<CryptoPrice> findPricesInRangeOrderedByTimestampDesc(
            @Param("crypto") Crypto crypto,
            @Param("from") Instant from,
            @Param("to") Instant to,
            Pageable pageable
    );

    /**
     * Retrieves the earliest available timestamp for the given cryptocurrency.
     *
     * @param crypto cryptocurrency entity
     * @return minimum timestamp present in the database
     */
    @Query("""
                SELECT MIN(cp.timestamp)
                FROM CryptoPrice cp
                WHERE cp.crypto = :crypto
            """)
    Instant findMinTimestamp(@Param("crypto") Crypto crypto);

    /**
     * Retrieves the latest available timestamp for the given cryptocurrency.
     *
     * @param crypto cryptocurrency entity
     * @return maximum timestamp present in the database
     */
    @Query("""
                SELECT MAX(cp.timestamp)
                FROM CryptoPrice cp
                WHERE cp.crypto = :crypto
            """)
    Instant findMaxTimestamp(@Param("crypto") Crypto crypto);

}
