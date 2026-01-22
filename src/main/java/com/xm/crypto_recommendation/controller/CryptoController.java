package com.xm.crypto_recommendation.controller;

import com.xm.crypto_recommendation.domain.dto.CryptoNormalizedRange;
import com.xm.crypto_recommendation.domain.dto.CryptoStats;
import com.xm.crypto_recommendation.service.CryptoPriceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/crypto")
public class CryptoController {

    private final CryptoPriceService cryptoPriceService;

    @Autowired
    public CryptoController(CryptoPriceService cryptoPriceService) {
        this.cryptoPriceService = cryptoPriceService;
    }

    @GetMapping("/{symbol}/stats")
    public CryptoStats getCryptoStats(
            @PathVariable String symbol,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to) {
        return cryptoPriceService.getCryptoStats(symbol, from, to);
    }

    @GetMapping("/normalized-range")
    public List<CryptoNormalizedRange> getCryptosByNormalizedRange(
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to
    ) {
        return cryptoPriceService.getCryptosByNormalizedRange(from, to);
    }

    @GetMapping("/highest-normalized-range")
    public CryptoNormalizedRange getHighestNormalizedRange(
            @RequestParam LocalDate date
    ) {
        return cryptoPriceService.getHighestNormalizedRangeForDay(date);
    }
}
