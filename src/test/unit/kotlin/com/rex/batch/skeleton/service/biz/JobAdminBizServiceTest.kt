package com.rex.batch.skeleton.service.biz

import com.rex.batch.skeleton.dto.batch.JobScheduleConfigDto
import com.rex.batch.skeleton.persistence.jpa.service.batch.JobErrorLogJpaEntityService
import com.rex.batch.skeleton.persistence.jpa.service.batch.JobExecutionSummaryJpaEntityService
import com.rex.batch.skeleton.persistence.jpa.service.batch.JobScheduleConfigJpaEntityService
import com.rex.batch.skeleton.persistence.jpa.service.batch.WebhookTriggerHistoryJpaEntityService
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.job.JobExecution
import org.springframework.batch.core.job.JobInstance
import org.springframework.batch.core.job.parameters.JobParameters
import org.springframework.batch.core.repository.explore.JobExplorer
import java.time.LocalDateTime

/**
 * `JobAdminBizService` 의 집계·필터 — `AdminMappingService` 에서 이관한 조회 로직이 그대로 동작하는지.
 * JA-1 대시보드 집계(오늘 실행·완료·실패·실행 중·스케줄 수) · JA-2 실행 목록의 jobName·status 필터와 페이지 · JA-3 없는 실행 id → null.
 */
class JobAdminBizServiceTest : BehaviorSpec({
    val jobExplorer = mockk<JobExplorer>()
    val summaryService = mockk<JobExecutionSummaryJpaEntityService>()
    val errorLogService = mockk<JobErrorLogJpaEntityService>()
    val scheduleService = mockk<JobScheduleConfigJpaEntityService>()
    val webhookService = mockk<WebhookTriggerHistoryJpaEntityService>()
    val service = JobAdminBizService(jobExplorer, summaryService, errorLogService, scheduleService, webhookService)

    fun execution(id: Long, instance: JobInstance, status: BatchStatus, startTime: LocalDateTime?): JobExecution =
        JobExecution(id, instance, JobParameters()).apply {
            setStatus(status)
            startTime?.let { setStartTime(it) }
            if (status == BatchStatus.COMPLETED || status == BatchStatus.FAILED) startTime?.let { setEndTime(it.plusSeconds(1)) }
        }

    Given("오늘 실행 3(완료 2·실패 1)·실행 중 1·어제 실행 1 인 sampleJob 과 활성 스케줄 2") {
        val instance = JobInstance(1L, "sampleJob")
        val today = LocalDateTime.now().withHour(9)
        every { jobExplorer.jobNames } returns listOf("sampleJob")
        every { jobExplorer.findJobInstancesByName("sampleJob", 0, 100) } returns listOf(instance)
        every { jobExplorer.getJobExecutions(instance) } returns
            listOf(
                execution(1L, instance, BatchStatus.COMPLETED, today),
                execution(2L, instance, BatchStatus.COMPLETED, today.plusMinutes(1)),
                execution(3L, instance, BatchStatus.FAILED, today.plusMinutes(2)),
                execution(4L, instance, BatchStatus.STARTED, today.plusMinutes(3)),
                execution(5L, instance, BatchStatus.COMPLETED, today.minusDays(1)),
            )
        every { scheduleService.getAllEnabled() } returns
            listOf(
                JobScheduleConfigDto(jobName = "a", cronExpression = "0 0 3 * * ?"),
                JobScheduleConfigDto(jobName = "b", cronExpression = "0 0 4 * * ?"),
            )

        Then("JA-1 totalJobs 1 · 오늘 4 · 완료 2 · 실패 1 · 실행 중 1(이전엔 오늘 시작한 실행이 두 번 세어져 2) · 스케줄 2") {
            val summary = service.getDashboardSummary()
            summary.totalJobs shouldBe 1
            summary.totalExecutionsToday shouldBe 4
            summary.completedToday shouldBe 2
            summary.failedToday shouldBe 1
            summary.runningNow shouldBe 1
            summary.scheduledJobs shouldBe 2
        }
    }

    Given("sampleJob 실행 4 — COMPLETED 3 · FAILED 1") {
        val instance = JobInstance(1L, "sampleJob")
        val t = LocalDateTime.of(2026, 9, 17, 9, 0)
        every { jobExplorer.jobNames } returns listOf("sampleJob")
        every { jobExplorer.findJobInstancesByName("sampleJob", 0, 1000) } returns listOf(instance)
        every { jobExplorer.getJobExecutions(instance) } returns
            listOf(
                execution(1L, instance, BatchStatus.COMPLETED, t),
                execution(2L, instance, BatchStatus.COMPLETED, t.plusMinutes(1)),
                execution(3L, instance, BatchStatus.FAILED, t.plusMinutes(2)),
                execution(4L, instance, BatchStatus.COMPLETED, t.plusMinutes(3)),
            )

        Then("JA-2 status=COMPLETED 필터 3, 최신순 정렬, page=1·size=2 → 1개(가장 오래된 것)") {
            val all = service.getJobExecutions(page = 0, size = 20, jobName = "sampleJob", status = "COMPLETED")
            all.map { it.jobExecutionId } shouldBe listOf(4L, 2L, 1L)
            val paged = service.getJobExecutions(page = 1, size = 2, jobName = "sampleJob", status = "COMPLETED")
            paged.map { it.jobExecutionId } shouldBe listOf(1L)
            all.first().stepExecutions shouldBe emptyList()
        }
    }

    Given("없는 실행 id") {
        every { jobExplorer.getJobExecution(999L) } returns null

        Then("JA-3 null 을 돌려준다(컨트롤러가 404 로 바꾼다)") {
            service.getJobExecution(999L) shouldBe null
        }
    }
})