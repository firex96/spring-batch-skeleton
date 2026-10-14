package com.rex.batch.skeleton.controller

import com.rex.batch.skeleton.config.TestcontainersConfiguration
import com.rex.batch.skeleton.dto.batch.JobErrorLogDto
import com.rex.batch.skeleton.dto.batch.JobExecutionSummaryDto
import com.rex.batch.skeleton.dto.batch.JobScheduleConfigDto
import com.rex.batch.skeleton.dto.batch.WebhookTriggerHistoryDto
import com.rex.batch.skeleton.dto.webhook.JobExecutionDto
import com.rex.batch.skeleton.dto.webhook.StepExecutionDto
import com.rex.batch.skeleton.persistence.jpa.service.batch.JobErrorLogJpaEntityService
import com.rex.batch.skeleton.persistence.jpa.service.batch.JobExecutionSummaryJpaEntityService
import com.rex.batch.skeleton.persistence.jpa.service.batch.JobScheduleConfigJpaEntityService
import com.rex.batch.skeleton.persistence.jpa.service.batch.WebhookTriggerHistoryJpaEntityService
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.springframework.batch.core.job.parameters.JobParameters
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.infrastructure.item.ExecutionContext
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import tools.jackson.databind.json.JsonMapper
import java.time.LocalDate
import kotlin.reflect.full.memberProperties

/**
 * 관리자 API 7 엔드포인트의 **응답 키 집합 = DTO 프로퍼티명 집합** — 응답이 DTO 에서 Model 로 바뀌어도 JSON 계약이 그대로임을 고정한다.
 * 시드는 EntityService·`JobRepository`(Spring Batch 6: createJobInstance → createJobExecution → createStepExecution)로 넣고 finally 에서 지운다.
 * 인증 계약은 `AdminApiReadAuthorizationIntegrationTest` — 여기서는 유효 키만 쓴다.
 *
 * 테스트 실행:
 * ```bash
 * ./gradlew integrationTest --tests "*.controller.AdminApiContractIntegrationTest"
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
class AdminApiContractIntegrationTest(
    private val mockMvc: MockMvc,
    private val jsonMapper: JsonMapper,
    private val jobRepository: JobRepository,
    private val summaryService: JobExecutionSummaryJpaEntityService,
    private val errorLogService: JobErrorLogJpaEntityService,
    private val scheduleService: JobScheduleConfigJpaEntityService,
    private val webhookService: WebhookTriggerHistoryJpaEntityService,
) : DescribeSpec() {
    private fun getJson(path: String): Any? {
        val body =
            mockMvc
                .perform(get(path).header("X-API-Key", "test-key-full"))
                .andExpect(status().isOk)
                .andReturn()
                .response
                .contentAsString
        return jsonMapper.readValue(body, Any::class.java)
    }

    @Suppress("UNCHECKED_CAST")
    private fun keysOfFirst(json: Any?): Set<String> = ((json as List<Map<String, Any?>>).first()).keys

    @Suppress("UNCHECKED_CAST")
    private fun keysOf(json: Any?): Set<String> = (json as Map<String, Any?>).keys

    private inline fun <reified T : Any> propertyNames(): Set<String> = T::class.memberProperties.map { it.name }.toSet()

    init {
        describe("/admin/api 응답 키 집합은 DTO 프로퍼티명 집합과 같다") {
            it("summary · daily-stats · executions · executions/{id} · errors · schedules · webhooks — 7 엔드포인트") {
                val jobName = "contractProbeJob"
                val instance = jobRepository.createJobInstance(jobName, JobParameters())
                val execution = jobRepository.createJobExecution(instance, JobParameters(), ExecutionContext())
                // 상태는 갱신하지 않는다(STARTING 그대로) — update 로 버전이 오르면 같은 객체로 지울 때 낙관적 잠금에 걸린다. 키 집합 비교에는 상태가 필요 없다
                val step = jobRepository.createStepExecution("contractProbeStep", execution)

                val summary = summaryService.create(JobExecutionSummaryDto(jobName = jobName, summaryDate = LocalDate.now(), totalExecutions = 1, completedCount = 1))
                val errorLog =
                    errorLogService.create(
                        JobErrorLogDto(jobExecutionId = execution.id, jobName = jobName, errorType = "ProbeException", errorMessage = "probe"),
                    )
                val schedule = scheduleService.create(JobScheduleConfigDto(jobName = jobName, cronExpression = "0 0 5 * * ?", isEnabled = true))
                val webhook =
                    webhookService.create(
                        WebhookTriggerHistoryDto(jobName = jobName, jobExecutionId = execution.id, traceId = "probe-trace", triggerSource = "WEBHOOK", status = "SUCCESS"),
                    )
                try {
                    keysOf(getJson("/admin/api/dashboard/summary")) shouldContainExactly
                        setOf("totalJobs", "totalExecutionsToday", "completedToday", "failedToday", "runningNow", "scheduledJobs", "lastUpdated")
                    keysOfFirst(getJson("/admin/api/dashboard/daily-stats?days=7")) shouldContainExactly
                        setOf("date", "totalExecutions", "completedCount", "failedCount", "avgDurationMs")

                    val executions = getJson("/admin/api/executions?jobName=$jobName")
                    keysOfFirst(executions) shouldContainExactly propertyNames<JobExecutionDto>()
                    @Suppress("UNCHECKED_CAST")
                    val detail = getJson("/admin/api/executions/${execution.id}") as Map<String, Any?>
                    detail.keys shouldContainExactly propertyNames<JobExecutionDto>()
                    @Suppress("UNCHECKED_CAST")
                    val steps = detail["stepExecutions"] as List<Map<String, Any?>>
                    steps.first().keys shouldContainExactly propertyNames<StepExecutionDto>()
                    detail["jobName"] shouldBe jobName

                    keysOfFirst(getJson("/admin/api/errors?jobName=$jobName")) shouldContainExactly propertyNames<JobErrorLogDto>()
                    keysOfFirst(getJson("/admin/api/schedules")) shouldContainExactly propertyNames<JobScheduleConfigDto>()
                    keysOfFirst(getJson("/admin/api/webhooks?jobName=$jobName")) shouldContainExactly propertyNames<WebhookTriggerHistoryDto>()
                } finally {
                    webhookService.delete(webhook.id)
                    scheduleService.delete(schedule.id)
                    errorLogService.delete(errorLog.id)
                    summaryService.delete(summary.id)
                    // 인스턴스 삭제가 실행·스텝을 DB 에서 다시 읽어 함께 지운다(낙관적 잠금 버전을 DB 값으로)
                    jobRepository.deleteJobInstance(instance)
                }
            }
        }
    }
}