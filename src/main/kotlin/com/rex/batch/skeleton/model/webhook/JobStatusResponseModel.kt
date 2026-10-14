package com.rex.batch.skeleton.model.webhook

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

/**
 * Job 상태 응답 모델
 */
data class JobStatusResponseModel(
    /** Job 실행 ID */
    @field:JsonProperty("jobExecutionId")
    val jobExecutionId: Long,
    /** Job 이름 */
    @field:JsonProperty("jobName")
    val jobName: String,
    /** 상태 */
    @field:JsonProperty("status")
    val status: String,
    /** 추적 ID */
    @field:JsonProperty("traceId")
    val traceId: String?,
    /** 시작 시간 */
    @field:JsonProperty("startTime")
    val startTime: LocalDateTime?,
    /** 종료 시간 */
    @field:JsonProperty("endTime")
    val endTime: LocalDateTime?,
    /** 종료 코드 */
    @field:JsonProperty("exitCode")
    val exitCode: String,
    /** 종료 설명 */
    @field:JsonProperty("exitDescription")
    val exitDescription: String?,
    /** Step 실행 정보 목록 */
    @field:JsonProperty("steps")
    val steps: List<StepStatusModel>,
)