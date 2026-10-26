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

    // 전용 핸들러가 없는 Spring 예외(ErrorResponse)를 그 상태로 응답할 때 쓴다 — fromStatus
    PATH_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON_003", "Requested path not found"),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "COMMON_004", "Method not allowed"),
    NOT_ACCEPTABLE(HttpStatus.NOT_ACCEPTABLE, "COMMON_005", "Requested response format not available"),
    PAYLOAD_TOO_LARGE(HttpStatus.PAYLOAD_TOO_LARGE, "COMMON_006", "Request body too large"),
    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "COMMON_007", "Unsupported request content type"),
    SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "COMMON_008", "Service temporarily unavailable"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_999", "Internal server error"),
    ;

    companion object {
        /** 상태 기반 처리의 대표 코드 — 일반 코드에서만 고른다(404 는 JOB_NOT_FOUND 가 아니라 PATH_NOT_FOUND). 상태가 겹치면 초기화가 실패한다. */
        private val BY_STATUS: Map<Int, ErrorCode> =
            listOf(
                INVALID_REQUEST,
                PATH_NOT_FOUND,
                METHOD_NOT_ALLOWED,
                NOT_ACCEPTABLE,
                PAYLOAD_TOO_LARGE,
                UNSUPPORTED_MEDIA_TYPE,
                INTERNAL_ERROR,
                SERVICE_UNAVAILABLE,
            ).let { codes -> codes.associateBy { it.status.value() }.also { check(it.size == codes.size) { "BY_STATUS 에 같은 상태의 코드가 둘 있다" } } }

        /**
         * 상태에 맞는 코드. 목록 밖 상태는 4xx → [INVALID_REQUEST], 5xx → [INTERNAL_ERROR] 이고,
         * 응답 HTTP 상태도 그 코드의 상태가 된다 — 상태와 코드가 어긋난 응답은 나가지 않는다.
         */
        fun fromStatus(status: Int): ErrorCode = BY_STATUS[status] ?: if (status >= 500) INTERNAL_ERROR else INVALID_REQUEST
    }
}