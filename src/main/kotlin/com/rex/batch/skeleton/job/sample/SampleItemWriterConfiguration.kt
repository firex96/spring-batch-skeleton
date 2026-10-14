package com.rex.batch.skeleton.job.sample

import com.rex.batch.skeleton.dto.SampleDto
import com.rex.batch.skeleton.util.logger
import org.springframework.batch.infrastructure.item.database.JdbcBatchItemWriter
import org.springframework.batch.infrastructure.item.database.builder.JdbcBatchItemWriterBuilder
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.sql.DataSource

/**
 * 샘플 ItemWriter 설정
 *
 * JdbcBatchItemWriter를 사용한 대량 데이터 저장.
 * JDBC 배치 처리로 성능 최적화.
 */
@Configuration
class SampleItemWriterConfiguration {
    private val log = logger()

    @Bean
    fun sampleItemWriter(
        @Qualifier("mainDataSource") dataSource: DataSource,
    ): JdbcBatchItemWriter<SampleDto> {
        log.info("SampleItemWriter 초기화")

        return JdbcBatchItemWriterBuilder<SampleDto>()
            .dataSource(dataSource)
            .sql(
                """
                UPDATE sample
                SET status = :status,
                    processed_at = :processedAt,
                    updated_at = NOW()
                WHERE id = :id
                """.trimIndent(),
            ).beanMapped()
            .build()
    }
}