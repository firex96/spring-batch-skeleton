package com.rex.batch.skeleton.persistence.jpa.service.batch

import com.rex.batch.skeleton.dto.batch.JobScheduleConfigDto
import com.rex.batch.skeleton.mapper.batch.JobScheduleConfigMapper
import com.rex.batch.skeleton.persistence.jpa.repository.batch.JobScheduleConfigJpaRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * Job 스케줄 설정 JPA 엔티티 서비스
 *
 * JPA를 통한 스케줄 설정 CRUD 작업을 처리합니다.
 */
@Transactional(readOnly = true, transactionManager = "batchTransactionManager")
@Service
class JobScheduleConfigJpaEntityService(
    private val repository: JobScheduleConfigJpaRepository,
    private val mapper: JobScheduleConfigMapper,
) {
    /**
     * ID로 스케줄 설정 조회
     */
    fun get(id: Long): JobScheduleConfigDto? =
        repository.findByIdOrNull(id)?.let {
            mapper.toDto(it)
        }

    /**
     * Job 이름으로 스케줄 설정 조회
     */
    fun getByJobName(jobName: String): JobScheduleConfigDto? =
        repository.findByJobName(jobName)?.let {
            mapper.toDto(it)
        }

    /**
     * 전체 스케줄 설정 조회
     */
    fun getAll(): List<JobScheduleConfigDto> =
        repository.findAll().let {
            mapper.toDtoList(it)
        }

    /**
     * 활성화된 스케줄 설정 조회
     */
    fun getAllEnabled(): List<JobScheduleConfigDto> =
        repository.findAllByIsEnabledTrue().let {
            mapper.toDtoList(it)
        }

    /**
     * 비활성화된 스케줄 설정 조회
     */
    fun getAllDisabled(): List<JobScheduleConfigDto> =
        repository.findAllByIsEnabledFalse().let {
            mapper.toDtoList(it)
        }

    /**
     * 다음 실행 시간이 특정 시간 이전인 스케줄 조회
     */
    fun getAllDueForExecution(nextFireTime: LocalDateTime): List<JobScheduleConfigDto> =
        repository.findAllByIsEnabledTrueAndNextFireTimeBefore(nextFireTime).let {
            mapper.toDtoList(it)
        }

    /**
     * 스케줄 설정 생성
     */
    @Transactional(transactionManager = "batchTransactionManager")
    fun create(dto: JobScheduleConfigDto): JobScheduleConfigDto {
        require(dto.id == 0L) { "신규 생성 시 ID는 0이어야 합니다." }

        return repository.save(mapper.toEntity(dto)).let {
            mapper.toDto(it)
        }
    }

    /**
     * 스케줄 설정 수정
     */
    @Transactional(transactionManager = "batchTransactionManager")
    fun update(dto: JobScheduleConfigDto): JobScheduleConfigDto {
        repository.findById(dto.id).orElseThrow {
            NoSuchElementException("JobScheduleConfig with id ${dto.id} not found")
        }

        return repository.save(mapper.toEntity(dto)).let {
            mapper.toDto(it)
        }
    }

    /**
     * 스케줄 설정 삭제
     */
    @Transactional(transactionManager = "batchTransactionManager")
    fun delete(id: Long) {
        repository.deleteById(id)
    }
}