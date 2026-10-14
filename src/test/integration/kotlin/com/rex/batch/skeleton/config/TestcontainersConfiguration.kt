package com.rex.batch.skeleton.config

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Bean
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.utility.DockerImageName

/**
 * Testcontainers 설정.
 *
 * Docker 컨테이너를 사용하여 실제 데이터베이스와 통합 테스트를 수행합니다.
 * - MySQL `skeleton_batch`: batch(메타·Quartz) 데이터소스 — Flyway 가 스키마를 만든다
 * - MySQL `skeleton_main`: main 데이터소스 — `db/manual/create-main.sql`
 * - MySQL `skeleton_sub`: sub 데이터소스 — `db/manual/create-sub.sql`
 *
 * 스키마마다 컨테이너를 따로 띄워 데이터소스가 엉뚱한 스키마에 붙으면 테이블이 없어 바로 드러나게 한다.
 *
 * 사용 예시:
 * ```kotlin
 * @SpringBootTest
 * @ActiveProfiles("test")
 * @ContextConfiguration(initializers = [TestcontainersConfiguration.Initializer::class])
 * class MyDatabaseIntegrationTest {
 *     // 테스트 코드
 * }
 * ```
 *
 * 주의사항:
 * - Docker가 설치되어 있어야 합니다
 * - 테스트 실행 시 컨테이너 시작으로 인한 초기 지연이 있습니다
 */
@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {
    companion object {
        private const val MYSQL_IMAGE = "mysql:8.0"

        /**
         * MySQL 테스트 컨테이너 (batch 메타·Quartz 데이터소스용).
         */
        val mysqlContainer: MySQLContainer<*> by lazy { mysql("skeleton_batch", null) }

        /**
         * MySQL 테스트 컨테이너 (main 데이터소스용).
         */
        val mainMysqlContainer: MySQLContainer<*> by lazy { mysql("skeleton_main", "db/manual/create-main.sql") }

        /**
         * MySQL 테스트 컨테이너 (sub 데이터소스용).
         */
        val subMysqlContainer: MySQLContainer<*> by lazy { mysql("skeleton_sub", "db/manual/create-sub.sql") }

        private fun mysql(
            database: String,
            initScript: String?,
        ): MySQLContainer<*> =
            MySQLContainer(DockerImageName.parse(MYSQL_IMAGE))
                .withDatabaseName(database)
                .apply { if (initScript != null) withInitScript(initScript) }
                .withUsername("test")
                .withPassword("test")
                .also { it.start() }
    }

    /**
     * ApplicationContextInitializer for configuring datasources from Testcontainers.
     * Use with @ContextConfiguration(initializers = [TestcontainersConfiguration.Initializer::class])
     *
     * 테스트마다 companion object + @DynamicPropertySource 를 두는 대신 이 한 줄로 끝낸다.
     * 컨테이너가 늘어도 테스트를 고치지 않는다.
     */
    class Initializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
        override fun initialize(applicationContext: ConfigurableApplicationContext) {
            val mysql = mysqlContainer
            val main = mainMysqlContainer
            val sub = subMysqlContainer

            TestPropertyValues.of(
                "spring.flyway.enabled=true",
                "app.flyway.mysql.username=${mysql.username}",
                "app.flyway.mysql.password=${mysql.password}",
                "spring.batch.jdbc.initialize-schema=never",
                *mysqlProperties("spring.datasource", mysql),
                *mysqlProperties("spring.datasource.batch.write", mysql),
                *mysqlProperties("spring.datasource.batch.read", mysql),
                *mysqlProperties("spring.datasource.main.write", main),
                *mysqlProperties("spring.datasource.main.read", main),
                *mysqlProperties("spring.datasource.sub.write", sub),
                *mysqlProperties("spring.datasource.sub.read", sub),
            ).applyTo(applicationContext.environment)
        }

        private fun mysqlProperties(
            prefix: String,
            container: MySQLContainer<*>,
        ): Array<String> = properties(prefix, container.jdbcUrl, container.username, container.password, container.driverClassName)

        /**
         * `driver-class-name` 도 함께 주입한다 — 컨테이너가 알려 주는 드라이버를 그대로 쓴다.
         * `jdbc-url` 과 `url` 을 둘 다 넣는 이유는 Hikari 와 Boot 가 서로 다른 키를 읽기 때문이다.
         */
        private fun properties(
            prefix: String,
            url: String,
            username: String,
            password: String,
            driverClassName: String,
        ): Array<String> =
            arrayOf(
                "$prefix.jdbc-url=$url",
                "$prefix.url=$url",
                "$prefix.username=$username",
                "$prefix.password=$password",
                "$prefix.driver-class-name=$driverClassName",
            )
    }

    @Bean
    fun mysqlContainer(): MySQLContainer<*> = Companion.mysqlContainer

    @Bean
    fun mainMysqlContainer(): MySQLContainer<*> = Companion.mainMysqlContainer

    @Bean
    fun subMysqlContainer(): MySQLContainer<*> = Companion.subMysqlContainer
}