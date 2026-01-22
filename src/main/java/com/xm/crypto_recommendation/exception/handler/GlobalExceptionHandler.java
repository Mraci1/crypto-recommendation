package com.xm.crypto_recommendation.exception.handler;

import com.xm.crypto_recommendation.exception.ApiError;
import com.xm.crypto_recommendation.exception.NoDataException;
import com.xm.crypto_recommendation.exception.UnsupportedCryptoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

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
}
