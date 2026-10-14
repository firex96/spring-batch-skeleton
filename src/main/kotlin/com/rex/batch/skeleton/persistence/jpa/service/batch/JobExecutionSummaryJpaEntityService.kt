package com.rex.batch.skeleton.persistence.jpa.service.batch

import com.rex.batch.skeleton.dto.batch.JobExecutionSummaryDto
import com.rex.batch.skeleton.mapper.batch.JobExecutionSummaryMapper
import com.rex.batch.skeleton.persistence.jpa.repository.batch.JobExecutionSummaryJpaRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

/**
 * Job 실행 통계 요약 JPA 엔티티 서비스
 *
 * JPA를 통한 실행 통계 CRUD 작업을 처리합니다.
 */
@Transactional(readOnly = true, transactionManager = "batchTransactionManager")
@Service
class JobExecutionSummaryJpaEntityService(
    private val repository: JobExecutionSummaryJpaRepository,
    private val mapper: JobExecutionSummaryMapper,
) {
    /**
     * ID로 통계 요약 조회
     */
    fun get(id: Long): JobExecutionSummaryDto? =
        repository.findByIdOrNull(id)?.let {
            mapper.toDto(it)
        }

    /**
     * Job 이름으로 통계 요약 조회
     */
    fun getAllByJobName(jobName: String): List<JobExecutionSummaryDto> =
        repository.findAllByJobName(jobName).let {
            mapper.toDtoList(it)
        }

    /**
     * 특정 날짜의 통계 요약 조회
     */
    fun getAllBySummaryDate(summaryDate: LocalDate): List<JobExecutionSummaryDto> =
        repository.findAllBySummaryDate(summaryDate).let {
            mapper.toDtoList(it)
        }

    /**
     * Job 이름과 날짜로 일별 통계 조회
     */
    fun getDailySummary(
        jobName: String,
        summaryDate: LocalDate,
    ): JobExecutionSummaryDto? =
        repository.findByJobNameAndSummaryDateAndSummaryHourIsNull(jobName, summaryDate)?.let {
            mapper.toDto(it)
        }

    /**
     * Job 이름과 날짜, 시간으로 시간별 통계 조회
     */
    fun getHourlySummary(
        jobName: String,
        summaryDate: LocalDate,
        summaryHour: Short,
    ): JobExecutionSummaryDto? =
        repository.findByJobNameAndSummaryDateAndSummaryHour(jobName, summaryDate, summaryHour)?.let {
            mapper.toDto(it)
        }

    /**
     * 날짜 범위로 통계 요약 조회
     */
    fun getAllBySummaryDateBetween(
        startDate: LocalDate,
        endDate: LocalDate,
    ): List<JobExecutionSummaryDto> =
        repository.findAllBySummaryDateBetween(startDate, endDate).let {
            mapper.toDtoList(it)
        }

    /**
     * Job 이름과 날짜 범위로 통계 요약 조회
     */
    fun getAllByJobNameAndSummaryDateBetween(
        jobName: String,
        startDate: LocalDate,
        endDate: LocalDate,
    ): List<JobExecutionSummaryDto> =
        repository.findAllByJobNameAndSummaryDateBetween(jobName, startDate, endDate).let {
            mapper.toDtoList(it)
        }

    /**
     * 통계 요약 생성
     */
    @Transactional(transactionManager = "batchTransactionManager")
    fun create(dto: JobExecutionSummaryDto): JobExecutionSummaryDto {
        require(dto.id == 0L) { "신규 생성 시 ID는 0이어야 합니다." }

        return repository.save(mapper.toEntity(dto)).let {
            mapper.toDto(it)
        }
    }

    /**
     * 통계 요약 수정
     */
    @Transactional(transactionManager = "batchTransactionManager")
    fun update(dto: JobExecutionSummaryDto): JobExecutionSummaryDto {
        repository.findById(dto.id).orElseThrow {
            NoSuchElementException("JobExecutionSummary with id ${dto.id} not found")
        }

        return repository.save(mapper.toEntity(dto)).let {
            mapper.toDto(it)
        }
    }

    /**
     * 통계 요약 삭제
     */
    @Transactional(transactionManager = "batchTransactionManager")
    fun delete(id: Long) {
        repository.deleteById(id)
    }
}