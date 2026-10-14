package com.rex.batch.skeleton.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

/**
 * API 에러 응답 모델
 */
data class ErrorResponseModel(
    /** 에러 코드 */
    @field:JsonProperty("code")
    val code: String,
    /** 에러 메시지 */
    @field:JsonProperty("message")
    val message: String,
    /** 상세 정보 */
    @field:JsonProperty("details")
    val details: String? = null,
    /** 발생 시간 */
    @field:JsonProperty("timestamp")
    val timestamp: LocalDateTime = LocalDateTime.now(),
)