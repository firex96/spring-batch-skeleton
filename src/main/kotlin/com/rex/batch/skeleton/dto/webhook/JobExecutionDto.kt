package com.rex.batch.skeleton.dto.webhook

import java.time.LocalDateTime

/**
 * Job 실행 정보 DTO
 */
data class JobExecutionDto(
    val jobExecutionId: Long,
    val jobName: String,
    val status: String,
    val traceId: String?,
    val startTime: LocalDateTime?,
    val endTime: LocalDateTime?,
    val exitCode: String,
    val exitDescription: String?,
    val parameters: Map<String, String>,
    val stepExecutions: List<StepExecutionDto>,
)