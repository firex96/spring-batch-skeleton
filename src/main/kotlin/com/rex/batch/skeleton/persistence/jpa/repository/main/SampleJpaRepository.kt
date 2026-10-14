package com.rex.batch.skeleton.persistence.jpa.repository.main

import com.rex.batch.skeleton.persistence.jpa.entity.main.SampleJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * 샘플 JPA 리포지토리
 */
@Repository
interface SampleJpaRepository : JpaRepository<SampleJpaEntity, Long> {
    /**
     * 상태별 샘플 조회
     */
    fun findAllByStatus(status: String): List<SampleJpaEntity>

    /**
     * 처리되지 않은 샘플 조회
     */
    fun findAllByProcessedAtIsNull(): List<SampleJpaEntity>
}