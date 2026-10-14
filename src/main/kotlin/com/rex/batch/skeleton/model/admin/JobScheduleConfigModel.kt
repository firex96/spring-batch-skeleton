package com.rex.batch.skeleton.model.admin

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

/** Job 스케줄 설정 응답(`/admin/api/schedules`) — 필드는 `dto.batch.JobScheduleConfigDto` 와 동일. */
data class JobScheduleConfigModel(
    @field:JsonProperty("id")
    val id: Long,
    @field:JsonProperty("jobName")
    val jobName: String,
    @field:JsonProperty("cronExpression")
    val cronExpression: String,
    @field:JsonProperty("description")
    val description: String?,
    @field:JsonProperty("isEnabled")
    val isEnabled: Boolean,
    @field:JsonProperty("timezone")
    val timezone: String,
    @field:JsonProperty("lastTriggeredAt")
    val lastTriggeredAt: LocalDateTime?,
    @field:JsonProperty("nextFireTime")
    val nextFireTime: LocalDateTime?,
    @field:JsonProperty("createdBy")
    val createdBy: Long,
    @field:JsonProperty("updatedBy")
    val updatedBy: Long,
    @field:JsonProperty("createdAt")
    val createdAt: LocalDateTime,
    @field:JsonProperty("updatedAt")
    val updatedAt: LocalDateTime,
)