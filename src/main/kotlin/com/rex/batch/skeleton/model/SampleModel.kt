package com.rex.batch.skeleton.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

/**
 * 샘플 응답 모델
 *
 * API 응답이나 외부 시스템과의 통신에 사용됩니다.
 */
data class SampleModel(
    @field:JsonProperty("id")
    val id: Long,
    @field:JsonProperty("name")
    val name: String,
    @field:JsonProperty("status")
    val status: String,
    @field:JsonProperty("processedAt")
    val processedAt: LocalDateTime? = null,
    @field:JsonProperty("description")
    val description: String? = null,
)