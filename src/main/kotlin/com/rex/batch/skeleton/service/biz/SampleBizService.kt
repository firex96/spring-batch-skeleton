package com.rex.batch.skeleton.service.biz

import com.rex.batch.skeleton.dto.SampleDto
import com.rex.batch.skeleton.persistence.jdbc.service.main.SampleJdbcEntityService
import com.rex.batch.skeleton.persistence.jpa.service.main.SampleJpaEntityService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 샘플 비즈니스 서비스
 *
 * 비즈니스 로직을 처리하고 트랜잭션 경계를 설정합니다.
 * JPA와 JDBC EntityService를 조합하여 사용합니다.
 */
@Transactional(readOnly = true, transactionManager = "mainTransactionManager")
@Service
class SampleBizService(
    private val sampleJpaEntityService: SampleJpaEntityService,
    private val sampleJdbcEntityService: SampleJdbcEntityService,
) {
    /**
     * ID로 샘플 조회
     */
    fun find(id: Long): SampleDto? = sampleJpaEntityService.get(id)

    /**
     * 상태별 샘플 목록 조회
     */
    fun findAllByStatus(status: String): List<SampleDto> = sampleJpaEntityService.getAllByStatus(status)

    /**
     * 처리되지 않은 샘플 목록 조회
     */
    fun findAllUnprocessed(): List<SampleDto> = sampleJpaEntityService.getAllUnprocessed()

    /**
     * 처리 대상 샘플 ID 목록 조회 (JDBC 사용)
     */
    fun findPendingSampleIds(): List<Long> = sampleJdbcEntityService.getPendingSampleIds()

    /**
     * 샘플 생성
     */
    @Transactional(transactionManager = "mainTransactionManager")
    fun add(dto: SampleDto): SampleDto = sampleJpaEntityService.create(dto)

    /**
     * 샘플 수정
     */
    @Transactional(transactionManager = "mainTransactionManager")
    fun modify(dto: SampleDto): SampleDto = sampleJpaEntityService.update(dto)

    /**
     * 샘플 삭제
     */
    @Transactional(transactionManager = "mainTransactionManager")
    fun remove(id: Long) = sampleJpaEntityService.delete(id)

    /**
     * 샘플 일괄 저장
     */
    @Transactional(transactionManager = "mainTransactionManager")
    fun saveAll(dtoList: List<SampleDto>): List<SampleDto> = sampleJpaEntityService.saveAll(dtoList)

    /**
     * 처리 완료 상태 일괄 업데이트 (JDBC 사용)
     */
    @Transactional(transactionManager = "mainTransactionManager")
    fun markAsCompleted(ids: List<Long>): Int = sampleJdbcEntityService.updateStatusToCompleted(ids)
}