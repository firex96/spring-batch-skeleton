package com.rex.batch.skeleton.model.webhook

import com.fasterxml.jackson.annotation.JsonProperty
/**
 * Job 트리거 요청 모델
 */
data class JobTriggerRequestModel(
    /** Job 파라미터 — 본문이 없거나 키가 없으면 null(요청 본문 자체가 선택이다) */
    @field:JsonProperty("parameters")
    val parameters: Map<String, String>?,
)