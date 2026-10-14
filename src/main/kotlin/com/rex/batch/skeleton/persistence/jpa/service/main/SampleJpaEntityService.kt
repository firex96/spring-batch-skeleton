package com.rex.batch.skeleton.persistence.jpa.service.main

import com.rex.batch.skeleton.dto.SampleDto
import com.rex.batch.skeleton.mapper.SampleMapper
import com.rex.batch.skeleton.persistence.jpa.repository.main.SampleJpaRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 샘플 JPA 엔티티 서비스
 *
 * JPA를 통한 샘플 데이터 CRUD 작업을 처리합니다.
 */
@Transactional(readOnly = true, transactionManager = "mainTransactionManager")
@Service
class SampleJpaEntityService(
    private val sampleJpaRepository: SampleJpaRepository,
    private val mapper: SampleMapper,
) {
    /**
     * ID로 샘플 조회
     */
    fun get(id: Long): SampleDto? =
        sampleJpaRepository.findByIdOrNull(id)?.let {
            mapper.toDto(it)
        }

    /**
     * 상태별 샘플 목록 조회
     */
    fun getAllByStatus(status: String): List<SampleDto> =
        sampleJpaRepository.findAllByStatus(status).let {
            mapper.jpaEntitiesToDtoList(it)
        }

    /**
     * 처리되지 않은 샘플 목록 조회
     */
    fun getAllUnprocessed(): List<SampleDto> =
        sampleJpaRepository.findAllByProcessedAtIsNull().let {
            mapper.jpaEntitiesToDtoList(it)
        }

    /**
     * 샘플 생성
     */
    @Transactional(transactionManager = "mainTransactionManager")
    fun create(dto: SampleDto): SampleDto {
        require(dto.id == 0L) { "신규 생성 시 ID는 0이어야 합니다." }

        return sampleJpaRepository.save(mapper.toEntity(dto)).let {
            mapper.toDto(it)
        }
    }

    /**
     * 샘플 수정
     */
    @Transactional(transactionManager = "mainTransactionManager")
    fun update(dto: SampleDto): SampleDto {
        sampleJpaRepository.findById(dto.id).orElseThrow {
            NoSuchElementException("Sample with id ${dto.id} not found")
        }

        return sampleJpaRepository.save(mapper.toEntity(dto)).let {
            mapper.toDto(it)
        }
    }

    /**
     * 샘플 삭제
     */
    @Transactional(transactionManager = "mainTransactionManager")
    fun delete(id: Long) {
        sampleJpaRepository.deleteById(id)
    }

    /**
     * 샘플 일괄 저장
     */
    @Transactional(transactionManager = "mainTransactionManager")
    fun saveAll(dtoList: List<SampleDto>): List<SampleDto> {
        val entities = dtoList.map { mapper.toEntity(it) }
        return sampleJpaRepository.saveAll(entities).let {
            mapper.jpaEntitiesToDtoList(it)
        }
    }
}