package com.rex.batch.skeleton.dto.batch

import java.time.LocalDateTime

/**
 * Webhook 트리거 이력 DTO
 *
 * 서비스 계층 간 Webhook 트리거 이력 데이터 전달에 사용됩니다.
 */
data class WebhookTriggerHistoryDto(
    val id: Long = 0,
    val jobName: String,
    val jobExecutionId: Long? = null,
    val traceId: String,
    val triggerSource: String,
    val apiKeyName: String? = null,
    val clientIp: String? = null,
    val requestParameters: String? = null,
    val status: String,
    val errorMessage: String? = null,
    val triggeredAt: LocalDateTime = LocalDateTime.now(),
    val completedAt: LocalDateTime? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(), // NOT NULL 컬럼 — persist 시 감사 리스너가 덮는 자리표시자
    val updatedAt: LocalDateTime = LocalDateTime.now(), // NOT NULL 컬럼 — 자리표시자
)