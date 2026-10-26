package com.rex.batch.skeleton.controller

import com.rex.batch.skeleton.config.TestcontainersConfiguration
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.string.shouldNotContain
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * 실제 웹훅·관리자 API 경로에서 요청 값 형식 오류는 400 `COMMON_001` 이고, 응답에 요청 값·예외 메시지가 나가지 않는다.
 *
 * 전용 핸들러가 없던 때는 `MethodArgumentTypeMismatchException`·`HttpMessageNotReadableException` 이 캐치올 500 `COMMON_999` 로 나갔다.
 */
@SpringBootTest(
    properties = [
        "spring.autoconfigure.exclude=org.springframework.boot.quartz.autoconfigure.QuartzAutoConfiguration",
    ],
)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ContextConfiguration(initializers = [TestcontainersConfiguration.Initializer::class])
class RequestValueFormatIntegrationTest(
    private val mockMvc: MockMvc,
) : DescribeSpec() {
    init {
        describe("요청 값 형식 오류") {

            it("웹훅 실행 조회의 Long 경로 변수에 문자를 주면 400 COMMON_001 이고 details 에는 파라미터 이름만 있다") {
                val body =
                    mockMvc
                        .perform(get("/api/v1/jobs/executions/abc").header("X-API-Key", "test-key-full"))
                        .andExpect(status().isBadRequest)
                        .andExpect(jsonPath("$.code").value("COMMON_001"))
                        .andExpect(jsonPath("$.details").value("parameter=jobExecutionId"))
                        .andReturn()
                        .response.contentAsString
                body shouldNotContain "abc"
                body shouldNotContain "Exception"
            }

            it("관리자 API 의 Int 쿼리에 문자를 주면 400 COMMON_001 이다") {
                mockMvc
                    .perform(get("/admin/api/executions").param("page", "first").header("X-API-Key", "test-key-full"))
                    .andExpect(status().isBadRequest)
                    .andExpect(jsonPath("$.code").value("COMMON_001"))
                    .andExpect(jsonPath("$.details").value("parameter=page"))
            }

            it("웹훅 트리거 본문이 깨진 JSON 이면 400 COMMON_001 이고 details 가 없다") {
                val body =
                    mockMvc
                        .perform(
                            post("/api/v1/jobs/sampleJob/trigger")
                                .header("X-API-Key", "test-key-full")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"parameters\":"),
                        ).andExpect(status().isBadRequest)
                        .andExpect(jsonPath("$.code").value("COMMON_001"))
                        .andExpect(jsonPath("$.details").doesNotExist())
                        .andReturn()
                        .response.contentAsString
                body shouldNotContain "Exception"
                body shouldNotContain "tools.jackson"
            }
        }
    }
}