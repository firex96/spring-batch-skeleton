package com.rex.batch.skeleton.persistence

import com.rex.batch.skeleton.config.persistence.MysqlFlywayConfiguration
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.exception.FlywayValidateException
import org.quartz.JobBuilder
import org.quartz.JobKey
import org.quartz.TriggerBuilder
import org.quartz.TriggerKey
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.core.io.ClassPathResource
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.DriverManagerDataSource
import org.springframework.scheduling.quartz.SchedulerFactoryBean
import org.testcontainers.containers.MySQLContainer
import java.util.Properties
import java.util.function.Supplier

/** 실제 애플리케이션 Flyway 빈으로 빈 DB 초기화와 재실행/체크섬 실패를 검증한다. */
class MysqlSchemaInitializationIntegrationTest : StringSpec({
    "MySQL 초기화는 레포 이력과 소유 테이블만 생성하고 재실행 시 중복하지 않는다" {
        val mysql = MySQLContainer("mysql:8.0").withDatabaseName("skeleton_batch")
        mysql.start()
        val dataSource = DriverManagerDataSource(mysql.jdbcUrl, mysql.username, mysql.password)
        val business = MySQLContainer("mysql:8.0").withDatabaseName("skeleton_main")
        business.start()
        val businessDataSource = DriverManagerDataSource(business.jdbcUrl, business.username, business.password)
        fun context(enabled: Boolean): AnnotationConfigApplicationContext {
            val context = AnnotationConfigApplicationContext()
            TestPropertyValues.of(
                "spring.flyway.enabled=$enabled",
                "app.flyway.mysql.username=${mysql.username}",
                "app.flyway.mysql.password=${mysql.password}",
            ).applyTo(context)
            context.registerBean(
                "batchWriteDataSourceProperties",
                DataSourceProperties::class.java,
                Supplier {
                    DataSourceProperties().apply { url = mysql.jdbcUrl }
                },
            )
            context.register(MysqlFlywayConfiguration::class.java)
            context.refresh()
            return context
        }
        try {
            val jdbc = JdbcTemplate(dataSource)
            context(false).use { disabled ->
                disabled.getBeansOfType(Flyway::class.java).isEmpty() shouldBe true
                jdbc.queryForObject("SELECT COUNT(*) FROM information_schema.tables WHERE table_schema=DATABASE()", Long::class.java) shouldBe 0L
            }
            context(true).use { context ->
                jdbc.queryForObject("SELECT DATABASE()", String::class.java) shouldBe "skeleton_batch"
                val flyway = context.getBean("flywayBatch", Flyway::class.java)
                flyway.configuration.table shouldBe "flyway_schema_history_batch"
                flyway.validate()
                flyway.migrate().migrationsExecuted shouldBe 0
                jdbc.queryForObject("SELECT COUNT(*) FROM BATCH_JOB_INSTANCE", Long::class.java) shouldBe 0L
                jdbc.queryForObject("SELECT COUNT(*) FROM QRTZ_JOB_DETAILS", Long::class.java) shouldBe 0L
                jdbc.queryForObject("SELECT COUNT(*) FROM CUSTOM_SEQ", Long::class.java) shouldBe 3L
                jdbc.queryForObject("SELECT COUNT(*) FROM information_schema.tables WHERE table_schema=DATABASE() AND table_name='sample'", Long::class.java) shouldBe 0L
                // DDL 이 명시한 콜레이션 — 새 DB 의 테이블·문자열 컬럼은 전부 utf8mb4_0900_ai_ci (이력 테이블은 Flyway 가 서버 기본으로 만들어 제외)
                jdbc.queryForList(
                    "SELECT DISTINCT table_collation FROM information_schema.tables WHERE table_schema=DATABASE() AND table_type='BASE TABLE' AND table_name NOT LIKE 'flyway\\_schema\\_history%'",
                    String::class.java,
                ) shouldBe listOf("utf8mb4_0900_ai_ci")
                jdbc.queryForList(
                    "SELECT DISTINCT collation_name FROM information_schema.columns WHERE table_schema=DATABASE() AND collation_name IS NOT NULL AND table_name NOT LIKE 'flyway\\_schema\\_history%'",
                    String::class.java,
                ) shouldBe listOf("utf8mb4_0900_ai_ci")
                val businessJdbc = JdbcTemplate(businessDataSource)
                businessJdbc.queryForObject("SELECT DATABASE()", String::class.java) shouldBe "skeleton_main"
                businessJdbc.queryForObject("SELECT COUNT(*) FROM information_schema.tables WHERE table_schema=DATABASE()", Long::class.java) shouldBe 0L
                businessJdbc.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema=DATABASE() AND table_name='BATCH_JOB_INSTANCE'",
                    Long::class.java,
                ) shouldBe
                    0L
            }
            fun scheduler(): SchedulerFactoryBean = SchedulerFactoryBean().apply {
                setSchedulerName("schema-persistence-test")
                setDataSource(dataSource)
                setAutoStartup(false)
                setQuartzProperties(
                    Properties().apply {
                        setProperty("org.quartz.jobStore.tablePrefix", "QRTZ_")
                        setProperty("org.quartz.threadPool.threadCount", "1")
                    },
                )
                afterPropertiesSet()
            }
            val first = scheduler()
            try {
                val job = JobBuilder.newJob(SchemaProbeQuartzJob::class.java)
                    .withIdentity("schema-job").storeDurably().build()
                first.scheduler.addJob(job, false)
                first.scheduler.scheduleJob(
                    TriggerBuilder.newTrigger().withIdentity("schema-trigger")
                        .forJob(job).startAt(java.util.Date(System.currentTimeMillis() + 3600000)).build(),
                )
            } finally {
                first.destroy()
            }
            val restarted = scheduler()
            try {
                restarted.scheduler.checkExists(JobKey("schema-job")) shouldBe true
                restarted.scheduler.checkExists(TriggerKey("schema-trigger")) shouldBe true
                restarted.scheduler.deleteJob(JobKey("schema-job")) shouldBe true
            } finally {
                restarted.destroy()
            }
            context(true).use { it.getBean("flywayBatch", Flyway::class.java).validate() }
            jdbc.update("UPDATE flyway_schema_history_batch SET checksum=checksum+1 WHERE version='1'")
            val validation = Flyway.configure().dataSource(dataSource)
                .locations("classpath:db/migration/batch")
                .table("flyway_schema_history_batch").load()
            shouldThrow<FlywayValidateException> { validation.validate() }
        } finally {
            business.stop()
            mysql.stop()
        }
    }
})