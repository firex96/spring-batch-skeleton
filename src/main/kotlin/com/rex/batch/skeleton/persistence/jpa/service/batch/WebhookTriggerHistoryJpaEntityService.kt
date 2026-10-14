package com.rex.batch.skeleton.persistence.jpa.service.batch

import com.rex.batch.skeleton.dto.batch.WebhookTriggerHistoryDto
import com.rex.batch.skeleton.mapper.batch.WebhookTriggerHistoryMapper
import com.rex.batch.skeleton.persistence.jpa.repository.batch.WebhookTriggerHistoryJpaRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * Webhook 트리거 이력 JPA 엔티티 서비스
 *
 * JPA를 통한 Webhook 트리거 이력 CRUD 작업을 처리합니다.
 */
@Transactional(readOnly = true, transactionManager = "batchTransactionManager")
@Service
class WebhookTriggerHistoryJpaEntityService(
    private val repository: WebhookTriggerHistoryJpaRepository,
    private val mapper: WebhookTriggerHistoryMapper,
) {
    /**
     * ID로 트리거 이력 조회
     */
    fun get(id: Long): WebhookTriggerHistoryDto? =
        repository.findByIdOrNull(id)?.let {
            mapper.toDto(it)
        }

    /**
     * Job 이름으로 트리거 이력 조회
     */
    fun getAllByJobName(jobName: String): List<WebhookTriggerHistoryDto> =
        repository.findAllByJobName(jobName).let {
            mapper.toDtoList(it)
        }

    /**
     * Job Execution ID로 트리거 이력 조회
     */
    fun getByJobExecutionId(jobExecutionId: Long): WebhookTriggerHistoryDto? =
        repository.findByJobExecutionId(jobExecutionId)?.let {
            mapper.toDto(it)
        }

    /**
     * Trace ID로 트리거 이력 조회
     */
    fun getByTraceId(traceId: String): WebhookTriggerHistoryDto? =
        repository.findByTraceId(traceId)?.let {
            mapper.toDto(it)
        }

    /**
     * 상태별 트리거 이력 조회
     */
    fun getAllByStatus(status: String): List<WebhookTriggerHistoryDto> =
        repository.findAllByStatus(status).let {
            mapper.toDtoList(it)
        }

    /**
     * 트리거 시간 범위로 이력 조회
     */
    fun getAllByTriggeredAtBetween(
        startTime: LocalDateTime,
        endTime: LocalDateTime,
    ): List<WebhookTriggerHistoryDto> =
        repository.findAllByTriggeredAtBetween(startTime, endTime).let {
            mapper.toDtoList(it)
        }

    /**
     * 트리거 이력 생성
     */
    @Transactional(transactionManager = "batchTransactionManager")
    fun create(dto: WebhookTriggerHistoryDto): WebhookTriggerHistoryDto {
        require(dto.id == 0L) { "신규 생성 시 ID는 0이어야 합니다." }

        return repository.save(mapper.toEntity(dto)).let {
            mapper.toDto(it)
        }
    }

    /**
     * 트리거 이력 수정
     */
    @Transactional(transactionManager = "batchTransactionManager")
    fun update(dto: WebhookTriggerHistoryDto): WebhookTriggerHistoryDto {
        repository.findById(dto.id).orElseThrow {
            NoSuchElementException("WebhookTriggerHistory with id ${dto.id} not found")
        }

        return repository.save(mapper.toEntity(dto)).let {
            mapper.toDto(it)
        }
    }

    /**
     * 트리거 이력 삭제
     */
    @Transactional(transactionManager = "batchTransactionManager")
    fun delete(id: Long) {
        repository.deleteById(id)
    }
}