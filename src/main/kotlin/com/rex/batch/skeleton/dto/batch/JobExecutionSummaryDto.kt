package com.rex.batch.skeleton.dto.batch

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Job 실행 통계 요약 DTO
 *
 * 서비스 계층 간 실행 통계 데이터 전달에 사용됩니다.
 */
data class JobExecutionSummaryDto(
    val id: Long = 0,
    val jobName: String,
    val summaryDate: LocalDate,
    val summaryHour: Short? = null,
    val totalExecutions: Int = 0,
    val completedCount: Int = 0,
    val failedCount: Int = 0,
    val stoppedCount: Int = 0,
    val totalReadCount: Long = 0,
    val totalWriteCount: Long = 0,
    val totalSkipCount: Long = 0,
    val avgDurationMs: Long? = null,
    val minDurationMs: Long? = null,
    val maxDurationMs: Long? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(), // NOT NULL 컬럼 — persist 시 감사 리스너가 덮는 자리표시자
    val updatedAt: LocalDateTime = LocalDateTime.now(), // NOT NULL 컬럼 — 자리표시자
)