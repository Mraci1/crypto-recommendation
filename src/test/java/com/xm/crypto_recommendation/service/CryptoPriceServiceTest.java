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
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
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
        given(cryptoRepository.findBySymbol(BTC)).willReturn(Optional.of(DEFAULT_CRYPTO));
        mockPriceRepositoryWithDefaults();
        // When
        underTest.getCryptoStats(symbol, DEFAULT_FROM_DATE, DEFAULT_TO_DATE);
        // Then
        verify(cryptoRepository).findBySymbol(BTC);
    }

    @Test
    void testGetCryptoStats() {
        // Given
        given(cryptoRepository.findBySymbol(BTC)).willReturn(Optional.of(DEFAULT_CRYPTO));
        mockPriceRepositoryWithDefaults();
        CryptoPricePoint expectedPricePoint = new CryptoPricePoint(DEFAULT_PRICE, DEFAULT_FROM);
        CryptoStats expectedStats = new CryptoStats(BTC, expectedPricePoint, expectedPricePoint, expectedPricePoint, expectedPricePoint);
        // When
        CryptoStats cryptoStats = underTest.getCryptoStats(BTC, DEFAULT_FROM_DATE, DEFAULT_TO_DATE);
        // Then
        verify(cryptoPriceRepository).findPricesInRangeOrderedByPriceDesc(DEFAULT_CRYPTO, DEFAULT_FROM, DEFAULT_TO, LIMIT_ONE);
        verify(cryptoPriceRepository).findPricesInRangeOrderedByPriceAsc(DEFAULT_CRYPTO, DEFAULT_FROM, DEFAULT_TO, LIMIT_ONE);
        verify(cryptoPriceRepository).findPricesInRangeOrderedByTimestampDesc(DEFAULT_CRYPTO, DEFAULT_FROM, DEFAULT_TO, LIMIT_ONE);
        verify(cryptoPriceRepository).findPricesInRangeOrderedByTimestampAsc(DEFAULT_CRYPTO, DEFAULT_FROM, DEFAULT_TO, LIMIT_ONE);
        assertEquals(expectedStats, cryptoStats);
    }

    @Test
    void testGetCryptoStatsWhenFromAndToIsNull() {
        // Given
        given(cryptoRepository.findBySymbol(BTC)).willReturn(Optional.of(DEFAULT_CRYPTO));
        mockPriceRepositoryWithDefaults();
        given(cryptoPriceRepository.findMaxTimestamp(DEFAULT_CRYPTO)).willReturn(DEFAULT_TO);
        given(cryptoPriceRepository.findMinTimestamp(DEFAULT_CRYPTO)).willReturn(DEFAULT_FROM);
        CryptoPricePoint expectedPricePoint = new CryptoPricePoint(DEFAULT_PRICE, DEFAULT_FROM);
        CryptoStats expectedStats = new CryptoStats(BTC, expectedPricePoint, expectedPricePoint, expectedPricePoint, expectedPricePoint);
        // When
        CryptoStats cryptoStats = underTest.getCryptoStats(BTC, null, null);
        // Then
        verify(cryptoPriceRepository).findPricesInRangeOrderedByPriceDesc(DEFAULT_CRYPTO, DEFAULT_FROM, DEFAULT_TO, LIMIT_ONE);
        verify(cryptoPriceRepository).findPricesInRangeOrderedByPriceAsc(DEFAULT_CRYPTO, DEFAULT_FROM, DEFAULT_TO, LIMIT_ONE);
        verify(cryptoPriceRepository).findPricesInRangeOrderedByTimestampDesc(DEFAULT_CRYPTO, DEFAULT_FROM, DEFAULT_TO, LIMIT_ONE);
        verify(cryptoPriceRepository).findPricesInRangeOrderedByTimestampAsc(DEFAULT_CRYPTO, DEFAULT_FROM, DEFAULT_TO, LIMIT_ONE);
        assertEquals(expectedStats, cryptoStats);
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
        given(cryptoRepository.findBySymbol(BTC)).willReturn(Optional.of(DEFAULT_CRYPTO));
        // When / Then
        assertThrows(NoDataException.class, () -> underTest.getCryptoStats(BTC, DEFAULT_FROM_DATE, DEFAULT_TO_DATE));
    }

    @Test
    void testGetCryptosByNormalizedRange() {
        // Given
        Crypto crypto = new Crypto("ETH");
        given(cryptoRepository.findAll()).willReturn(List.of(DEFAULT_CRYPTO, crypto));
        BigDecimal btcMax = BigDecimal.valueOf(200.0);
        BigDecimal btcMin = BigDecimal.valueOf(100.0);
        BigDecimal ethMax = BigDecimal.valueOf(654.321);
        BigDecimal ethMin = BigDecimal.valueOf(123.456);
        given(cryptoPriceRepository.findPricesInRangeOrderedByPriceDesc(DEFAULT_CRYPTO, DEFAULT_FROM, DEFAULT_TO, LIMIT_ONE))
                .willReturn(List.of(new CryptoPrice(DEFAULT_CRYPTO, DEFAULT_FROM, btcMax)));
        given(cryptoPriceRepository.findPricesInRangeOrderedByPriceAsc(DEFAULT_CRYPTO, DEFAULT_FROM, DEFAULT_TO, LIMIT_ONE))
                .willReturn(List.of(new CryptoPrice(DEFAULT_CRYPTO, DEFAULT_FROM, btcMin)));
        given(cryptoPriceRepository.findPricesInRangeOrderedByPriceDesc(crypto, DEFAULT_FROM, DEFAULT_TO, LIMIT_ONE))
                .willReturn(List.of(new CryptoPrice(crypto, DEFAULT_FROM, ethMax)));
        given(cryptoPriceRepository.findPricesInRangeOrderedByPriceAsc(crypto, DEFAULT_FROM, DEFAULT_TO, LIMIT_ONE))
                .willReturn(List.of(new CryptoPrice(crypto, DEFAULT_FROM, ethMin)));
        // When
        List<CryptoNormalizedRange> cryptosByNormalizedRange = underTest.getCryptosByNormalizedRange(DEFAULT_FROM_DATE, DEFAULT_TO_DATE);
        // Then
        verify(cryptoRepository).findAll();
        assertEquals(2, cryptosByNormalizedRange.size());
        assertEquals("ETH", cryptosByNormalizedRange.get(0).symbol());
        assertEquals("BTC", cryptosByNormalizedRange.get(1).symbol());
        assertEquals(ethMax.subtract(ethMin).divide(ethMin, 8, RoundingMode.HALF_UP), cryptosByNormalizedRange.get(0).normalizedRange());
        assertEquals(btcMax.subtract(btcMin).divide(btcMin, 8, RoundingMode.HALF_UP), cryptosByNormalizedRange.get(1).normalizedRange());
    }

    @Test
    void testGetCryptosByNormalizedRangeWhenToAndFromIsNull() {
        // Given
        Crypto crypto = new Crypto("ETH");
        given(cryptoRepository.findAll()).willReturn(List.of(DEFAULT_CRYPTO, crypto));
        given(cryptoPriceRepository.findMaxTimestamp(any())).willReturn(DEFAULT_TO);
        given(cryptoPriceRepository.findMinTimestamp(any())).willReturn(DEFAULT_FROM);
        // When
        List<CryptoNormalizedRange> cryptosByNormalizedRange = underTest.getCryptosByNormalizedRange(null, null);
        // Then
        verify(cryptoRepository).findAll();
        verify(cryptoPriceRepository, times(2)).findMinTimestamp(any());
        verify(cryptoPriceRepository, times(2)).findMaxTimestamp(any());
        verify(cryptoPriceRepository, times(2)).findPricesInRangeOrderedByPriceAsc(any(), eq(DEFAULT_FROM), eq(DEFAULT_TO), eq(LIMIT_ONE));
        verify(cryptoPriceRepository, times(2)).findPricesInRangeOrderedByPriceDesc(any(), eq(DEFAULT_FROM), eq(DEFAULT_TO), eq(LIMIT_ONE));
    }

    @Test
    void testGetCryptosByNormalizedRangeWhenOneMinIsZero() {
        // Given
        Crypto crypto = new Crypto("ETH");
        given(cryptoRepository.findAll()).willReturn(List.of(DEFAULT_CRYPTO, crypto));
        BigDecimal btcMax = BigDecimal.valueOf(200.0);
        BigDecimal btcMin = BigDecimal.valueOf(100.0);
        BigDecimal ethMax = BigDecimal.valueOf(654.321);
        BigDecimal ethMin = BigDecimal.valueOf(0);
        given(cryptoPriceRepository.findPricesInRangeOrderedByPriceDesc(DEFAULT_CRYPTO, DEFAULT_FROM, DEFAULT_TO, LIMIT_ONE))
                .willReturn(List.of(new CryptoPrice(DEFAULT_CRYPTO, DEFAULT_FROM, btcMax)));
        given(cryptoPriceRepository.findPricesInRangeOrderedByPriceAsc(DEFAULT_CRYPTO, DEFAULT_FROM, DEFAULT_TO, LIMIT_ONE))
                .willReturn(List.of(new CryptoPrice(DEFAULT_CRYPTO, DEFAULT_FROM, btcMin)));
        given(cryptoPriceRepository.findPricesInRangeOrderedByPriceDesc(crypto, DEFAULT_FROM, DEFAULT_TO, LIMIT_ONE))
                .willReturn(List.of(new CryptoPrice(crypto, DEFAULT_FROM, ethMax)));
        given(cryptoPriceRepository.findPricesInRangeOrderedByPriceAsc(crypto, DEFAULT_FROM, DEFAULT_TO, LIMIT_ONE))
                .willReturn(List.of(new CryptoPrice(crypto, DEFAULT_FROM, ethMin)));
        // When
        List<CryptoNormalizedRange> cryptosByNormalizedRange = underTest.getCryptosByNormalizedRange(DEFAULT_FROM_DATE, DEFAULT_TO_DATE);
        // Then
        verify(cryptoRepository).findAll();
        assertEquals(1, cryptosByNormalizedRange.size());
        assertEquals("BTC", cryptosByNormalizedRange.get(0).symbol());
        assertEquals(btcMax.subtract(btcMin).divide(btcMin, 8, RoundingMode.HALF_UP), cryptosByNormalizedRange.get(0).normalizedRange());
    }

    @Test
    void testGetCryptosByNormalizedRangeWhenOneMinAndMaxIsNull() {
        // Given
        Crypto crypto = new Crypto("ETH");
        given(cryptoRepository.findAll()).willReturn(List.of(DEFAULT_CRYPTO, crypto));
        BigDecimal ethMax = BigDecimal.valueOf(654.321);
        BigDecimal ethMin = BigDecimal.valueOf(123.456);
        given(cryptoPriceRepository.findPricesInRangeOrderedByPriceDesc(DEFAULT_CRYPTO, DEFAULT_FROM, DEFAULT_TO, LIMIT_ONE))
                .willReturn(Collections.emptyList());
        given(cryptoPriceRepository.findPricesInRangeOrderedByPriceAsc(DEFAULT_CRYPTO, DEFAULT_FROM, DEFAULT_TO, LIMIT_ONE))
                .willReturn(Collections.emptyList());
        given(cryptoPriceRepository.findPricesInRangeOrderedByPriceDesc(crypto, DEFAULT_FROM, DEFAULT_TO, LIMIT_ONE))
                .willReturn(List.of(new CryptoPrice(crypto, DEFAULT_FROM, ethMax)));
        given(cryptoPriceRepository.findPricesInRangeOrderedByPriceAsc(crypto, DEFAULT_FROM, DEFAULT_TO, LIMIT_ONE))
                .willReturn(List.of(new CryptoPrice(crypto, DEFAULT_FROM, ethMin)));
        // When
        List<CryptoNormalizedRange> cryptosByNormalizedRange = underTest.getCryptosByNormalizedRange(DEFAULT_FROM_DATE, DEFAULT_TO_DATE);
        // Then
        verify(cryptoRepository).findAll();
        assertEquals(1, cryptosByNormalizedRange.size());
        assertEquals("ETH", cryptosByNormalizedRange.get(0).symbol());
        assertEquals(ethMax.subtract(ethMin).divide(ethMin, 8, RoundingMode.HALF_UP), cryptosByNormalizedRange.get(0).normalizedRange());
    }

    @Test
    void testGetHighestNormalizedRangeForDay() {
        // Given
        Crypto crypto = new Crypto("ETH");
        given(cryptoRepository.findAll()).willReturn(List.of(DEFAULT_CRYPTO, crypto));
        given(cryptoPriceRepository.findPricesInRangeOrderedByPriceDesc(eq(DEFAULT_CRYPTO), any(), any(), any())).willReturn(List.of(
                new CryptoPrice(DEFAULT_CRYPTO, DEFAULT_FROM, BigDecimal.valueOf(400))
        ));
        given(cryptoPriceRepository.findPricesInRangeOrderedByPriceAsc(eq(DEFAULT_CRYPTO), any(), any(), any())).willReturn(List.of(
                new CryptoPrice(DEFAULT_CRYPTO, DEFAULT_FROM, BigDecimal.valueOf(200))
        ));
        given(cryptoPriceRepository.findPricesInRangeOrderedByPriceDesc(eq(crypto), any(), any(), any())).willReturn(List.of(
                new CryptoPrice(crypto, DEFAULT_FROM, BigDecimal.valueOf(300))
        ));
        given(cryptoPriceRepository.findPricesInRangeOrderedByPriceAsc(eq(crypto), any(), any(), any())).willReturn(List.of(
                new CryptoPrice(crypto, DEFAULT_FROM, BigDecimal.valueOf(100))
        ));
        // When
        CryptoNormalizedRange highestNormalizedRange = underTest.getHighestNormalizedRangeForDay(DEFAULT_FROM_DATE);
        // Then
        assertEquals("ETH", highestNormalizedRange.symbol());
        BigDecimal expectedNormalizedRange = BigDecimal.valueOf(300 - 100)
                .divide(BigDecimal.valueOf(100), 8, RoundingMode.HALF_UP);
        assertEquals(expectedNormalizedRange, highestNormalizedRange.normalizedRange());

    }

    @Test
    void testGetHighestNormalizedRangeForDayWhenNoDataIsAvailable() {
        // Given
        Crypto crypto = new Crypto("ETH");
        given(cryptoRepository.findAll()).willReturn(List.of(DEFAULT_CRYPTO, crypto));

        // When / Then
        assertThrows(NoDataException.class, () -> underTest.getHighestNormalizedRangeForDay(DEFAULT_FROM_DATE));
    }

    private static String[] symbolNormalizationProvider() {
        return new String[]{"btc", "BtC", "BTC"};
    }

    private void mockPriceRepositoryWithDefaults() {
        given(cryptoPriceRepository.findPricesInRangeOrderedByPriceDesc(
                DEFAULT_CRYPTO, DEFAULT_FROM, DEFAULT_TO, LIMIT_ONE)).willReturn(List.of(DEFAULT_CRYPTO_PRICE));
        given(cryptoPriceRepository.findPricesInRangeOrderedByPriceAsc(
                DEFAULT_CRYPTO, DEFAULT_FROM, DEFAULT_TO, LIMIT_ONE)).willReturn(List.of(DEFAULT_CRYPTO_PRICE));
        given(cryptoPriceRepository.findPricesInRangeOrderedByTimestampDesc(
                DEFAULT_CRYPTO, DEFAULT_FROM, DEFAULT_TO, LIMIT_ONE)).willReturn(List.of(DEFAULT_CRYPTO_PRICE));
        given(cryptoPriceRepository.findPricesInRangeOrderedByTimestampAsc(
                DEFAULT_CRYPTO, DEFAULT_FROM, DEFAULT_TO, LIMIT_ONE)).willReturn(List.of(DEFAULT_CRYPTO_PRICE));
    }
}
