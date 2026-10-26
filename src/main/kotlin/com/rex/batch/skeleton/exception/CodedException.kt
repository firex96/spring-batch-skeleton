package com.rex.batch.skeleton.exception

import com.rex.batch.skeleton.enums.ErrorCode

/**
 * 상태·코드를 [errorCode] 하나가 정하는 예외의 부모. `GlobalExceptionHandler` 가 이 타입 하나로 받는다.
 *
 * 새 업무 코드가 필요하면 `ErrorCode` 에 항목을 추가하고 `throw CodedException(ErrorCode.X)` 로 던진다.
 * [message] 는 응답 `message` 로, [details] 는 응답 `details` 로 나간다 — 요청에서 온 값(위조 가능)을 담지 않는다.
 */
open class CodedException(
    val errorCode: ErrorCode,
    message: String = errorCode.message,
    val details: String? = null,
    cause: Throwable? = null,
) : RuntimeException(message, cause)