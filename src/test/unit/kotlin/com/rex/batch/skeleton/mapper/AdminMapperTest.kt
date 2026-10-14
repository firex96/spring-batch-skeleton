package com.rex.batch.skeleton.mapper

import com.rex.batch.skeleton.dto.admin.DailyStatsDto
import com.rex.batch.skeleton.dto.admin.DashboardSummaryDto
import com.rex.batch.skeleton.dto.batch.JobErrorLogDto
import com.rex.batch.skeleton.dto.batch.JobScheduleConfigDto
import com.rex.batch.skeleton.dto.batch.WebhookTriggerHistoryDto
import com.rex.batch.skeleton.dto.webhook.JobExecutionDto
import com.rex.batch.skeleton.dto.webhook.StepExecutionDto
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import org.mapstruct.factory.Mappers
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * 관리자 API 응답이 DTO → Model 로 바뀌면서 필드값이 하나도 어긋나지 않는지 — 관리자 API 계약 무변경의 매퍼 단위 증명.
 * `is`-prefix(`isEnabled`) 는 true·false 둘 다, nullable 은 null 유지, 중첩(`stepExecutions`) 포함.
 */
class AdminMapperTest : BehaviorSpec({
    val mapper = Mappers.getMapper(AdminMapper::class.java)
    val t0 = LocalDateTime.of(2026, 9, 17, 10, 0)

    Given("DashboardSummaryDto") {
        val dto = DashboardSummaryDto(totalJobs = 3, totalExecutionsToday = 5, completedToday = 4, failedToday = 1, runningNow = 0, scheduledJobs = 2, lastUpdated = t0)
        Then("AM-1 모든 필드가 그대로 옮겨진다") {
            val m = mapper.toModel(dto)
            m.totalJobs shouldBe 3
            m.totalExecutionsToday shouldBe 5
            m.completedToday shouldBe 4
            m.failedToday shouldBe 1
            m.runningNow shouldBe 0
            m.scheduledJobs shouldBe 2
            m.lastUpdated shouldBe t0
        }
    }

    Given("DailyStatsDto — 데이터 없는 날(기본값)과 있는 날") {
        val empty = DailyStatsDto(date = LocalDate.of(2026, 9, 16))
        val full = DailyStatsDto(date = LocalDate.of(2026, 9, 17), totalExecutions = 7, completedCount = 6, failedCount = 1, avgDurationMs = 1234L)
        Then("AM-2 0·null 과 실제 값이 그대로 옮겨진다") {
            val models = mapper.toDailyStatsModelList(listOf(empty, full))
            models[0].totalExecutions shouldBe 0
            models[0].avgDurationMs shouldBe null
            models[1].date shouldBe LocalDate.of(2026, 9, 17)
            models[1].completedCount shouldBe 6
            models[1].avgDurationMs shouldBe 1234L
        }
    }

    Given("JobExecutionDto — 중첩 StepExecutionDto 포함") {
        val step =
            StepExecutionDto(
                stepExecutionId = 11L,
                stepName = "sampleStep",
                status = "COMPLETED",
                readCount = 10,
                writeCount = 9,
                commitCount = 1,
                rollbackCount = 0,
                filterCount = 1,
                skipCount = 0,
                startTime = t0,
                endTime = t0.plusSeconds(3),
                exitCode = "COMPLETED",
                exitDescription = null,
            )
        val dto =
            JobExecutionDto(
                jobExecutionId = 1L,
                jobName = "sampleJob",
                status = "COMPLETED",
                traceId = "trace-1",
                startTime = t0,
                endTime = t0.plusSeconds(5),
                exitCode = "COMPLETED",
                exitDescription = null,
                parameters = mapOf("branch" to "A"),
                stepExecutions = listOf(step),
            )
        Then("AM-3 Job·Step 필드가 그대로 옮겨진다") {
            val m = mapper.toJobExecutionModel(dto)
            m.jobExecutionId shouldBe 1L
            m.jobName shouldBe "sampleJob"
            m.traceId shouldBe "trace-1"
            m.exitDescription shouldBe null
            m.parameters shouldBe mapOf("branch" to "A")
            m.stepExecutions.single().stepExecutionId shouldBe 11L
            m.stepExecutions.single().stepName shouldBe "sampleStep"
            m.stepExecutions.single().readCount shouldBe 10L
            m.stepExecutions.single().skipCount shouldBe 0L
            m.stepExecutions.single().endTime shouldBe t0.plusSeconds(3)
        }
    }

    Given("JobErrorLogDto") {
        val dto =
            JobErrorLogDto(
                id = 5L,
                jobExecutionId = 1L,
                stepExecutionId = null,
                jobName = "sampleJob",
                stepName = null,
                errorType = "RuntimeException",
                errorCode = "E1",
                errorMessage = "boom",
                stackTrace = null,
                itemInfo = null,
                occurredAt = t0,
                createdAt = t0,
            )
        Then("AM-4 nullable 은 null 로, 값은 그대로") {
            val m = mapper.toModel(dto)
            m.id shouldBe 5L
            m.stepExecutionId shouldBe null
            m.errorCode shouldBe "E1"
            m.errorMessage shouldBe "boom"
            m.occurredAt shouldBe t0
        }
    }

    Given("JobScheduleConfigDto — isEnabled true·false") {
        val enabled = JobScheduleConfigDto(id = 1L, jobName = "a", cronExpression = "0 0 3 * * ?", isEnabled = true, createdAt = t0, updatedAt = t0)
        val disabled = JobScheduleConfigDto(id = 2L, jobName = "b", cronExpression = "0 0 4 * * ?", isEnabled = false, createdAt = t0, updatedAt = t0)
        Then("AM-5 is-prefix Boolean 이 둘 다 그대로 옮겨진다(명시 매핑이 없으면 항상 false)") {
            mapper.toModel(enabled).isEnabled shouldBe true
            mapper.toModel(disabled).isEnabled shouldBe false
            mapper.toModel(enabled).timezone shouldBe "Asia/Seoul"
            mapper.toModel(enabled).nextFireTime shouldBe null
        }
    }

    Given("WebhookTriggerHistoryDto") {
        val dto =
            WebhookTriggerHistoryDto(
                id = 9L,
                jobName = "sampleJob",
                jobExecutionId = 1L,
                traceId = "trace-9",
                triggerSource = "WEBHOOK",
                apiKeyName = "test-key-full",
                clientIp = "127.0.0.1",
                requestParameters = null,
                status = "SUCCESS",
                errorMessage = null,
                triggeredAt = t0,
                completedAt = t0.plusSeconds(1),
                createdAt = t0,
                updatedAt = t0,
            )
        Then("AM-6 모든 필드가 그대로 옮겨진다") {
            val m = mapper.toModel(dto)
            m.id shouldBe 9L
            m.apiKeyName shouldBe "test-key-full"
            m.requestParameters shouldBe null
            m.status shouldBe "SUCCESS"
            m.completedAt shouldBe t0.plusSeconds(1)
        }
    }
})