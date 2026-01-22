package com.xm.crypto_recommendation.exception;

/**
 * Thrown when a requested cryptocurrency symbol is not supported
 * or does not exist in the system.
 *
 * <p>
 * This exception is typically raised when a client requests a crypto
 * that has no corresponding data loaded into the application.
 * </p>
 */
public class UnsupportedCryptoException extends RuntimeException {
    public UnsupportedCryptoException(String message) {
        super(message);
    }
}
