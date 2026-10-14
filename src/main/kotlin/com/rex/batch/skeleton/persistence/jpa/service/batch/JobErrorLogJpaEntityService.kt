package com.rex.batch.skeleton.persistence.jpa.service.batch

import com.rex.batch.skeleton.dto.batch.JobErrorLogDto
import com.rex.batch.skeleton.mapper.batch.JobErrorLogMapper
import com.rex.batch.skeleton.persistence.jpa.repository.batch.JobErrorLogJpaRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * Job 에러 로그 JPA 엔티티 서비스
 *
 * JPA를 통한 에러 로그 CRUD 작업을 처리합니다.
 */
@Transactional(readOnly = true, transactionManager = "batchTransactionManager")
@Service
class JobErrorLogJpaEntityService(
    private val repository: JobErrorLogJpaRepository,
    private val mapper: JobErrorLogMapper,
) {
    /**
     * ID로 에러 로그 조회
     */
    fun get(id: Long): JobErrorLogDto? =
        repository.findByIdOrNull(id)?.let {
            mapper.toDto(it)
        }

    /**
     * Job Execution ID로 에러 로그 조회
     */
    fun getAllByJobExecutionId(jobExecutionId: Long): List<JobErrorLogDto> =
        repository.findAllByJobExecutionId(jobExecutionId).let {
            mapper.toDtoList(it)
        }

    /**
     * Step Execution ID로 에러 로그 조회
     */
    fun getAllByStepExecutionId(stepExecutionId: Long): List<JobErrorLogDto> =
        repository.findAllByStepExecutionId(stepExecutionId).let {
            mapper.toDtoList(it)
        }

    /**
     * Job 이름으로 에러 로그 조회
     */
    fun getAllByJobName(jobName: String): List<JobErrorLogDto> =
        repository.findAllByJobName(jobName).let {
            mapper.toDtoList(it)
        }

    /**
     * 에러 유형별 로그 조회
     */
    fun getAllByErrorType(errorType: String): List<JobErrorLogDto> =
        repository.findAllByErrorType(errorType).let {
            mapper.toDtoList(it)
        }

    /**
     * 발생 시간 범위로 에러 로그 조회
     */
    fun getAllByOccurredAtBetween(
        startTime: LocalDateTime,
        endTime: LocalDateTime,
    ): List<JobErrorLogDto> =
        repository.findAllByOccurredAtBetween(startTime, endTime).let {
            mapper.toDtoList(it)
        }

    /**
     * Job 이름과 발생 시간 범위로 에러 로그 조회
     */
    fun getAllByJobNameAndOccurredAtBetween(
        jobName: String,
        startTime: LocalDateTime,
        endTime: LocalDateTime,
    ): List<JobErrorLogDto> =
        repository.findAllByJobNameAndOccurredAtBetween(jobName, startTime, endTime).let {
            mapper.toDtoList(it)
        }

    /**
     * 에러 로그 생성
     */
    @Transactional(transactionManager = "batchTransactionManager")
    fun create(dto: JobErrorLogDto): JobErrorLogDto {
        require(dto.id == 0L) { "신규 생성 시 ID는 0이어야 합니다." }

        return repository.save(mapper.toEntity(dto)).let {
            mapper.toDto(it)
        }
    }

    /**
     * 에러 로그 삭제
     */
    @Transactional(transactionManager = "batchTransactionManager")
    fun delete(id: Long) {
        repository.deleteById(id)
    }
}