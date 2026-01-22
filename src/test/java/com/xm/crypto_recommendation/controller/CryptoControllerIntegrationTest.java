package com.xm.crypto_recommendation.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CryptoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * Verifies:
     * - CSV parsing on startup
     * - DB persistence
     * - stats calculation
     * - JSON response structure
     */
    @Test
    void getCryptoStatsShouldReturnStatsFromCsvData() throws Exception {
        mockMvc.perform(get("/api/cryptos/BTC/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symbol").value("BTC"))
                .andExpect(jsonPath("$.min.price").value(46813.21000000))
                .andExpect(jsonPath("$.max.price").value(47143.98000000))
                .andExpect(jsonPath("$.oldest.timestamp").exists())
                .andExpect(jsonPath("$.newest.timestamp").exists());
    }

    /**
     * Verifies:
     * - normalized range calculation
     * - descending sort
     * - full pipeline CSV → DB → service → controller
     */
    @Test
    void getCryptosByNormalizedRangeShouldReturnSortedList() throws Exception {
        mockMvc.perform(get("/api/cryptos/normalized-range"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].symbol").exists())
                .andExpect(jsonPath("$[0].normalizedRange").isNumber());
    }

    /**
     * Verifies:
     * - date parameter binding
     * - daily normalized range calculation
     */
    @Test
    void getHighestNormalizedRangeForDayShouldReturnCrypto() throws Exception {
        mockMvc.perform(get("/api/cryptos/highest-normalized-range")
                        .param("date", "2022-01-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symbol").exists())
                .andExpect(jsonPath("$.normalizedRange").isNumber());
    }

    /**
     * Verifies:
     * - unsupported crypto handling
     * - global exception handler
     * - standardized error response
     */
    @Test
    void getCryptoStatsWhenCryptoIsUnsupportedShouldReturn404() throws Exception {
        mockMvc.perform(get("/api/cryptos/UNKNOWN/stats"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("UNSUPPORTED_CRYPTO"))
                .andExpect(jsonPath("$.message").exists());
    }
}
