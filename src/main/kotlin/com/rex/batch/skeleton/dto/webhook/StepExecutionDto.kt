package com.rex.batch.skeleton.dto.webhook

import java.time.LocalDateTime

/**
 * Step 실행 정보 DTO
 */
data class StepExecutionDto(
    val stepExecutionId: Long,
    val stepName: String,
    val status: String,
    val readCount: Long,
    val writeCount: Long,
    val commitCount: Long,
    val rollbackCount: Long,
    val filterCount: Long,
    val skipCount: Long,
    val startTime: LocalDateTime?,
    val endTime: LocalDateTime?,
    val exitCode: String,
    val exitDescription: String?,
)