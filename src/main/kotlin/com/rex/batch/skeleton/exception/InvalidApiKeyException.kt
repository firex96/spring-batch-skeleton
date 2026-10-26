package com.rex.batch.skeleton.exception

import com.rex.batch.skeleton.enums.ErrorCode

/**
 * API Key 인증 실패 예외 — 401 `AUTH_001`.
 */
class InvalidApiKeyException(
    message: String = ErrorCode.INVALID_API_KEY.message,
    cause: Throwable? = null,
) : CodedException(ErrorCode.INVALID_API_KEY, message, cause = cause)