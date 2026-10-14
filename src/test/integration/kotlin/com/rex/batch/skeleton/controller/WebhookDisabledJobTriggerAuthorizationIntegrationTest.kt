package com.rex.batch.skeleton.controller

import com.rex.batch.skeleton.config.TestcontainersConfiguration
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import javax.sql.DataSource

/**
 * `webhook.enabled=false` 에서 트리거가 **fail-closed** 인지 (A-9-a·A-9-b).
 *
 * 이 플래그를 끄면 `WebMvcConfiguration` 이 미등록되어 **인증 인터셉터가 통째로 사라진다.**
 * 그때 남는 방어는 인가자뿐이다 — attribute 가 없으므로 403 으로 닫는다.
 * 2xx 가 나오면 **플래그 하나로 무인증 원격 Job 실행이 열린 것**이다.
 *
 * **두 진입점 각각** 확인한다. 인터셉터가 사라지면 둘 다 열리므로 한쪽만 보면 놓친다.
 * 그리고 **403 만으로는 부족하다** — 거부 응답을 주면서 Job 은 돌아 버리는 구현을
 * 잡기 위해 실행 이력이 늘지 않았음까지 단언한다.
 *
 * Kotest 는 Spring 설정을 **Spec 클래스**에 적용하므로 DSL 중첩 블록에 어노테이션을 붙여
 * 별도 컨텍스트를 만들 수 없다. 그래서 최상위 Spec 으로 분리했다.
 */
@SpringBootTest(
    properties = [
        "webhook.enabled=false",
        "spring.autoconfigure.exclude=org.springframework.boot.quartz.autoconfigure.QuartzAutoConfiguration",
    ],
)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ContextConfiguration(initializers = [TestcontainersConfiguration.Initializer::class])
class WebhookDisabledJobTriggerAuthorizationIntegrationTest(
    private val mockMvc: MockMvc,
    @Qualifier("batchDataSource") private val dataSource: DataSource,
) : DescribeSpec() {
    init {
        val jdbcTemplate = JdbcTemplate(dataSource)

        fun jobExecutionCount(): Int =
            jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM BATCH_JOB_EXECUTION",
                Int::class.java,
            ) ?: 0

        describe("webhook.enabled=false") {

            it("A-9-a /api 트리거가 403 이고 Job 이 실행되지 않는다") {
                val before = jobExecutionCount()

                mockMvc
                    .perform(
                        post("/api/v1/jobs/sampleJob/trigger")
                            .header("X-API-Key", "test-key-full")
                            .contentType(MediaType.APPLICATION_JSON),
                    )
                    .andExpect(status().isForbidden)
                    .andExpect(jsonPath("$.code").value("AUTH_002"))

                jobExecutionCount() shouldBe before
            }

            it("A-9-b /admin/api 트리거도 403 이고 Job 이 실행되지 않는다") {
                val before = jobExecutionCount()

                mockMvc
                    .perform(
                        post("/admin/api/jobs/sampleJob/trigger")
                            .header("X-API-Key", "test-key-full")
                            .contentType(MediaType.APPLICATION_JSON),
                    )
                    .andExpect(status().isForbidden)
                    .andExpect(jsonPath("$.code").value("AUTH_002"))

                jobExecutionCount() shouldBe before
            }
        }
    }
}