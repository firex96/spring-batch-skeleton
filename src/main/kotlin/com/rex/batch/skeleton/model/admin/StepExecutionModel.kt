package com.rex.batch.skeleton.model.admin

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

/** Step 실행 정보 응답(관리자 API) — 필드는 `dto.webhook.StepExecutionDto` 와 동일. */
data class StepExecutionModel(
    @field:JsonProperty("stepExecutionId")
    val stepExecutionId: Long,
    @field:JsonProperty("stepName")
    val stepName: String,
    @field:JsonProperty("status")
    val status: String,
    @field:JsonProperty("readCount")
    val readCount: Long,
    @field:JsonProperty("writeCount")
    val writeCount: Long,
    @field:JsonProperty("commitCount")
    val commitCount: Long,
    @field:JsonProperty("rollbackCount")
    val rollbackCount: Long,
    @field:JsonProperty("filterCount")
    val filterCount: Long,
    @field:JsonProperty("skipCount")
    val skipCount: Long,
    @field:JsonProperty("startTime")
    val startTime: LocalDateTime?,
    @field:JsonProperty("endTime")
    val endTime: LocalDateTime?,
    @field:JsonProperty("exitCode")
    val exitCode: String,
    @field:JsonProperty("exitDescription")
    val exitDescription: String?,
)