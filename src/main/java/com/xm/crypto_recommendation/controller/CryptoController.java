package com.xm.crypto_recommendation.controller;

import com.xm.crypto_recommendation.domain.dto.CryptoNormalizedRange;
import com.xm.crypto_recommendation.domain.dto.CryptoStats;
import com.xm.crypto_recommendation.exception.ApiError;
import com.xm.crypto_recommendation.service.CryptoPriceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;


/**
 * REST controller exposing endpoints for querying cryptocurrency statistics
 * and investment-related recommendations based on historical price data.
 */
@RestController
@RequestMapping("/api/cryptos")
@Tag(name = "Crypto Recommendations", description = "Endpoints for crypto statistics and recommendations")
public class CryptoController {

    private final CryptoPriceService cryptoPriceService;

    @Autowired
    public CryptoController(CryptoPriceService cryptoPriceService) {
        this.cryptoPriceService = cryptoPriceService;
    }

    /**
     * Returns price statistics (oldest, newest, minimum, maximum) for a given cryptocurrency.
     *
     * <p>
     * The time range can be optionally restricted using {@code from} and {@code to} dates.
     * If omitted, the full available time range for the given crypto is used.
     * </p>
     *
     * @param symbol cryptocurrency symbol (e.g. BTC, ETH)
     * @param from   optional start date (inclusive)
     * @param to     optional end date (inclusive)
     * @return aggregated crypto price statistics
     */
    @Operation(
            summary = "Get crypto price statistics",
            description = "Returns oldest, newest, minimum and maximum prices for a given crypto symbol"
    )
    @ApiResponse(responseCode = "200", description = "Statistics successfully calculated")
    @ApiResponse(
            responseCode = "404",
            description = "Unsupported crypto or no data available",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    @ApiResponse(
            responseCode = "400",
            description = "Invalid date range supplied",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    @GetMapping("/{symbol}/stats")
    public CryptoStats getCryptoStats(
            @Parameter(description = "Cryptocurrency symbol (e.g. BTC)", example = "BTC")
            @PathVariable String symbol,
            @Parameter(description = "Start date (YYYY-MM-DD)", example = "2022-01-01")
            @RequestParam(required = false) LocalDate from,
            @Parameter(description = "End date (YYYY-MM-DD)", example = "2022-01-31")
            @RequestParam(required = false) LocalDate to) {
        return cryptoPriceService.getCryptoStats(symbol, from, to);
    }

    /**
     * Returns all supported cryptocurrencies sorted by normalized price range
     * in descending order.
     *
     * <p>
     * Normalized range is calculated as {@code (max - min) / min}.
     * </p>
     * <p>
     * The time range can be optionally restricted using {@code from} and {@code to} dates.
     * If omitted, the full available time range is used.
     * </p>
     *
     * @param from optional start date (inclusive)
     * @param to   optional end date (inclusive)
     * @return list of cryptos sorted by normalized range
     */
    @Operation(
            summary = "Get cryptos by normalized range",
            description = "Returns all cryptos sorted descending by normalized price range"
    )
    @ApiResponse(responseCode = "200", description = "Normalized ranges successfully calculated")
    @ApiResponse(
            responseCode = "400",
            description = "Invalid date range supplied",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    @GetMapping("/normalized-range")
    public List<CryptoNormalizedRange> getCryptosByNormalizedRange(
            @Parameter(description = "Start date (YYYY-MM-DD)", example = "2022-01-01")
            @RequestParam(required = false) LocalDate from,
            @Parameter(description = "End date (YYYY-MM-DD)", example = "2022-01-31")
            @RequestParam(required = false) LocalDate to
    ) {
        return cryptoPriceService.getCryptosByNormalizedRange(from, to);
    }

    /**
     * Returns the cryptocurrency with the highest normalized price range
     * for a specific day.
     *
     * @param date date for which the highest normalized range should be calculated
     * @return crypto with the highest normalized range on the given date
     */
    @Operation(
            summary = "Get crypto with highest normalized range for a day",
            description = "Returns the crypto that had the highest normalized range on the specified date"
    )
    @ApiResponse(responseCode = "200", description = "Crypto successfully identified")
    @ApiResponse(
            responseCode = "404",
            description = "No data available for the given date",
            content = @Content(schema = @Schema(implementation = ApiError.class))
    )
    @GetMapping("/highest-normalized-range")
    public CryptoNormalizedRange getHighestNormalizedRange(
            @Parameter(description = "Date to evaluate (YYYY-MM-DD)", example = "2022-01-01", required = true)
            @RequestParam LocalDate date
    ) {
        return cryptoPriceService.getHighestNormalizedRangeForDay(date);
    }
}
