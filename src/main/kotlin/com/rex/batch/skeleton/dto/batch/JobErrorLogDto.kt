package com.rex.batch.skeleton.dto.batch

import java.time.LocalDateTime

/**
 * Job 에러 로그 DTO
 *
 * 서비스 계층 간 에러 로그 데이터 전달에 사용됩니다.
 */
data class JobErrorLogDto(
    val id: Long = 0,
    val jobExecutionId: Long,
    val stepExecutionId: Long? = null,
    val jobName: String,
    val stepName: String? = null,
    val errorType: String,
    val errorCode: String? = null,
    val errorMessage: String,
    val stackTrace: String? = null,
    val itemInfo: String? = null,
    val occurredAt: LocalDateTime = LocalDateTime.now(),
    val createdAt: LocalDateTime = LocalDateTime.now(), // NOT NULL 컬럼 — persist 시 감사 리스너가 덮는 자리표시자
)