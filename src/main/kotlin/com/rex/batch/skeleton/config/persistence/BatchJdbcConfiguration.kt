package com.rex.batch.skeleton.config.persistence

import com.rex.batch.skeleton.converter.ProductStatusReadingConverter
import com.rex.batch.skeleton.converter.StringEnumWritingConverter
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import javax.sql.DataSource

/**
 * 기본(Batch) JDBC 설정
 *
 * 배치 메타데이터 데이터소스를 사용하는 JDBC Repository 설정.
 */
@Configuration
@EnableJdbcRepositories(
    basePackages = [
        "com.rex.batch.skeleton.persistence.jdbc.repository.batch",
    ],
    transactionManagerRef = "batchTransactionManager",
)
class BatchJdbcConfiguration {
    @Primary
    @Bean
    fun batchJdbcTemplate(
        @Qualifier("batchDataSource") dataSource: DataSource,
    ): JdbcTemplate = JdbcTemplate(dataSource)

    @Primary
    @Bean
    fun batchNamedParameterJdbcOperations(
        @Qualifier("batchDataSource") dataSource: DataSource,
    ): NamedParameterJdbcOperations = NamedParameterJdbcTemplate(dataSource)

    @Primary
    @Bean
    fun jdbcCustomConversions(): JdbcCustomConversions = JdbcCustomConversions(
        listOf(
            ProductStatusReadingConverter(),
            StringEnumWritingConverter(),
        ),
    )
}