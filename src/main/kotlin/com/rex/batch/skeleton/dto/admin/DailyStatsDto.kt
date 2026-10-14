package com.rex.batch.skeleton.dto.admin

import java.time.LocalDate

/** 일별 Job 실행 통계 — 데이터가 없는 날짜는 0 으로 채운다. 응답은 `DailyStatsModel`. */
data class DailyStatsDto(
    val date: LocalDate,
    val totalExecutions: Int = 0,
    val completedCount: Int = 0,
    val failedCount: Int = 0,
    val avgDurationMs: Long? = null,
)