package com.rex.batch.skeleton.model.webhook

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

/**
 * Step 상태 모델
 */
data class StepStatusModel(
    /** Step 실행 ID */
    @field:JsonProperty("stepExecutionId")
    val stepExecutionId: Long,
    /** Step 이름 */
    @field:JsonProperty("stepName")
    val stepName: String,
    /** 상태 */
    @field:JsonProperty("status")
    val status: String,
    /** 읽기 건수 */
    @field:JsonProperty("readCount")
    val readCount: Long,
    /** 쓰기 건수 */
    @field:JsonProperty("writeCount")
    val writeCount: Long,
    /** 커밋 건수 */
    @field:JsonProperty("commitCount")
    val commitCount: Long,
    /** 롤백 건수 */
    @field:JsonProperty("rollbackCount")
    val rollbackCount: Long,
    /** 필터 건수 */
    @field:JsonProperty("filterCount")
    val filterCount: Long,
    /** 스킵 건수 */
    @field:JsonProperty("skipCount")
    val skipCount: Long,
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
)