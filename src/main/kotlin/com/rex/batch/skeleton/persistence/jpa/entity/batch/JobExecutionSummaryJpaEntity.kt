package com.rex.batch.skeleton.persistence.jpa.entity.batch

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Job 실행 통계 요약 JPA 엔티티
 *
 * Job 실행 통계 요약 (대시보드/리포팅용)을 저장합니다.
 */
@Entity
@Table(name = "JOB_EXECUTION_SUMMARY")
class JobExecutionSummaryJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SUMMARY_ID")
    var id: Long = 0

    @Column(name = "JOB_NAME", nullable = false, length = 100)
    var jobName: String = ""

    @Column(name = "SUMMARY_DATE", nullable = false)
    var summaryDate: LocalDate = LocalDate.now()

    @Column(name = "SUMMARY_HOUR")
    var summaryHour: Short? = null

    @Column(name = "TOTAL_EXECUTIONS", nullable = false)
    var totalExecutions: Int = 0

    @Column(name = "COMPLETED_COUNT", nullable = false)
    var completedCount: Int = 0

    @Column(name = "FAILED_COUNT", nullable = false)
    var failedCount: Int = 0

    @Column(name = "STOPPED_COUNT", nullable = false)
    var stoppedCount: Int = 0

    @Column(name = "TOTAL_READ_COUNT", nullable = false)
    var totalReadCount: Long = 0

    @Column(name = "TOTAL_WRITE_COUNT", nullable = false)
    var totalWriteCount: Long = 0

    @Column(name = "TOTAL_SKIP_COUNT", nullable = false)
    var totalSkipCount: Long = 0

    @Column(name = "AVG_DURATION_MS")
    var avgDurationMs: Long? = null

    @Column(name = "MIN_DURATION_MS")
    var minDurationMs: Long? = null

    @Column(name = "MAX_DURATION_MS")
    var maxDurationMs: Long? = null

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()

    @Column(name = "UPDATED_AT", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
}