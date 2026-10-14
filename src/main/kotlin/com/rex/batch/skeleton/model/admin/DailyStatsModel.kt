package com.rex.batch.skeleton.model.admin

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate

/**
 * 일별 통계 모델
 *
 * 일별 Job 실행 통계를 담습니다.
 */
data class DailyStatsModel(
    @field:JsonProperty("date")
    val date: LocalDate,
    @field:JsonProperty("totalExecutions")
    val totalExecutions: Int = 0,
    @field:JsonProperty("completedCount")
    val completedCount: Int = 0,
    @field:JsonProperty("failedCount")
    val failedCount: Int = 0,
    @field:JsonProperty("avgDurationMs")
    val avgDurationMs: Long? = null,
)