package com.rex.batch.skeleton.persistence

import com.rex.batch.skeleton.config.TestcontainersConfiguration
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import javax.sql.DataSource

/**
 * 각 DataSource 풀이 **의도한 스키마에 실제로 연결되는지** 검증한다.
 *
 * 세 그룹(batch 메타 · main · sub)이 모두 MySQL 이라 엔진으로는 구분되지 않는다 — 풀마다 `SELECT DATABASE()`
 * 로 붙은 스키마를 확인한다. 통합테스트는 스키마마다 컨테이너를 따로 띄우므로 잘못 배선된 풀은 다른 이름을 돌려준다.
 *
 * **라우팅/lazy 프록시(`{group}DataSource`)가 아니라 원본 풀 6개에 직접 연결한다.**
 * 프록시에서 트랜잭션 없이 연결하면 write 쪽만 열려, `*.read` 가 잘못된 스키마에
 * 남아 있어도 통과한다.
 *
 * **Hikari 로그 확인으로는 부족하다** — 풀은 lazy 라 로그가 있어도 연결이
 * 한 번도 열리지 않을 수 있다. 쿼리로 실제 연결을 연다.
 */
@SpringBootTest(
    properties = [
        // test 프로파일은 RAMJobStore 를 쓰는데 JDBC 속성이 함께 넘어가 Quartz 기동이 깨진다.
        // 기존 batch 통합테스트와 같은 이유로 자동 구성을 제외한다.
        "spring.autoconfigure.exclude=org.springframework.boot.quartz.autoconfigure.QuartzAutoConfiguration",
    ],
)
@ActiveProfiles("test")
@ContextConfiguration(initializers = [TestcontainersConfiguration.Initializer::class])
class DataSourceEngineBindingIntegrationTest(
    private val applicationContext: ApplicationContext,
) : BehaviorSpec({

    fun databaseOf(beanName: String): String {
        val dataSource = applicationContext.getBean(beanName, DataSource::class.java)
        return dataSource.connection.use { connection ->
            connection.createStatement().use { statement ->
                statement.executeQuery("SELECT DATABASE()").use { result ->
                    result.next()
                    result.getString(1)
                }
            }
        }
    }

    mapOf(
        "batch" to "skeleton_batch",
        "main" to "skeleton_main",
        "sub" to "skeleton_sub",
    ).forEach { (group, database) ->
        Given("$group 그룹") {
            listOf("${group}WriteDataSource", "${group}ReadDataSource").forEach { beanName ->
                Then("$beanName 은 $database 에 연결된다") {
                    databaseOf(beanName) shouldBe database
                }
            }
        }
    }
})