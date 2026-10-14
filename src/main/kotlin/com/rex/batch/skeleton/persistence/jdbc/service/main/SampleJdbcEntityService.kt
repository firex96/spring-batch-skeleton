package com.rex.batch.skeleton.persistence.jdbc.service.main

import com.rex.batch.skeleton.dto.SampleDto
import com.rex.batch.skeleton.mapper.SampleMapper
import com.rex.batch.skeleton.persistence.jdbc.repository.main.SampleJdbcRepository
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 샘플 JDBC 엔티티 서비스
 *
 * 복잡한 쿼리나 대량 데이터 처리 시 JDBC를 직접 사용합니다.
 */
@Transactional(readOnly = true, transactionManager = "mainTransactionManager")
@Service
class SampleJdbcEntityService(
    @Qualifier("mainNamedParameterJdbcOperations")
    private val jdbcOperations: NamedParameterJdbcOperations,
    private val sampleJdbcRepository: SampleJdbcRepository,
    private val mapper: SampleMapper,
) {
    /**
     * ID로 샘플 조회
     */
    fun get(id: Long): SampleDto? =
        sampleJdbcRepository.findById(id).orElse(null)?.let {
            mapper.toDto(it)
        }

    /**
     * 상태별 샘플 조회
     */
    fun getAllByStatus(status: String): List<SampleDto> =
        sampleJdbcRepository.findAllByStatus(status).let {
            mapper.jdbcEntitiesToDtoList(it)
        }

    /**
     * 처리 대상 샘플 ID 목록 조회 (복잡한 쿼리 예시)
     */
    fun getPendingSampleIds(): List<Long> {
        val namedParameters = MapSqlParameterSource("status", "PENDING")

        return jdbcOperations
            .queryForList(
                """
                SELECT id
                FROM sample
                WHERE status = :status
                AND processed_at IS NULL
                ORDER BY created_at ASC
                """.trimIndent(),
                namedParameters,
                Long::class.javaObjectType,
            ).filterNotNull()
    }

    /**
     * 처리 완료 상태 일괄 업데이트
     */
    @Transactional(transactionManager = "mainTransactionManager")
    fun updateStatusToCompleted(ids: List<Long>): Int {
        if (ids.isEmpty()) return 0

        val namedParameters =
            MapSqlParameterSource()
                .addValue("ids", ids)
                .addValue("status", "COMPLETED")

        return jdbcOperations.update(
            """
            UPDATE sample
            SET status = :status, processed_at = NOW(), updated_at = NOW()
            WHERE id IN (:ids)
            """.trimIndent(),
            namedParameters,
        )
    }
}