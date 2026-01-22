package com.xm.crypto_recommendation.exception;

/**
 * Thrown when no price data is available for a given query.
 *
 * <p>
 * Typically used when a valid cryptocurrency is requested, but no
 * data exists for the specified date or time range.
 * </p>
 */
public class NoDataException extends RuntimeException {
    public NoDataException(String message) {
        super(message);
    }
}
