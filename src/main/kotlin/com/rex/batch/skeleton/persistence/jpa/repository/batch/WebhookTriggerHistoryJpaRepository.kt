package com.rex.batch.skeleton.persistence.jpa.repository.batch

import com.rex.batch.skeleton.persistence.jpa.entity.batch.WebhookTriggerHistoryJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

/**
 * Webhook 트리거 이력 JPA 리포지토리
 */
@Repository
interface WebhookTriggerHistoryJpaRepository : JpaRepository<WebhookTriggerHistoryJpaEntity, Long> {
    /**
     * Job 이름으로 트리거 이력 조회
     */
    fun findAllByJobName(jobName: String): List<WebhookTriggerHistoryJpaEntity>

    /**
     * Job Execution ID로 트리거 이력 조회
     */
    fun findByJobExecutionId(jobExecutionId: Long): WebhookTriggerHistoryJpaEntity?

    /**
     * Trace ID로 트리거 이력 조회
     */
    fun findByTraceId(traceId: String): WebhookTriggerHistoryJpaEntity?

    /**
     * 상태별 트리거 이력 조회
     */
    fun findAllByStatus(status: String): List<WebhookTriggerHistoryJpaEntity>

    /**
     * 트리거 시간 범위로 이력 조회
     */
    fun findAllByTriggeredAtBetween(
        startTime: LocalDateTime,
        endTime: LocalDateTime,
    ): List<WebhookTriggerHistoryJpaEntity>
}