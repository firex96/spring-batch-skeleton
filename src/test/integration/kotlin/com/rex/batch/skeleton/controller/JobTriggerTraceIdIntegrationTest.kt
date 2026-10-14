package com.rex.batch.skeleton.controller

import com.rex.batch.skeleton.config.TestcontainersConfiguration
import io.kotest.core.spec.style.DescribeSpec
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * 인바운드 traceId 가 Job 실행까지 이어지는지 (T-1~T-5).
 *
 * 과거 `JobLauncherBizService` 는 트리거마다 **무조건 새 UUID** 를 만들었다. 호출자가
 * `X-Trace-Id` 를 보내도 Job 로그는 다른 값으로 남아 **웹훅 경계에서 추적이 끊겼다.**
 *
 * 배선의 나머지는 이미 있었다 — `JobTraceListener` 가 `JobParameters` 에서 MDC 로
 * 복원한다. 가운데 한 구간(`MDC` → `JobParameters`)만 이으면 된다.
 */
@SpringBootTest(
    properties = [
        "spring.autoconfigure.exclude=org.springframework.boot.quartz.autoconfigure.QuartzAutoConfiguration",
    ],
)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ContextConfiguration(initializers = [TestcontainersConfiguration.Initializer::class])
class JobTriggerTraceIdIntegrationTest(
    private val mockMvc: MockMvc,
) : DescribeSpec() {
    init {
        describe("트리거 응답의 traceId") {

            it("T-1 유효한 X-Trace-Id 를 보내면 그 값이 그대로 쓰인다") {
                mockMvc
                    .perform(
                        post("/api/v1/jobs/sampleJob/trigger")
                            .header("X-API-Key", "test-key-full")
                            .header("X-Trace-Id", "verify-abc-123")
                            .contentType(MediaType.APPLICATION_JSON),
                    )
                    .andExpect(status().isAccepted)
                    .andExpect(jsonPath("$.traceId").value("verify-abc-123"))
            }

            it("T-2 헤더가 없으면 새 값이 생성된다") {
                mockMvc
                    .perform(
                        post("/api/v1/jobs/sampleJob/trigger")
                            .header("X-API-Key", "test-key-full")
                            .contentType(MediaType.APPLICATION_JSON),
                    )
                    .andExpect(status().isAccepted)
                    .andExpect(jsonPath("$.traceId").isNotEmpty)
            }

            it("T-3 개행이 섞인 값은 그대로 쓰이지 않는다 — 로그 위조 차단") {
                mockMvc
                    .perform(
                        post("/api/v1/jobs/sampleJob/trigger")
                            .header("X-API-Key", "test-key-full")
                            .header("X-Trace-Id", "bad\ninjected")
                            .contentType(MediaType.APPLICATION_JSON),
                    )
                    .andExpect(status().isAccepted)
                    .andExpect(jsonPath("$.traceId").value(org.hamcrest.Matchers.not("bad\ninjected")))
            }

            it("T-4 65자를 넘는 값은 그대로 쓰이지 않는다") {
                val tooLong = "a".repeat(65)
                mockMvc
                    .perform(
                        post("/api/v1/jobs/sampleJob/trigger")
                            .header("X-API-Key", "test-key-full")
                            .header("X-Trace-Id", tooLong)
                            .contentType(MediaType.APPLICATION_JSON),
                    )
                    .andExpect(status().isAccepted)
                    .andExpect(jsonPath("$.traceId").value(org.hamcrest.Matchers.not(tooLong)))
            }

            it("T-5 본문으로 traceId 를 덮어쓰려 하면 400 으로 거부된다") {
                mockMvc
                    .perform(
                        post("/api/v1/jobs/sampleJob/trigger")
                            .header("X-API-Key", "test-key-full")
                            .header("X-Trace-Id", "verify-abc-123")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""{"parameters":{"traceId":"forged"}}"""),
                    )
                    .andExpect(status().isBadRequest)
                    .andExpect(jsonPath("$.code").value("COMMON_001"))
                    // 거부 응답이므로 트리거 응답 모델이 반환되지 않는다
                    .andExpect(jsonPath("$.jobExecutionId").doesNotExist())
            }
        }
    }
}