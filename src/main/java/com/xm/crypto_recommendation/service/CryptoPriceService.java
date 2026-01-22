package com.xm.crypto_recommendation.service;

import com.xm.crypto_recommendation.domain.dto.CryptoNormalizedRange;
import com.xm.crypto_recommendation.domain.dto.CryptoPricePoint;
import com.xm.crypto_recommendation.domain.dto.CryptoStats;
import com.xm.crypto_recommendation.domain.entity.Crypto;
import com.xm.crypto_recommendation.domain.entity.CryptoPrice;
import com.xm.crypto_recommendation.exception.NoDataException;
import com.xm.crypto_recommendation.exception.UnsupportedCryptoException;
import com.xm.crypto_recommendation.repository.CryptoPriceRepository;
import com.xm.crypto_recommendation.repository.CryptoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Service responsible for calculating cryptocurrency statistics and
 * investment-related metrics based on historical price data.
 */
@Service
public class CryptoPriceService {

    /**
     * Pageable used to efficiently fetch only a single record
     * (e.g. min/max/oldest/newest) from the database.
     */
    private static final Pageable LIMIT_ONE = PageRequest.of(0, 1);

    private final CryptoRepository cryptoRepository;
    private final CryptoPriceRepository cryptoPriceRepository;

    @Autowired
    public CryptoPriceService(CryptoRepository cryptoRepository, CryptoPriceRepository cryptoPriceRepository) {
        this.cryptoRepository = cryptoRepository;
        this.cryptoPriceRepository = cryptoPriceRepository;
    }

    /**
     * Returns aggregated price statistics (oldest, newest, minimum, maximum)
     * for a given cryptocurrency within an optional date range.
     *
     * <p>
     * If {@code from} or {@code to} are not provided, the earliest or latest
     * available timestamps for the crypto are used respectively.
     * </p>
     *
     * @param cryptoSymbol crypto symbol (case-insensitive)
     * @param from         optional start date (inclusive)
     * @param to           optional end date (inclusive)
     * @return aggregated crypto statistics
     *
     * @throws UnsupportedCryptoException if the crypto symbol is not supported
     * @throws NoDataException            if no price data exists in the given range
     * @throws IllegalArgumentException   if the resolved date range is invalid
     */
    public CryptoStats getCryptoStats(String cryptoSymbol, LocalDate from, LocalDate to) {
        String normalizedSymbol = cryptoSymbol.toUpperCase(Locale.ROOT);
        Crypto crypto = cryptoRepository.findBySymbol(normalizedSymbol).orElseThrow(() ->
                new UnsupportedCryptoException(normalizedSymbol));

        Instant resolvedFrom = (from != null) ? toStartInstant(from) : cryptoPriceRepository.findMinTimestamp(crypto);
        Instant resolvedTo = (to != null) ? toEndInstant(to) : cryptoPriceRepository.findMaxTimestamp(crypto);

        if (resolvedFrom.isAfter(resolvedTo)) {
            throw new IllegalArgumentException(
                    "'from' date must be before or equal to 'to' date"
            );
        }

        CryptoPricePoint oldestPricePoint = fetchPricePoint(() -> cryptoPriceRepository.findPricesInRangeOrderedByTimestampAsc(crypto, resolvedFrom, resolvedTo, LIMIT_ONE), cryptoSymbol);
        CryptoPricePoint newestPricePoint = fetchPricePoint(() -> cryptoPriceRepository.findPricesInRangeOrderedByTimestampDesc(crypto, resolvedFrom, resolvedTo, LIMIT_ONE), cryptoSymbol);
        CryptoPricePoint minPricePoint = fetchPricePoint(() -> cryptoPriceRepository.findPricesInRangeOrderedByPriceAsc(crypto, resolvedFrom, resolvedTo, LIMIT_ONE), cryptoSymbol);
        CryptoPricePoint maxPricePoint = fetchPricePoint(() -> cryptoPriceRepository.findPricesInRangeOrderedByPriceDesc(crypto, resolvedFrom, resolvedTo, LIMIT_ONE), cryptoSymbol);

        return new CryptoStats(crypto.getSymbol(), oldestPricePoint, newestPricePoint, minPricePoint, maxPricePoint);
    }

    /**
     * Returns all supported cryptocurrencies sorted by normalized price range
     * in descending order.
     *
     * <p>
     * The normalized range is calculated as {@code (max - min) / min}.
     * Cryptos with insufficient data or a minimum price of zero are excluded.
     * </p>
     *
     * @param from optional start date (inclusive)
     * @param to   optional end date (inclusive)
     * @return list of cryptos sorted by normalized range
     */
    public List<CryptoNormalizedRange> getCryptosByNormalizedRange(
            LocalDate from,
            LocalDate to
    ) {
        List<Crypto> cryptos = cryptoRepository.findAll();

        return cryptos.stream()
                .map(crypto -> calculateNormalizedRange(crypto, from, to))
                .flatMap(Optional::stream)
                .sorted(Comparator.comparing(CryptoNormalizedRange::normalizedRange).reversed())
                .toList();
    }

    /**
     * Returns the cryptocurrency with the highest normalized range for a given day.
     *
     * @param date date for which the normalized range should be calculated
     * @return crypto with the highest normalized range
     *
     * @throws NoDataException if no crypto has data for the given date
     */
    public CryptoNormalizedRange getHighestNormalizedRangeForDay(LocalDate date) {

        return cryptoRepository.findAll().stream()
                .map(crypto -> calculateNormalizedRange(crypto, date, date))
                .flatMap(Optional::stream)
                .max(Comparator.comparing(CryptoNormalizedRange::normalizedRange))
                .orElseThrow(() -> new NoDataException("No data for date: " + date));
    }

    /**
     * Executes a price query expected to return a single result and converts
     * it into a {@link CryptoPricePoint}.
     */
    private CryptoPricePoint fetchPricePoint(Supplier<List<CryptoPrice>> query, String symbol) {
        return query.get().stream()
                .findFirst()
                .map(this::createCryptoPricePoint)
                .orElseThrow(() -> new NoDataException(symbol));
    }

    /**
     * Calculates the normalized price range for a crypto within the given date range.
     *
     * <p>
     * Returns {@link Optional#empty()} if the crypto has no data in the range
     * or if the minimum price is zero.
     * </p>
     */
    private Optional<CryptoNormalizedRange> calculateNormalizedRange(
            Crypto crypto,
            LocalDate from,
            LocalDate to
    ) {
        Instant resolvedFrom = (from != null) ? toStartInstant(from) : cryptoPriceRepository.findMinTimestamp(crypto);
        Instant resolvedTo = (to != null) ? toEndInstant(to) : cryptoPriceRepository.findMaxTimestamp(crypto);

        CryptoPrice min = cryptoPriceRepository
                .findPricesInRangeOrderedByPriceAsc(crypto, resolvedFrom, resolvedTo, LIMIT_ONE)
                .stream()
                .findFirst()
                .orElse(null);

        CryptoPrice max = cryptoPriceRepository
                .findPricesInRangeOrderedByPriceDesc(crypto, resolvedFrom, resolvedTo, LIMIT_ONE)
                .stream()
                .findFirst()
                .orElse(null);

        if (min == null || max == null) {
            return Optional.empty();
        }

        if (min.getPrice().compareTo(BigDecimal.ZERO) == 0) {
            return Optional.empty();
        }

        BigDecimal normalizedRange = max.getPrice()
                .subtract(min.getPrice())
                .divide(min.getPrice(), 8, RoundingMode.HALF_UP);

        return Optional.of(
                new CryptoNormalizedRange(
                        crypto.getSymbol(),
                        normalizedRange
                )
        );
    }


    private CryptoPricePoint createCryptoPricePoint(CryptoPrice cryptoPrice) {
        return new CryptoPricePoint(cryptoPrice.getPrice(), cryptoPrice.getTimestamp());
    }

    private Instant toStartInstant(LocalDate date) {
        return date.atStartOfDay(ZoneOffset.UTC).toInstant();
    }

    private Instant toEndInstant(LocalDate date) {
        return date.plusDays(1)
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant()
                .minusNanos(1);
    }
}
