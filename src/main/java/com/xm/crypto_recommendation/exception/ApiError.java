package com.xm.crypto_recommendation.exception;

/**
 * Standardized error response returned by the REST API.
 *
 * <p>
 * This record is used for all error responses produced by the application
 * and is exposed to API consumers as part of the public contract.
 * </p>
 *
 * @param code    machine-readable error code identifying the error type
 * @param message human-readable description of the error
 */
public record ApiError(
        String code,
        String message
) {
}
