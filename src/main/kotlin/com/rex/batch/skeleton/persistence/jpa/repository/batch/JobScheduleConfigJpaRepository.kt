package com.rex.batch.skeleton.persistence.jpa.repository.batch

import com.rex.batch.skeleton.persistence.jpa.entity.batch.JobScheduleConfigJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

/**
 * Job 스케줄 설정 JPA 리포지토리
 */
@Repository
interface JobScheduleConfigJpaRepository : JpaRepository<JobScheduleConfigJpaEntity, Long> {
    /**
     * Job 이름으로 스케줄 설정 조회
     */
    fun findByJobName(jobName: String): JobScheduleConfigJpaEntity?

    /**
     * 활성화된 스케줄 설정 조회
     */
    fun findAllByIsEnabledTrue(): List<JobScheduleConfigJpaEntity>

    /**
     * 비활성화된 스케줄 설정 조회
     */
    fun findAllByIsEnabledFalse(): List<JobScheduleConfigJpaEntity>

    /**
     * 다음 실행 시간이 특정 시간 이전인 스케줄 조회
     */
    fun findAllByIsEnabledTrueAndNextFireTimeBefore(
        nextFireTime: LocalDateTime,
    ): List<JobScheduleConfigJpaEntity>
}