package com.rex.batch.skeleton.model.admin

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

/**
 * 대시보드 요약 통계 모델
 *
 * Admin 대시보드에 표시할 통계 데이터를 담습니다.
 */
data class DashboardSummaryModel(
    @field:JsonProperty("totalJobs")
    val totalJobs: Int,
    @field:JsonProperty("totalExecutionsToday")
    val totalExecutionsToday: Int,
    @field:JsonProperty("completedToday")
    val completedToday: Int,
    @field:JsonProperty("failedToday")
    val failedToday: Int,
    @field:JsonProperty("runningNow")
    val runningNow: Int,
    @field:JsonProperty("scheduledJobs")
    val scheduledJobs: Int,
    @field:JsonProperty("lastUpdated")
    val lastUpdated: LocalDateTime,
)