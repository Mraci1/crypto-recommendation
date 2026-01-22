package com.xm.crypto_recommendation.exception.handler;

import com.xm.crypto_recommendation.exception.ApiError;
import com.xm.crypto_recommendation.exception.NoDataException;
import com.xm.crypto_recommendation.exception.UnsupportedCryptoException;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler for the REST API.
 *
 * <p>
 * Translates domain-specific and validation exceptions into consistent,
 * structured HTTP error responses.
 * </p>
 *
 * <p>
 * All error responses follow a standardized {@link ApiError} format and are
 * documented via OpenAPI to provide clear feedback to API consumers.
 * </p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles requests for unsupported or unknown cryptocurrency symbols.
     *
     * @param ex thrown when a requested crypto does not exist
     * @return HTTP 404 response with error details
     */
    @ApiResponses({
            @ApiResponse(
                    responseCode = "404",
                    description = "Requested cryptocurrency is not supported",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    @ExceptionHandler(UnsupportedCryptoException.class)
    public ResponseEntity<ApiError> handleUnsupportedCrypto(
            UnsupportedCryptoException ex
    ) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ApiError(
                        "UNSUPPORTED_CRYPTO",
                        ex.getMessage()
                ));
    }

    /**
     * Handles cases where no price data is available for the given query parameters.
     *
     * @param ex thrown when no data exists for the requested time range or date
     * @return HTTP 404 response with error details
     */
    @ApiResponses({
            @ApiResponse(
                    responseCode = "404",
                    description = "No data available for the given request",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    @ExceptionHandler(NoDataException.class)
    public ResponseEntity<ApiError> handleNoData(
            NoDataException ex
    ) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ApiError(
                        "NO_DATA",
                        ex.getMessage()
                ));
    }

    /**
     * Handles invalid request parameters or logically incorrect input.
     *
     * @param ex thrown when request validation fails
     * @return HTTP 400 response with error details
     */
    @ApiResponses({
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request parameters",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleBadRequest(
            IllegalArgumentException ex
    ) {
        return ResponseEntity
                .badRequest()
                .body(new ApiError(
                        "INVALID_REQUEST",
                        ex.getMessage()
                ));
    }

    /**
     * Catches all unhandled exceptions, returning a generic server error response.
     *
     * @param ex any unexpected exception
     * @return HTTP 500 response with generic error details
     */
    @ApiResponses({
            @ApiResponse(
                    responseCode = "500",
                    description = "Unexpected server error",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiError("INTERNAL_ERROR", "Unexpected server error"));
    }
}
