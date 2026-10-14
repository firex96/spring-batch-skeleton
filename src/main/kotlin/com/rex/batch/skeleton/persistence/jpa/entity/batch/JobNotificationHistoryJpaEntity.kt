package com.rex.batch.skeleton.persistence.jpa.entity.batch

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

/**
 * Job 알림 발송 이력 JPA 엔티티
 *
 * Job 완료/실패 알림 발송 이력을 저장합니다.
 */
@Entity
@Table(name = "JOB_NOTIFICATION_HISTORY")
class JobNotificationHistoryJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "NOTIFICATION_ID")
    var id: Long = 0

    @Column(name = "JOB_EXECUTION_ID", nullable = false)
    var jobExecutionId: Long = 0

    @Column(name = "JOB_NAME", nullable = false, length = 100)
    var jobName: String = ""

    @Column(name = "NOTIFICATION_TYPE", nullable = false, length = 30)
    var notificationType: String = ""

    @Column(name = "NOTIFICATION_EVENT", nullable = false, length = 30)
    var notificationEvent: String = ""

    @Column(name = "RECIPIENT", nullable = false, length = 500)
    var recipient: String = ""

    @Column(name = "SUBJECT", length = 500)
    var subject: String? = null

    @Column(name = "CONTENT", columnDefinition = "TEXT")
    var content: String? = null

    @Column(name = "STATUS", nullable = false, length = 20)
    var status: String = ""

    @Column(name = "ERROR_MESSAGE", length = 2500)
    var errorMessage: String? = null

    @Column(name = "RETRY_COUNT", nullable = false)
    var retryCount: Int = 0

    @Column(name = "SENT_AT")
    var sentAt: LocalDateTime? = null

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()

    @Column(name = "UPDATED_AT", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
}