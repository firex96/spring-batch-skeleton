package com.rex.batch.skeleton.job.sample

import com.rex.batch.skeleton.constant.Constant
import com.rex.batch.skeleton.dto.SampleDto
import com.rex.batch.skeleton.util.logger
import org.springframework.batch.infrastructure.item.database.JdbcPagingItemReader
import org.springframework.batch.infrastructure.item.database.Order
import org.springframework.batch.infrastructure.item.database.builder.JdbcPagingItemReaderBuilder
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.DataClassRowMapper
import javax.sql.DataSource

/**
 * 샘플 ItemReader 설정
 *
 * JdbcPagingItemReader를 사용한 대용량 데이터 읽기.
 * 페이징 처리로 메모리 효율적인 데이터 조회.
 */
@Configuration
class SampleItemReaderConfiguration {
    private val log = logger()

    @Bean
    fun sampleItemReader(
        @Qualifier("mainReadDataSource") dataSource: DataSource,
    ): JdbcPagingItemReader<SampleDto> {
        log.info("SampleItemReader 초기화")

        return JdbcPagingItemReaderBuilder<SampleDto>()
            .name("sampleItemReader")
            .dataSource(dataSource)
            .selectClause("SELECT id, name, status, processed_at, description, created_by, created_at, updated_by, updated_at")
            .fromClause("FROM sample")
            .whereClause("WHERE status = :status AND processed_at IS NULL")
            .sortKeys(mapOf("id" to Order.ASCENDING))
            .parameterValues(mapOf("status" to "PENDING"))
            .pageSize(Constant.Batch.DEFAULT_CHUNK_SIZE)
            .rowMapper(DataClassRowMapper(SampleDto::class.java))
            .build()
    }
}