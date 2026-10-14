package com.rex.batch.skeleton.model.admin

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

/** Job 실행 정보 응답(관리자 API `/admin/api/executions`) — 필드는 `dto.webhook.JobExecutionDto` 와 동일(웹훅 응답 `JobStatusResponseModel` 과는 다른 계약). */
data class JobExecutionModel(
    @field:JsonProperty("jobExecutionId")
    val jobExecutionId: Long,
    @field:JsonProperty("jobName")
    val jobName: String,
    @field:JsonProperty("status")
    val status: String,
    @field:JsonProperty("traceId")
    val traceId: String?,
    @field:JsonProperty("startTime")
    val startTime: LocalDateTime?,
    @field:JsonProperty("endTime")
    val endTime: LocalDateTime?,
    @field:JsonProperty("exitCode")
    val exitCode: String,
    @field:JsonProperty("exitDescription")
    val exitDescription: String?,
    @field:JsonProperty("parameters")
    val parameters: Map<String, String>,
    @field:JsonProperty("stepExecutions")
    val stepExecutions: List<StepExecutionModel>,
)