package com.rex.batch.skeleton.persistence.jpa.entity.batch

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

/**
 * Job 에러 로그 JPA 엔티티
 *
 * Job/Step 실행 중 발생한 상세 에러 로그를 저장합니다.
 */
@Entity
@Table(name = "JOB_ERROR_LOG")
class JobErrorLogJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ERROR_LOG_ID")
    var id: Long = 0

    @Column(name = "JOB_EXECUTION_ID", nullable = false)
    var jobExecutionId: Long = 0

    @Column(name = "STEP_EXECUTION_ID")
    var stepExecutionId: Long? = null

    @Column(name = "JOB_NAME", nullable = false, length = 100)
    var jobName: String = ""

    @Column(name = "STEP_NAME", length = 100)
    var stepName: String? = null

    @Column(name = "ERROR_TYPE", nullable = false, length = 100)
    var errorType: String = ""

    @Column(name = "ERROR_CODE", length = 50)
    var errorCode: String? = null

    @Column(name = "ERROR_MESSAGE", nullable = false, length = 2500)
    var errorMessage: String = ""

    @Column(name = "STACK_TRACE", columnDefinition = "TEXT")
    var stackTrace: String? = null

    @Column(name = "ITEM_INFO", columnDefinition = "TEXT")
    var itemInfo: String? = null

    @Column(name = "OCCURRED_AT", nullable = false)
    var occurredAt: LocalDateTime = LocalDateTime.now()

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
}