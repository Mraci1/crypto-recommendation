package com.xm.crypto_recommendation.controller;

import com.xm.crypto_recommendation.service.CryptoPriceService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CryptoControllerTest {

    @Mock
    private CryptoPriceService cryptoPriceService;

    @InjectMocks
    private CryptoController underTest;

    @Test
    void getCryptoStats() {
        //Given
        given(cryptoPriceService.getCryptoStats(any(), any(), any())).willReturn(mock());
        LocalDate from = LocalDate.of(2023, 1, 1);
        LocalDate to = LocalDate.of(2023, 1, 31);
        String symbol = "BTC";
        //When
        underTest.getCryptoStats(symbol, from, to);
        //Then
        verify(cryptoPriceService).getCryptoStats(symbol, from, to);
    }

    @Test
    void getCryptosByNormalizedRange() {
        given(cryptoPriceService.getCryptosByNormalizedRange(any(), any())).willReturn(mock());
        LocalDate from = LocalDate.of(2023, 1, 1);
        LocalDate to = LocalDate.of(2023, 1, 31);
        //When
        underTest.getCryptosByNormalizedRange(from, to);
        //Then
        verify(cryptoPriceService).getCryptosByNormalizedRange(from, to);
    }

    @Test
    void getHighestNormalizedRange() {
        given(cryptoPriceService.getHighestNormalizedRangeForDay(any())).willReturn(mock());
        LocalDate day = LocalDate.of(2023, 1, 1);
        //When
        underTest.getHighestNormalizedRange(day);
        //Then
        verify(cryptoPriceService).getHighestNormalizedRangeForDay(day);
    }
}