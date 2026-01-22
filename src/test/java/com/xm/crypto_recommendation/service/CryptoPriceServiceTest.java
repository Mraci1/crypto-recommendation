package com.xm.crypto_recommendation.service;

import com.xm.crypto_recommendation.domain.dto.CryptoPricePoint;
import com.xm.crypto_recommendation.domain.dto.CryptoStats;
import com.xm.crypto_recommendation.domain.entity.Crypto;
import com.xm.crypto_recommendation.domain.entity.CryptoPrice;
import com.xm.crypto_recommendation.exception.NoDataException;
import com.xm.crypto_recommendation.exception.UnsupportedCryptoException;
import com.xm.crypto_recommendation.repository.CryptoPriceRepository;
import com.xm.crypto_recommendation.repository.CryptoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CryptoPriceServiceTest {

    private static final String BTC = "BTC";
    private static final Pageable LIMIT_ONE = PageRequest.of(0, 1);
    private static final LocalDate DEFAULT_FROM_DATE = LocalDate.of(2023, 1, 1);
    private static final LocalDate DEFAULT_TO_DATE = DEFAULT_FROM_DATE.plusDays(31);
    private static final Instant DEFAULT_FROM = DEFAULT_FROM_DATE.atStartOfDay().toInstant(java.time.ZoneOffset.UTC);
    private static final Instant DEFAULT_TO = DEFAULT_TO_DATE.plusDays(1).atStartOfDay().toInstant(java.time.ZoneOffset.UTC).minusNanos(1);
    private static final Crypto DEFAULT_CRYPTO = new Crypto(BTC);
    private static final BigDecimal DEFAULT_PRICE = BigDecimal.valueOf(123.456);
    private static final CryptoPrice DEFAULT_CRYPTO_PRICE = new CryptoPrice(DEFAULT_CRYPTO, DEFAULT_FROM, DEFAULT_PRICE);

    @Mock
    private CryptoRepository cryptoRepository;
    @Mock
    private CryptoPriceRepository cryptoPriceRepository;

    @InjectMocks
    private CryptoPriceService underTest;

    @ParameterizedTest
    @MethodSource("symbolNormalizationProvider")
    void testSymbolNormalization(String symbol) {
        // Given
        Crypto crypto = new Crypto(BTC);
        given(cryptoRepository.findBySymbol(BTC)).willReturn(Optional.of(crypto));
        mockPriceRepositoryWithDefaults(crypto);
        // When
        underTest.getCryptoStats(symbol, DEFAULT_FROM_DATE, DEFAULT_TO_DATE);
        // Then
        verify(cryptoRepository).findBySymbol(BTC);
    }

    @Test
    void testGetCryptoStats() {
        // Given
        Crypto crypto = new Crypto(BTC);
        given(cryptoRepository.findBySymbol(BTC)).willReturn(Optional.of(crypto));
        mockPriceRepositoryWithDefaults(crypto);
        CryptoPricePoint expectedPricePoint = new CryptoPricePoint(DEFAULT_PRICE, DEFAULT_FROM);
        CryptoStats expectedStats = new CryptoStats(BTC, expectedPricePoint, expectedPricePoint, expectedPricePoint, expectedPricePoint);
        // When
        CryptoStats cryptoStats = underTest.getCryptoStats(crypto.getSymbol(), DEFAULT_FROM_DATE, DEFAULT_TO_DATE);
        // Then
        verify(cryptoPriceRepository).findPricesInRangeOrderedByPriceDesc(crypto, DEFAULT_FROM, DEFAULT_TO, LIMIT_ONE);
        verify(cryptoPriceRepository).findPricesInRangeOrderedByPriceAsc(crypto, DEFAULT_FROM, DEFAULT_TO, LIMIT_ONE);
        verify(cryptoPriceRepository).findPricesInRangeOrderedByTimestampDesc(crypto, DEFAULT_FROM, DEFAULT_TO, LIMIT_ONE);
        verify(cryptoPriceRepository).findPricesInRangeOrderedByTimestampAsc(crypto, DEFAULT_FROM, DEFAULT_TO, LIMIT_ONE);
        assertEquals(cryptoStats, expectedStats);
    }

    @Test
    void testGetCryptoStatsWhenFromAndToIsNull() {
        // Given
        Crypto crypto = new Crypto(BTC);
        given(cryptoRepository.findBySymbol(BTC)).willReturn(Optional.of(crypto));
        mockPriceRepositoryWithDefaults(crypto);
        CryptoPricePoint expectedPricePoint = new CryptoPricePoint(DEFAULT_PRICE, DEFAULT_FROM);
        CryptoStats expectedStats = new CryptoStats(BTC, expectedPricePoint, expectedPricePoint, expectedPricePoint, expectedPricePoint);
        // When
        CryptoStats cryptoStats = underTest.getCryptoStats(crypto.getSymbol(), null, null);
        // Then
        verify(cryptoPriceRepository).findPricesInRangeOrderedByPriceDesc(crypto, DEFAULT_FROM, DEFAULT_TO, LIMIT_ONE);
        verify(cryptoPriceRepository).findPricesInRangeOrderedByPriceAsc(crypto, DEFAULT_FROM, DEFAULT_TO, LIMIT_ONE);
        verify(cryptoPriceRepository).findPricesInRangeOrderedByTimestampDesc(crypto, DEFAULT_FROM, DEFAULT_TO, LIMIT_ONE);
        verify(cryptoPriceRepository).findPricesInRangeOrderedByTimestampAsc(crypto, DEFAULT_FROM, DEFAULT_TO, LIMIT_ONE);
        assertEquals(cryptoStats, expectedStats);
    }

    @Test
    void testGetCryptoStatsWhenCryptoNotFound() {
        // Given
        given(cryptoRepository.findBySymbol(BTC)).willReturn(Optional.empty());
        // When / Then
        assertThrows(UnsupportedCryptoException.class, () ->
                underTest.getCryptoStats(BTC, DEFAULT_FROM_DATE, DEFAULT_TO_DATE));
    }

    @Test
    void testGetCryptoStatsWhenInvalidTimeRange() {
        // Given
        LocalDate fromDate = LocalDate.of(2023, 2, 1);
        LocalDate toDate = LocalDate.of(2023, 1, 1);
        given(cryptoRepository.findBySymbol(BTC)).willReturn(Optional.of(DEFAULT_CRYPTO));
        // When / Then
        assertThrows(IllegalArgumentException.class, () ->
                underTest.getCryptoStats(BTC, fromDate, toDate));
    }

    @Test
    void testGetCryptoStatsWhenNoPricesInRange() {
        // Given
        Crypto crypto = new Crypto(BTC);
        given(cryptoRepository.findBySymbol(BTC)).willReturn(Optional.of(crypto));
        // When / Then
        assertThrows(NoDataException.class, () -> underTest.getCryptoStats(crypto.getSymbol(), DEFAULT_FROM_DATE, DEFAULT_TO_DATE));
    }

    private static String[] symbolNormalizationProvider() {
        return new String[]{"btc", "BtC", "BTC"};
    }

    private void mockPriceRepositoryWithDefaults(Crypto crypto) {
        given(cryptoPriceRepository.findPricesInRangeOrderedByPriceDesc(
                crypto, DEFAULT_FROM, DEFAULT_TO, LIMIT_ONE)).willReturn(List.of(DEFAULT_CRYPTO_PRICE));
        given(cryptoPriceRepository.findPricesInRangeOrderedByPriceAsc(
                crypto, DEFAULT_FROM, DEFAULT_TO, LIMIT_ONE)).willReturn(List.of(DEFAULT_CRYPTO_PRICE));
        given(cryptoPriceRepository.findPricesInRangeOrderedByTimestampDesc(
                crypto, DEFAULT_FROM, DEFAULT_TO, LIMIT_ONE)).willReturn(List.of(DEFAULT_CRYPTO_PRICE));
        given(cryptoPriceRepository.findPricesInRangeOrderedByTimestampAsc(
                crypto, DEFAULT_FROM, DEFAULT_TO, LIMIT_ONE)).willReturn(List.of(DEFAULT_CRYPTO_PRICE));
        given(cryptoPriceRepository.findMaxTimestamp(crypto)).willReturn(DEFAULT_TO);
        given(cryptoPriceRepository.findMinTimestamp(crypto)).willReturn(DEFAULT_FROM);
    }
}
