package com.rex.batch.skeleton.config.persistence

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties
import org.springframework.boot.quartz.autoconfigure.QuartzDataSource
import org.springframework.boot.quartz.autoconfigure.QuartzTransactionManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy
import org.springframework.transaction.PlatformTransactionManager
import javax.sql.DataSource

/**
 * 기본 데이터소스 설정 (배치 메타데이터 + Quartz)
 *
 * Spring Batch JobRepository 및 Quartz 스케줄러 메타데이터 저장용.
 * MySQL을 사용하여 배치 실행 이력, Quartz 트리거 정보 등을 영구 저장.
 *
 * Note: Quartz는 DataSource에서 직접 드라이버 정보를 읽어야 하므로
 * batchWriteDataSource에 @QuartzDataSource를 적용하여 Quartz가 직접 사용하도록 합니다.
 */
@Configuration
class BatchDataSourceConfiguration {
    @Primary
    @Bean("batchWriteDataSourceProperties")
    @ConfigurationProperties(prefix = "spring.datasource.batch.write")
    fun batchWriteDataSourceProperties(): DataSourceProperties = DataSourceProperties()

    @Bean("batchReadDataSourceProperties")
    @ConfigurationProperties(prefix = "spring.datasource.batch.read")
    fun batchReadDataSourceProperties(): DataSourceProperties = DataSourceProperties()

    @QuartzDataSource
    @Bean("batchWriteDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.batch.write.hikari")
    fun batchWriteDataSource(
        @Qualifier("batchWriteDataSourceProperties") properties: DataSourceProperties,
    ): DataSource = properties.initializeDataSourceBuilder().build()

    @Bean("batchReadDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.batch.read.hikari")
    fun batchReadDataSource(
        @Qualifier("batchReadDataSourceProperties") properties: DataSourceProperties,
    ): DataSource = properties.initializeDataSourceBuilder().build()

    @Primary
    @Bean("batchDataSource")
    fun batchDataSource(
        @Qualifier("batchWriteDataSource") writeDataSource: DataSource,
        @Qualifier("batchReadDataSource") readDataSource: DataSource,
    ): DataSource =
        LazyConnectionDataSourceProxy(writeDataSource).apply {
            setReadOnlyDataSource(readDataSource)
        }

    /**
     * Quartz 가 JobDetail·Trigger 를 JobStore 에 저장할 때 쓰는 트랜잭션 매니저.
     *
     * Quartz(`LocalDataSourceJobStore`)는 [QuartzDataSource] 인 원본 write 풀에서 커넥션을 받는다. 등록을 primary
     * (`batchTransactionManager` — 프록시 `batchDataSource` 에 묶인 JPA 매니저) 트랜잭션에서 돌리면 Quartz 커넥션이
     * 그 트랜잭션에 참여하지 못하고, 풀이 `auto-commit: false` 라 **저장이 조용히 롤백돼 트리거가 등록되지 않는다.**
     * 같은 원본 풀에 묶인 매니저를 따로 지정해 등록이 커밋되게 한다.
     */
    @QuartzTransactionManager
    @Bean("quartzTransactionManager")
    fun quartzTransactionManager(
        @Qualifier("batchWriteDataSource") writeDataSource: DataSource,
    ): PlatformTransactionManager = DataSourceTransactionManager(writeDataSource)
}