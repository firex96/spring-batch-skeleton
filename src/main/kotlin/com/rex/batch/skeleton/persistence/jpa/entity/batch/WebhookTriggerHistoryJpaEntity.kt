package com.rex.batch.skeleton.persistence.jpa.entity.batch

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

/**
 * Webhook 트리거 이력 JPA 엔티티
 *
 * Webhook을 통한 Job 트리거 이력을 저장합니다.
 */
@Entity
@Table(name = "WEBHOOK_TRIGGER_HISTORY")
class WebhookTriggerHistoryJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TRIGGER_HISTORY_ID")
    var id: Long = 0

    @Column(name = "JOB_NAME", nullable = false, length = 100)
    var jobName: String = ""

    @Column(name = "JOB_EXECUTION_ID")
    var jobExecutionId: Long? = null

    @Column(name = "TRACE_ID", nullable = false, length = 50)
    var traceId: String = ""

    @Column(name = "TRIGGER_SOURCE", nullable = false, length = 50)
    var triggerSource: String = ""

    @Column(name = "API_KEY_NAME", length = 50)
    var apiKeyName: String? = null

    @Column(name = "CLIENT_IP", length = 45)
    var clientIp: String? = null

    @Column(name = "REQUEST_PARAMETERS", columnDefinition = "TEXT")
    var requestParameters: String? = null

    @Column(name = "STATUS", nullable = false, length = 20)
    var status: String = ""

    @Column(name = "ERROR_MESSAGE", length = 2500)
    var errorMessage: String? = null

    @Column(name = "TRIGGERED_AT", nullable = false)
    var triggeredAt: LocalDateTime = LocalDateTime.now()

    @Column(name = "COMPLETED_AT")
    var completedAt: LocalDateTime? = null

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()

    @Column(name = "UPDATED_AT", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
}