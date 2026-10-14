package com.rex.batch.skeleton.controller

import com.rex.batch.skeleton.config.TestcontainersConfiguration
import com.rex.batch.skeleton.dto.batch.JobScheduleConfigDto
import com.rex.batch.skeleton.persistence.jpa.service.batch.JobScheduleConfigJpaEntityService
import io.kotest.core.spec.style.DescribeSpec
import org.hamcrest.Matchers.contains
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * `/admin/api/schedules` 의 `isEnabled` 가 DB 값 그대로 내려가는지 — Entity → DTO → JSON 전 구간.
 *
 * `JobScheduleConfigMapper.toDto` 에 `@Mapping(source = "enabled", target = "isEnabled")` 가 없으면
 * `is`-prefix getter(`isEnabled()` → 소스 프로퍼티명 `enabled`)와 생성자 파라미터 `isEnabled` 가 어긋나
 * **모든 스케줄이 비활성으로 표시**됐다(IGNORE 정책이 덮고 있던 결함). 유닛 `JobScheduleConfigMapperTest` 가 매퍼를,
 * 이 클래스가 HTTP 응답을 고정한다. 인증 계약은 `AdminApiReadAuthorizationIntegrationTest` 가 본다 — 여기서는 유효 키만 쓴다.
 *
 * 테스트 실행:
 * ```bash
 * ./gradlew integrationTest --tests "*.controller.AdminApiScheduleConfigIntegrationTest"
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
class AdminApiScheduleConfigIntegrationTest(
    private val mockMvc: MockMvc,
    private val scheduleConfigService: JobScheduleConfigJpaEntityService,
) : DescribeSpec() {
    init {
        describe("/admin/api/schedules isEnabled 왕복") {

            it("활성 · 비활성 설정이 각각 true · false 로 내려간다 — 이전엔 둘 다 false") {
                val enabled =
                    scheduleConfigService.create(
                        JobScheduleConfigDto(jobName = "isPrefixProbeEnabledJob", cronExpression = "0 0 3 * * ?", isEnabled = true),
                    )
                val disabled =
                    scheduleConfigService.create(
                        JobScheduleConfigDto(jobName = "isPrefixProbeDisabledJob", cronExpression = "0 0 4 * * ?", isEnabled = false),
                    )
                try {
                    mockMvc
                        .perform(get("/admin/api/schedules").header("X-API-Key", "test-key-full"))
                        .andExpect(status().isOk)
                        // 필터 경로는 리스트를 돌려주므로 hamcrest contains 로 단언한다
                        .andExpect(jsonPath("$[?(@.jobName == 'isPrefixProbeEnabledJob')].isEnabled", contains(true)))
                        .andExpect(jsonPath("$[?(@.jobName == 'isPrefixProbeDisabledJob')].isEnabled", contains(false)))
                } finally {
                    scheduleConfigService.delete(enabled.id)
                    scheduleConfigService.delete(disabled.id)
                }
            }
        }
    }
}