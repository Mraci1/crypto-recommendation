package com.xm.crypto_recommendation.exception;

public record ApiError(
        String code,
        String message
) {}
