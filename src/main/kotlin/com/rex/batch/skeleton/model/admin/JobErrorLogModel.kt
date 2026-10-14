package com.rex.batch.skeleton.model.admin

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

/** Job 오류 로그 응답(`/admin/api/errors`) — 필드는 `dto.batch.JobErrorLogDto` 와 동일. */
data class JobErrorLogModel(
    @field:JsonProperty("id")
    val id: Long,
    @field:JsonProperty("jobExecutionId")
    val jobExecutionId: Long,
    @field:JsonProperty("stepExecutionId")
    val stepExecutionId: Long?,
    @field:JsonProperty("jobName")
    val jobName: String,
    @field:JsonProperty("stepName")
    val stepName: String?,
    @field:JsonProperty("errorType")
    val errorType: String,
    @field:JsonProperty("errorCode")
    val errorCode: String?,
    @field:JsonProperty("errorMessage")
    val errorMessage: String,
    @field:JsonProperty("stackTrace")
    val stackTrace: String?,
    @field:JsonProperty("itemInfo")
    val itemInfo: String?,
    @field:JsonProperty("occurredAt")
    val occurredAt: LocalDateTime,
    @field:JsonProperty("createdAt")
    val createdAt: LocalDateTime,
)