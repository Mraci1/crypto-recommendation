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

@Service
public class CryptoPriceService {

    private static final Pageable LIMIT_ONE = PageRequest.of(0, 1);

    private final CryptoRepository cryptoRepository;
    private final CryptoPriceRepository cryptoPriceRepository;

    @Autowired
    public CryptoPriceService(CryptoRepository cryptoRepository, CryptoPriceRepository cryptoPriceRepository) {
        this.cryptoRepository = cryptoRepository;
        this.cryptoPriceRepository = cryptoPriceRepository;
    }

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

    public CryptoNormalizedRange getHighestNormalizedRangeForDay(LocalDate date) {

        return cryptoRepository.findAll().stream()
                .map(crypto -> calculateNormalizedRange(crypto, date, date))
                .flatMap(Optional::stream)
                .max(Comparator.comparing(CryptoNormalizedRange::normalizedRange))
                .orElseThrow(() -> new NoDataException("No data for date: " + date));
    }

    private CryptoPricePoint fetchPricePoint(Supplier<List<CryptoPrice>> query, String symbol) {
        return query.get().stream()
                .findFirst()
                .map(this::createCryptoPricePoint)
                .orElseThrow(() -> new NoDataException(symbol));
    }

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
