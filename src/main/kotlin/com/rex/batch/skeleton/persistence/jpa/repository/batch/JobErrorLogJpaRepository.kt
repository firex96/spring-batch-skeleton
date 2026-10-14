package com.rex.batch.skeleton.persistence.jpa.repository.batch

import com.rex.batch.skeleton.persistence.jpa.entity.batch.JobErrorLogJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

/**
 * Job 에러 로그 JPA 리포지토리
 */
@Repository
interface JobErrorLogJpaRepository : JpaRepository<JobErrorLogJpaEntity, Long> {
    /**
     * Job Execution ID로 에러 로그 조회
     */
    fun findAllByJobExecutionId(jobExecutionId: Long): List<JobErrorLogJpaEntity>

    /**
     * Step Execution ID로 에러 로그 조회
     */
    fun findAllByStepExecutionId(stepExecutionId: Long): List<JobErrorLogJpaEntity>

    /**
     * Job 이름으로 에러 로그 조회
     */
    fun findAllByJobName(jobName: String): List<JobErrorLogJpaEntity>

    /**
     * 에러 유형별 로그 조회
     */
    fun findAllByErrorType(errorType: String): List<JobErrorLogJpaEntity>

    /**
     * 발생 시간 범위로 에러 로그 조회
     */
    fun findAllByOccurredAtBetween(
        startTime: LocalDateTime,
        endTime: LocalDateTime,
    ): List<JobErrorLogJpaEntity>

    /**
     * Job 이름과 발생 시간 범위로 에러 로그 조회
     */
    fun findAllByJobNameAndOccurredAtBetween(
        jobName: String,
        startTime: LocalDateTime,
        endTime: LocalDateTime,
    ): List<JobErrorLogJpaEntity>
}