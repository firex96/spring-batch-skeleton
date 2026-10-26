package com.rex.batch.skeleton.enums

import org.springframework.http.HttpStatus

/**
 * API 에러 코드
 */
enum class ErrorCode(
    val status: HttpStatus,
    val code: String,
    val message: String,
) {
    // 인증 관련
    INVALID_API_KEY(HttpStatus.UNAUTHORIZED, "AUTH_001", "Invalid or missing API key"),

    // 인가 관련 — 인증(401)과 구분한다
    FORBIDDEN(HttpStatus.FORBIDDEN, "AUTH_002", "Operation not permitted for this API key"),

    // Job 관련
    JOB_NOT_FOUND(HttpStatus.NOT_FOUND, "JOB_001", "Job not found"),
    JOB_EXECUTION_NOT_FOUND(HttpStatus.NOT_FOUND, "JOB_002", "Job execution not found"),
    JOB_ALREADY_RUNNING(HttpStatus.CONFLICT, "JOB_003", "Job is already running"),
    JOB_LAUNCH_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "JOB_004", "Failed to launch job"),

    // 배치 처리
    BATCH_PROCESS_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "BATCH_001", "Batch process failed"),

    // 일반
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "COMMON_001", "Invalid request"),
    ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON_002", "Item not found"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_999", "Internal server error"),
}