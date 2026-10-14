package com.rex.batch.skeleton.config.persistence

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
import org.springframework.transaction.PlatformTransactionManager
import javax.sql.DataSource

/**
 * 기본(Batch) JPA 설정
 *
 * 배치 메타데이터 데이터소스를 사용하는 JPA Repository 설정.
 */
@Configuration
@EnableJpaRepositories(
    basePackages = ["com.rex.batch.skeleton.persistence.jpa.repository.batch"],
    entityManagerFactoryRef = "batchEntityManagerFactory",
    transactionManagerRef = "batchTransactionManager",
)
class BatchJpaConfiguration {
    @Primary
    @Bean("batchEntityManagerFactory")
    @DependsOnDatabaseInitialization
    fun batchEntityManagerFactory(
        @Qualifier("batchDataSource") dataSource: DataSource,
        @Value("\${spring.jpa.show-sql:false}") showSql: Boolean,
        @Value("\${spring.jpa.properties.hibernate.dialect:}") dialect: String,
    ): LocalContainerEntityManagerFactoryBean =
        LocalContainerEntityManagerFactoryBean().apply {
            this.dataSource = dataSource
            setPackagesToScan("com.rex.batch.skeleton.persistence.jpa.entity.batch")
            jpaVendorAdapter =
                HibernateJpaVendorAdapter().apply {
                    setShowSql(showSql)
                    setGenerateDdl(false)
                    if (dialect.isNotBlank()) {
                        setDatabasePlatform(dialect)
                    }
                }
            persistenceUnitName = "batch"
        }

    @Primary
    @Bean("batchTransactionManager")
    fun batchTransactionManager(
        @Qualifier("batchEntityManagerFactory") entityManagerFactory: LocalContainerEntityManagerFactoryBean,
    ): PlatformTransactionManager =
        JpaTransactionManager().apply {
            this.entityManagerFactory = entityManagerFactory.`object`
        }
}