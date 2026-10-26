package com.rex.batch.skeleton.controller

import com.rex.batch.skeleton.config.TestcontainersConfiguration
import io.kotest.core.spec.style.DescribeSpec
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * 없는 실행 ID 조회는 웹훅·관리자 API 둘 다 404 `JOB_002` 오류 모델이다.
 *
 * Spring Batch 의 `JobExplorer.getJobExecution` 은 없는 id 에 `null` 대신 `EmptyResultDataAccessException` 을 던져
 * 캐치올 500 `COMMON_999` 로 나갔다. 관리자 API 의 null 분기(빈 본문 404)에는 닿지도 못했다.
 */
@SpringBootTest(
    properties = [
        "spring.autoconfigure.exclude=org.springframework.boot.quartz.autoconfigure.QuartzAutoConfiguration",
    ],
)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ContextConfiguration(initializers = [TestcontainersConfiguration.Initializer::class])
class JobExecutionNotFoundIntegrationTest(
    private val mockMvc: MockMvc,
) : DescribeSpec() {
    init {
        describe("없는 실행 ID 조회") {

            it("웹훅 API 는 404 JOB_002 다") {
                mockMvc
                    .perform(get("/api/v1/jobs/executions/$MISSING_ID").header("X-API-Key", "test-key-full"))
                    .andExpect(status().isNotFound)
                    .andExpect(jsonPath("$.code").value("JOB_002"))
                    .andExpect(jsonPath("$.details").value("jobExecutionId: $MISSING_ID"))
            }

            it("관리자 API 도 404 JOB_002 오류 모델이다") {
                mockMvc
                    .perform(get("/admin/api/executions/$MISSING_ID").header("X-API-Key", "test-key-full"))
                    .andExpect(status().isNotFound)
                    .andExpect(jsonPath("$.code").value("JOB_002"))
                    .andExpect(jsonPath("$.details").value("jobExecutionId: $MISSING_ID"))
            }
        }
    }

    companion object {
        private const val MISSING_ID = 999_999_999L
    }
}