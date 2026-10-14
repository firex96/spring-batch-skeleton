package com.rex.batch.skeleton.dto.batch

import java.time.LocalDateTime

/**
 * Job 스케줄 설정 DTO
 *
 * 서비스 계층 간 스케줄 설정 데이터 전달에 사용됩니다.
 */
data class JobScheduleConfigDto(
    val id: Long = 0L,
    val jobName: String,
    val cronExpression: String,
    val description: String? = null,
    val isEnabled: Boolean = true,
    val timezone: String = "Asia/Seoul",
    val lastTriggeredAt: LocalDateTime? = null,
    val nextFireTime: LocalDateTime? = null,
    val createdBy: Long = 0L,
    val updatedBy: Long = 0L,
    val createdAt: LocalDateTime = LocalDateTime.now(), // NOT NULL 컬럼 — persist 시 감사 리스너가 덮는 자리표시자
    val updatedAt: LocalDateTime = LocalDateTime.now(), // NOT NULL 컬럼 — 자리표시자
)