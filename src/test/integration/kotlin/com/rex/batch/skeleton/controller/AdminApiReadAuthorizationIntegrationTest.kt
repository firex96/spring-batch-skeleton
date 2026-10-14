package com.rex.batch.skeleton.controller

import com.rex.batch.skeleton.config.TestcontainersConfiguration
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping

/**
 * `/admin/api` **조회** 표면의 인증 계약.
 *
 * 이 표면은 오랫동안 무인증이었다 — 관리자 대시보드가 키 없이 호출하고 있어
 * 트리거만 닫고 조회는 열어 뒀다. 이 클래스가 그것이 닫혔음을 고정한다.
 *
 * **트리거 인가와 클래스를 나눈 이유**는 두 가지다.
 *
 * 1. 표면이 다르다 — 하나는 실행, 하나는 조회다.
 * 2. 장치 B 의 단언 신호가 **이 표면만** 가리켜야 한다. 한 클래스에 섞으면
 *    `isUnauthorized` 가 트리거 케이스에도 있어, 조회 단언이 통째로 사라져도
 *    장치 B 가 통과한다.
 *
 * 그래서 이 클래스에서 `isUnauthorized` 는 **R-1 에만** 나온다.
 * `/api/v1/jobs` 검사(A-11)를 여기 두지 않고 트리거 클래스에 둔 것도 같은 이유다.
 *
 * 테스트 실행:
 * ```bash
 * ./gradlew integrationTest --tests "*.controller.AdminApiReadAuthorizationIntegrationTest"
 * ```
 */
@SpringBootTest(
    properties = [
        "spring.autoconfigure.exclude=org.springframework.boot.quartz.autoconfigure.QuartzAutoConfiguration",
    ],
)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ContextConfiguration(initializers = [TestcontainersConfiguration.Initializer::class])
class AdminApiReadAuthorizationIntegrationTest(
    private val mockMvc: MockMvc,
    // actuator 가 controllerEndpointHandlerMapping 을 같은 타입으로 등록해 빈이 둘이다.
    // 이름으로 한정하지 않으면 컨텍스트 로딩부터 실패한다.
    @Qualifier("requestMappingHandlerMapping")
    private val handlerMapping: RequestMappingHandlerMapping,
) : DescribeSpec() {
    init {
        describe("/admin/api 조회 인증") {

            // 경로 하나만 보면 부족하다 — 인터셉터를 `/admin/api/dashboard/**` 처럼
            // 좁게 걸어도 통과한다. 조회 매핑 전수를 돈다.
            READ_PATHS.forEach { path ->
                it("R-1 키 없는 조회는 401 — $path") {
                    mockMvc
                        .perform(get(path))
                        .andExpect(status().isUnauthorized)
                }
            }

            it("R-2 유효한 키로는 조회가 된다 — 닫은 뒤에도 정상 경로가 산다") {
                // 이것이 없으면 키 검증이 깨져 "항상 401" 이어도 R-1 만으로는 초록불이다.
                mockMvc
                    .perform(get("/admin/api/dashboard/summary").header("X-API-Key", "test-key-full"))
                    .andExpect(status().isOk)
            }

            it("R-3 /admin HTML 셸은 키 없이 열려 있다 — 키를 입력할 화면에 도달해야 한다") {
                // 여기를 막으면 대시보드에 영원히 들어갈 수 없다.
                mockMvc
                    .perform(get("/admin"))
                    .andExpect(status().isOk)
            }

            it("R-4 조회 매핑 목록이 최신이다 — 새 엔드포인트가 생기면 READ_PATHS 를 갱신하라") {
                // R-1 은 손으로 적은 목록이라, 조회 엔드포인트가 새로 생겨도 그대로면
                // 그 경로는 검증되지 않는다. 실제 매핑과 대조해 목록이 낡는 것을 막는다.
                val discovered =
                    handlerMapping.handlerMethods.keys
                        .filter { info ->
                            val methods = info.methodsCondition.methods
                            methods.isEmpty() || RequestMethod.GET in methods
                        }.flatMap { info -> info.pathPatternsCondition?.patternValues.orEmpty() }
                        .filter { it.startsWith("/admin/api") }
                        .toSet()

                // 트리거는 POST 라 이 집합에 들어오지 않는다. 들어오면 필터 조건이 잘못된 것이다.
                discovered shouldBe EXPECTED_READ_PATTERNS
            }
        }
    }

    companion object {
        /** R-1 이 실제로 호출하는 경로. `{id}` 자리에는 값을 채운다. */
        private val READ_PATHS =
            listOf(
                "/admin/api/dashboard/summary",
                "/admin/api/dashboard/daily-stats",
                "/admin/api/executions",
                // 인터셉터가 핸들러보다 앞서므로 없는 id 라도 401 이 먼저 난다.
                "/admin/api/executions/1",
                "/admin/api/errors",
                "/admin/api/schedules",
                "/admin/api/webhooks",
                "/admin/api/jobs",
            )

        /** R-4 가 대조하는 매핑 패턴. 자리표시자를 그대로 담으므로 [READ_PATHS] 와 형태가 다르다. */
        private val EXPECTED_READ_PATTERNS =
            setOf(
                "/admin/api/dashboard/summary",
                "/admin/api/dashboard/daily-stats",
                "/admin/api/executions",
                "/admin/api/executions/{id}",
                "/admin/api/errors",
                "/admin/api/schedules",
                "/admin/api/webhooks",
                "/admin/api/jobs",
            )
    }
}