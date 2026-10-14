package com.rex.batch.skeleton.config.persistence

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy
import javax.sql.DataSource

/**
 * main 데이터소스 설정 — 업무 스키마(skeleton_main). 샘플 Job 이 읽고 쓰는 `sample` 테이블이 여기 있다.
 */
@Configuration
class MainDataSourceConfiguration {
    @Bean("mainWriteDataSourceProperties")
    @ConfigurationProperties(prefix = "spring.datasource.main.write")
    fun mainWriteDataSourceProperties(): DataSourceProperties = DataSourceProperties()

    @Bean("mainReadDataSourceProperties")
    @ConfigurationProperties(prefix = "spring.datasource.main.read")
    fun mainReadDataSourceProperties(): DataSourceProperties = DataSourceProperties()

    @Bean("mainWriteDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.main.write.hikari")
    fun mainWriteDataSource(
        @Qualifier("mainWriteDataSourceProperties") properties: DataSourceProperties,
    ): DataSource = properties.initializeDataSourceBuilder().build()

    @Bean("mainReadDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.main.read.hikari")
    fun mainReadDataSource(
        @Qualifier("mainReadDataSourceProperties") properties: DataSourceProperties,
    ): DataSource = properties.initializeDataSourceBuilder().build()

    @Bean("mainDataSource")
    fun mainDataSource(
        @Qualifier("mainWriteDataSource") writeDataSource: DataSource,
        @Qualifier("mainReadDataSource") readDataSource: DataSource,
    ): DataSource =
        LazyConnectionDataSourceProxy(writeDataSource).apply {
            setReadOnlyDataSource(readDataSource)
        }
}