package com.rex.batch.skeleton.persistence.jpa.service.batch

import com.rex.batch.skeleton.dto.batch.JobNotificationHistoryDto
import com.rex.batch.skeleton.mapper.batch.JobNotificationHistoryMapper
import com.rex.batch.skeleton.persistence.jpa.repository.batch.JobNotificationHistoryJpaRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * Job 알림 발송 이력 JPA 엔티티 서비스
 *
 * JPA를 통한 알림 발송 이력 CRUD 작업을 처리합니다.
 */
@Transactional(readOnly = true, transactionManager = "batchTransactionManager")
@Service
class JobNotificationHistoryJpaEntityService(
    private val repository: JobNotificationHistoryJpaRepository,
    private val mapper: JobNotificationHistoryMapper,
) {
    /**
     * ID로 알림 이력 조회
     */
    fun get(id: Long): JobNotificationHistoryDto? =
        repository.findByIdOrNull(id)?.let {
            mapper.toDto(it)
        }

    /**
     * Job Execution ID로 알림 이력 조회
     */
    fun getAllByJobExecutionId(jobExecutionId: Long): List<JobNotificationHistoryDto> =
        repository.findAllByJobExecutionId(jobExecutionId).let {
            mapper.toDtoList(it)
        }

    /**
     * Job 이름으로 알림 이력 조회
     */
    fun getAllByJobName(jobName: String): List<JobNotificationHistoryDto> =
        repository.findAllByJobName(jobName).let {
            mapper.toDtoList(it)
        }

    /**
     * 알림 유형별 이력 조회
     */
    fun getAllByNotificationType(notificationType: String): List<JobNotificationHistoryDto> =
        repository.findAllByNotificationType(notificationType).let {
            mapper.toDtoList(it)
        }

    /**
     * 상태별 알림 이력 조회
     */
    fun getAllByStatus(status: String): List<JobNotificationHistoryDto> =
        repository.findAllByStatus(status).let {
            mapper.toDtoList(it)
        }

    /**
     * 재발송이 필요한 알림 조회
     */
    fun getAllPendingRetry(
        status: String,
        maxRetryCount: Int,
    ): List<JobNotificationHistoryDto> =
        repository.findAllByStatusAndRetryCountLessThan(status, maxRetryCount).let {
            mapper.toDtoList(it)
        }

    /**
     * 생성 시간 범위로 이력 조회
     */
    fun getAllByCreatedAtBetween(
        startTime: LocalDateTime,
        endTime: LocalDateTime,
    ): List<JobNotificationHistoryDto> =
        repository.findAllByCreatedAtBetween(startTime, endTime).let {
            mapper.toDtoList(it)
        }

    /**
     * 알림 이력 생성
     */
    @Transactional(transactionManager = "batchTransactionManager")
    fun create(dto: JobNotificationHistoryDto): JobNotificationHistoryDto {
        require(dto.id == 0L) { "신규 생성 시 ID는 0이어야 합니다." }

        return repository.save(mapper.toEntity(dto)).let {
            mapper.toDto(it)
        }
    }

    /**
     * 알림 이력 수정
     */
    @Transactional(transactionManager = "batchTransactionManager")
    fun update(dto: JobNotificationHistoryDto): JobNotificationHistoryDto {
        repository.findById(dto.id).orElseThrow {
            NoSuchElementException("JobNotificationHistory with id ${dto.id} not found")
        }

        return repository.save(mapper.toEntity(dto)).let {
            mapper.toDto(it)
        }
    }

    /**
     * 알림 이력 삭제
     */
    @Transactional(transactionManager = "batchTransactionManager")
    fun delete(id: Long) {
        repository.deleteById(id)
    }
}