package com.rex.batch.skeleton.persistence.jpa.repository.batch

import com.rex.batch.skeleton.persistence.jpa.entity.batch.JobNotificationHistoryJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

/**
 * Job 알림 발송 이력 JPA 리포지토리
 */
@Repository
interface JobNotificationHistoryJpaRepository : JpaRepository<JobNotificationHistoryJpaEntity, Long> {
    /**
     * Job Execution ID로 알림 이력 조회
     */
    fun findAllByJobExecutionId(jobExecutionId: Long): List<JobNotificationHistoryJpaEntity>

    /**
     * Job 이름으로 알림 이력 조회
     */
    fun findAllByJobName(jobName: String): List<JobNotificationHistoryJpaEntity>

    /**
     * 알림 유형별 이력 조회
     */
    fun findAllByNotificationType(notificationType: String): List<JobNotificationHistoryJpaEntity>

    /**
     * 상태별 알림 이력 조회
     */
    fun findAllByStatus(status: String): List<JobNotificationHistoryJpaEntity>

    /**
     * 재발송이 필요한 알림 조회 (실패 상태 + 재시도 횟수 미만)
     */
    fun findAllByStatusAndRetryCountLessThan(
        status: String,
        maxRetryCount: Int,
    ): List<JobNotificationHistoryJpaEntity>

    /**
     * 생성 시간 범위로 이력 조회
     */
    fun findAllByCreatedAtBetween(
        startTime: LocalDateTime,
        endTime: LocalDateTime,
    ): List<JobNotificationHistoryJpaEntity>
}