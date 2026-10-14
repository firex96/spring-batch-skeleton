package com.rex.batch.skeleton.dto

import java.time.LocalDateTime

/**
 * 샘플 데이터 전송 객체 (DTO)
 *
 * 서비스 계층 간 데이터 전달에 사용됩니다.
 */
data class SampleDto(
    val id: Long = 0L,
    val name: String,
    val status: String = "PENDING",
    val processedAt: LocalDateTime? = null,
    val description: String? = null,
    val createdBy: Long = 0L,
    val createdAt: LocalDateTime = LocalDateTime.now(), // sample.created_at NOT NULL — persist 시 감사 리스너가 덮는 자리표시자
    val updatedBy: Long = 0L,
    val updatedAt: LocalDateTime = LocalDateTime.now(), // sample.updated_at NOT NULL — 자리표시자
)