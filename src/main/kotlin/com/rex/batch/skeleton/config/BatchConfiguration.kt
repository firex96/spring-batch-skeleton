package com.rex.batch.skeleton.config

import org.springframework.batch.core.configuration.support.JdbcDefaultBatchConfiguration
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher
import org.springframework.batch.core.repository.JobRepository
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.core.task.SyncTaskExecutor
import org.springframework.transaction.PlatformTransactionManager
import javax.sql.DataSource

/**
 * Spring Batch 설정
 *
 * Spring Batch 6.0에서는 JdbcDefaultBatchConfiguration을 상속하여
 * JDBC 기반 배치 인프라를 설정합니다.
 *
 * batchDataSource와 batchTransactionManager를 사용하여
 * 배치 메타데이터 저장소를 지정합니다.
 */
@Configuration
@DependsOnDatabaseInitialization
class BatchConfiguration(
    @Qualifier("batchDataSource") private val batchDataSource: DataSource,
    @Qualifier("batchTransactionManager") private val batchTransactionManager: PlatformTransactionManager,
) : JdbcDefaultBatchConfiguration() {
    override fun getDataSource(): DataSource = batchDataSource

    override fun getTransactionManager(): PlatformTransactionManager = batchTransactionManager

    override fun getTablePrefix(): String = "BATCH_"

    /**
     * Primary JobLauncher 빈.
     * 동기 실행을 기본으로 합니다. 비동기 실행이 필요한 경우 asyncJobLauncher를 사용합니다.
     */
    @Bean("jobLauncher")
    @Primary
    fun jobLauncher(jobRepository: JobRepository): JobLauncher {
        val launcher = TaskExecutorJobLauncher()
        launcher.setJobRepository(jobRepository)
        launcher.setTaskExecutor(SyncTaskExecutor())
        launcher.afterPropertiesSet()
        return launcher
    }
}