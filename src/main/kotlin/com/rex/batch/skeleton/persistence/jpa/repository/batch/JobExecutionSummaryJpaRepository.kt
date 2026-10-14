package com.rex.batch.skeleton.persistence.jpa.repository.batch

import com.rex.batch.skeleton.persistence.jpa.entity.batch.JobExecutionSummaryJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate

/**
 * Job 실행 통계 요약 JPA 리포지토리
 */
@Repository
interface JobExecutionSummaryJpaRepository : JpaRepository<JobExecutionSummaryJpaEntity, Long> {
    /**
     * Job 이름으로 통계 요약 조회
     */
    fun findAllByJobName(jobName: String): List<JobExecutionSummaryJpaEntity>

    /**
     * 특정 날짜의 통계 요약 조회
     */
    fun findAllBySummaryDate(summaryDate: LocalDate): List<JobExecutionSummaryJpaEntity>

    /**
     * Job 이름과 날짜로 일별 통계 조회 (summaryHour가 null인 경우)
     */
    fun findByJobNameAndSummaryDateAndSummaryHourIsNull(
        jobName: String,
        summaryDate: LocalDate,
    ): JobExecutionSummaryJpaEntity?

    /**
     * Job 이름과 날짜, 시간으로 시간별 통계 조회
     */
    fun findByJobNameAndSummaryDateAndSummaryHour(
        jobName: String,
        summaryDate: LocalDate,
        summaryHour: Short,
    ): JobExecutionSummaryJpaEntity?

    /**
     * 날짜 범위로 통계 요약 조회
     */
    fun findAllBySummaryDateBetween(
        startDate: LocalDate,
        endDate: LocalDate,
    ): List<JobExecutionSummaryJpaEntity>

    /**
     * Job 이름과 날짜 범위로 통계 요약 조회
     */
    fun findAllByJobNameAndSummaryDateBetween(
        jobName: String,
        startDate: LocalDate,
        endDate: LocalDate,
    ): List<JobExecutionSummaryJpaEntity>
}