package com.rex.batch.skeleton.dto.batch

import java.time.LocalDateTime

/**
 * Job 알림 발송 이력 DTO
 *
 * 서비스 계층 간 알림 이력 데이터 전달에 사용됩니다.
 */
data class JobNotificationHistoryDto(
    val id: Long = 0,
    val jobExecutionId: Long,
    val jobName: String,
    val notificationType: String,
    val notificationEvent: String,
    val recipient: String,
    val subject: String? = null,
    val content: String? = null,
    val status: String,
    val errorMessage: String? = null,
    val retryCount: Int = 0,
    val sentAt: LocalDateTime? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(), // NOT NULL 컬럼 — persist 시 감사 리스너가 덮는 자리표시자
    val updatedAt: LocalDateTime = LocalDateTime.now(), // NOT NULL 컬럼 — 자리표시자
)