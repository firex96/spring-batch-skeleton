package com.rex.batch.skeleton.persistence.jdbc.repository.main

import com.rex.batch.skeleton.persistence.jdbc.entity.main.SampleJdbcEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

/**
 * 샘플 JDBC 리포지토리
 */
@Repository
interface SampleJdbcRepository : CrudRepository<SampleJdbcEntity, Long> {
    /**
     * 상태별 샘플 조회
     */
    fun findAllByStatus(status: String): List<SampleJdbcEntity>
}