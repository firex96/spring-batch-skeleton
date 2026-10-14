package com.rex.batch.skeleton.controller

import com.rex.batch.skeleton.config.TestcontainersConfiguration
import io.kotest.core.spec.style.DescribeSpec
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
 * Job 트리거 인가의 경로·상태 코드 계약 (A-1~A-8·A-10).
 *
 * 이 스켈레톤은 **키 멤버십만 검사**했고 `/admin/api` 하위는 인터셉터 밖이라
 * **API Key 없이도 Job 을 트리거할 수 있었다**(OPTI KI-002 가 파생에서 먼저 발견).
 *
 * 지금은 `/api` 와 `/admin/api` **하위 전체**가 인증 대상이다.
 * 조회 표면의 인증 계약은 `AdminApiReadAuthorizationIntegrationTest` 가 본다.
 *
 * **인증(401)과 인가(403)를 구분한다** — 소비자가 「키가 틀렸다」와 「이 키로는 안 된다」를
 * 구분할 수 있어야 한다.
 *
 * `webhook.enabled=false` 분기(A-9)는 별도 컨텍스트가 필요해
 * [WebhookDisabledJobTriggerAuthorizationIntegrationTest] 에 있다.
 */
@SpringBootTest(
    properties = [
        "spring.autoconfigure.exclude=org.springframework.boot.quartz.autoconfigure.QuartzAutoConfiguration",
    ],
)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ContextConfiguration(initializers = [TestcontainersConfiguration.Initializer::class])
class JobTriggerAuthorizationIntegrationTest(
    private val mockMvc: MockMvc,
) : DescribeSpec() {
    init {
        describe("Job 트리거 인가") {

            it("A-1 키가 없으면 401") {
                mockMvc
                    .perform(post("/api/v1/jobs/sampleJob/trigger").contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized)
                    .andExpect(jsonPath("$.code").value("AUTH_001"))
            }

            it("A-2 잘못된 키면 401") {
                mockMvc
                    .perform(
                        post("/api/v1/jobs/sampleJob/trigger")
                            .header("X-API-Key", "wrong-key")
                            .contentType(MediaType.APPLICATION_JSON),
                    )
                    .andExpect(status().isUnauthorized)
                    .andExpect(jsonPath("$.code").value("AUTH_001"))
            }

            it("A-3 유효한 키라도 allowed-jobs 밖 Job 이면 403") {
                mockMvc
                    .perform(
                        post("/api/v1/jobs/sampleJob/trigger")
                            .header("X-API-Key", "test-key-none")
                            .contentType(MediaType.APPLICATION_JSON),
                    )
                    .andExpect(status().isForbidden)
                    .andExpect(jsonPath("$.code").value("AUTH_002"))
            }

            it("A-4 허용 목록 밖 파라미터면 400") {
                mockMvc
                    .perform(
                        post("/api/v1/jobs/sampleJob/trigger")
                            .header("X-API-Key", "test-key-full")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""{"parameters":{"traceId":"forged"}}"""),
                    )
                    .andExpect(status().isBadRequest)
                    .andExpect(jsonPath("$.code").value("COMMON_001"))
            }

            it("A-5 /admin/api 트리거도 키가 없으면 401 — 과거에는 무인증으로 통과했다") {
                mockMvc
                    .perform(post("/admin/api/jobs/sampleJob/trigger").contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized)
                    .andExpect(jsonPath("$.code").value("AUTH_001"))
            }

            it("A-7 같은 name 엔트리가 둘이면 좁은 키로 넓은 스코프를 얻지 못한다") {
                mockMvc
                    .perform(
                        post("/api/v1/jobs/sampleJob/trigger")
                            .header("X-API-Key", "test-key-dup-narrow")
                            .contentType(MediaType.APPLICATION_JSON),
                    )
                    .andExpect(status().isForbidden)
                    .andExpect(jsonPath("$.code").value("AUTH_002"))
            }

            it("A-8 /admin/api 트리거는 키 없이 401 — 조회 표면은 AdminApiReadAuthorizationIntegrationTest 가 본다") {
                mockMvc
                    .perform(post("/admin/api/jobs/sampleJob/trigger").contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized)
            }

            it("A-11 /api/v1/jobs 목록도 키 없이 401 — 죽은 excludePathPatterns 를 지워도 동작이 같다") {
                // 제거한 두 경로(/api/health, /api/v1/jobs/available)에는 대응 매핑이 없어
                // 제거 자체는 테스트로 관측할 수 없다. 여기서는 "지워도 /api/** 가 그대로
                // 덮는다" 만 확인한다. 제거 여부는 정적 확인으로 본다(검증 리포트).
                mockMvc
                    .perform(get("/api/v1/jobs"))
                    .andExpect(status().isUnauthorized)
            }

            it("A-10 허용된 키로 본문 없이 트리거하면 성공한다 — @RequestBody(required = false) 보존") {
                mockMvc
                    .perform(
                        post("/api/v1/jobs/sampleJob/trigger")
                            .header("X-API-Key", "test-key-full")
                            .contentType(MediaType.APPLICATION_JSON),
                    )
                    .andExpect(status().isAccepted)
            }
        }
    }
}