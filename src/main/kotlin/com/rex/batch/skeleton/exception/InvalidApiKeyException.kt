package com.rex.batch.skeleton.exception

import com.rex.batch.skeleton.enums.ErrorCode

/**
 * API Key 인증 실패 예외
 */
class InvalidApiKeyException(
    message: String = ErrorCode.INVALID_API_KEY.message,
    cause: Throwable? = null,
) : RuntimeException(message, cause) {
    val errorCode: ErrorCode = ErrorCode.INVALID_API_KEY
}