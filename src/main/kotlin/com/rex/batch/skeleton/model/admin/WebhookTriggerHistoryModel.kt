package com.rex.batch.skeleton.model.admin

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

/** 웹훅 트리거 이력 응답(`/admin/api/webhooks`) — 필드는 `dto.batch.WebhookTriggerHistoryDto` 와 동일. */
data class WebhookTriggerHistoryModel(
    @field:JsonProperty("id")
    val id: Long,
    @field:JsonProperty("jobName")
    val jobName: String,
    @field:JsonProperty("jobExecutionId")
    val jobExecutionId: Long?,
    @field:JsonProperty("traceId")
    val traceId: String,
    @field:JsonProperty("triggerSource")
    val triggerSource: String,
    @field:JsonProperty("apiKeyName")
    val apiKeyName: String?,
    @field:JsonProperty("clientIp")
    val clientIp: String?,
    @field:JsonProperty("requestParameters")
    val requestParameters: String?,
    @field:JsonProperty("status")
    val status: String,
    @field:JsonProperty("errorMessage")
    val errorMessage: String?,
    @field:JsonProperty("triggeredAt")
    val triggeredAt: LocalDateTime,
    @field:JsonProperty("completedAt")
    val completedAt: LocalDateTime?,
    @field:JsonProperty("createdAt")
    val createdAt: LocalDateTime,
    @field:JsonProperty("updatedAt")
    val updatedAt: LocalDateTime,
)