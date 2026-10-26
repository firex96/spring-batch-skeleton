package com.rex.batch.skeleton.exception

import com.rex.batch.skeleton.enums.ErrorCode

/**
 * 배치 처리 중 발생하는 예외 — 500 `BATCH_001`.
 *
 * Step 안에서 던진다. Spring Batch 의 실패 처리는 예외 타입에 의존하지 않으므로 Job 동작은 이 코드와 무관하고,
 * 관리자·웹훅 API 경로로 올라오면 이 코드로 응답한다.
 */
class BatchProcessException(
    message: String,
    cause: Throwable? = null,
) : CodedException(ErrorCode.BATCH_PROCESS_ERROR, message, cause = cause)