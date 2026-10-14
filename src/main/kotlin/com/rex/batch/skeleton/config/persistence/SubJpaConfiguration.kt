package com.rex.batch.skeleton.config.persistence

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
import org.springframework.transaction.PlatformTransactionManager
import javax.sql.DataSource

/**
 * sub JPA 설정 — 두 번째 업무 스키마(skeleton_sub) JPA Repository.
 */
@Configuration
@EnableJpaRepositories(
    basePackages = ["com.rex.batch.skeleton.persistence.jpa.repository.sub"],
    entityManagerFactoryRef = "subEntityManagerFactory",
    transactionManagerRef = "subTransactionManager",
)
class SubJpaConfiguration {
    @Bean("subEntityManagerFactory")
    @DependsOnDatabaseInitialization
    fun subEntityManagerFactory(
        @Qualifier("subDataSource") dataSource: DataSource,
        @Value("\${spring.jpa.show-sql:false}") showSql: Boolean,
        @Value("\${spring.jpa.properties.hibernate.dialect:}") dialect: String,
    ): LocalContainerEntityManagerFactoryBean =
        LocalContainerEntityManagerFactoryBean().apply {
            this.dataSource = dataSource
            setPackagesToScan("com.rex.batch.skeleton.persistence.jpa.entity.sub")
            jpaVendorAdapter =
                HibernateJpaVendorAdapter().apply {
                    setShowSql(showSql)
                    setGenerateDdl(false)
                    if (dialect.isNotBlank()) {
                        setDatabasePlatform(dialect)
                    }
                }
            persistenceUnitName = "sub"
        }

    @Bean("subTransactionManager")
    fun subTransactionManager(
        @Qualifier("subEntityManagerFactory") entityManagerFactory: LocalContainerEntityManagerFactoryBean,
    ): PlatformTransactionManager =
        JpaTransactionManager().apply {
            this.entityManagerFactory = entityManagerFactory.`object`
        }
}