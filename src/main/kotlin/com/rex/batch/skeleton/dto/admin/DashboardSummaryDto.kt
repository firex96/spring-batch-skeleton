package com.rex.batch.skeleton.dto.admin

import java.time.LocalDateTime

/** 대시보드 요약 통계 — `JobAdminBizService` 가 집계해 돌려주는 값. 응답은 `DashboardSummaryModel`. */
data class DashboardSummaryDto(
    val totalJobs: Int,
    val totalExecutionsToday: Int,
    val completedToday: Int,
    val failedToday: Int,
    val runningNow: Int,
    val scheduledJobs: Int,
    val lastUpdated: LocalDateTime,
)