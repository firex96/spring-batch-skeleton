package com.rex.batch.skeleton.model.webhook

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

/**
 * Job 트리거 응답 모델
 */
data class JobTriggerResponseModel(
    /** Job 실행 ID */
    @field:JsonProperty("jobExecutionId")
    val jobExecutionId: Long,
    /** Job 이름 */
    @field:JsonProperty("jobName")
    val jobName: String,
    /** 추적 ID */
    @field:JsonProperty("traceId")
    val traceId: String,
    /** 트리거 시간 */
    @field:JsonProperty("triggeredAt")
    val triggeredAt: LocalDateTime,
)