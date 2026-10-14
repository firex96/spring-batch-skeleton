package com.rex.batch.skeleton.constant

/**
 * 애플리케이션 전역 상수 정의
 */
object Constant {
    // ========== MDC Keys ==========
    /** 요청 추적 ID */
    const val KEY_TRACE_ID = "traceId"

    /** Job 이름 */
    const val KEY_JOB_NAME = "jobName"

    /** Job 실행 ID */
    const val KEY_JOB_EXECUTION_ID = "jobExecutionId"

    /** Step 이름 */
    const val KEY_STEP_NAME = "stepName"

    /** 사용자 ID */
    const val KEY_USER_ID = "userId"

    /** 인바운드/아웃바운드 traceId 헤더 이름. 형제 API 스켈레톤과 같은 값이어야 추적이 이어진다. */
    const val HEADER_TRACE_ID = "X-Trace-Id"

    /** 인바운드 userId 헤더 이름. */
    const val HEADER_USER_ID = "X-User-Id"

    // ========== System Constants ==========

    /** 시스템 사용자 ID (배치, 스케줄러 등) */
    const val SYSTEM_USER_ID = 0L

    // ========== SQL Constants ==========

    /** SQL IN 절 최대 아이템 수 */
    const val IN_CONDITION_ITEM_SIZE = 1_000

    // ========== Batch Constants ==========
    object Batch {
        /** 기본 청크 사이즈 */
        const val DEFAULT_CHUNK_SIZE = 100

        /** 기본 스킵 제한 */
        const val DEFAULT_SKIP_LIMIT = 10

        /** 기본 재시도 제한 */
        const val DEFAULT_RETRY_LIMIT = 3
    }
}