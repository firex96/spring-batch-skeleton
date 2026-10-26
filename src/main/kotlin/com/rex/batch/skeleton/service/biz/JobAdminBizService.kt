package com.rex.batch.skeleton.service.biz

import com.rex.batch.skeleton.dto.admin.DailyStatsDto
import com.rex.batch.skeleton.dto.admin.DashboardSummaryDto
import com.rex.batch.skeleton.dto.batch.JobErrorLogDto
import com.rex.batch.skeleton.dto.batch.JobScheduleConfigDto
import com.rex.batch.skeleton.dto.batch.WebhookTriggerHistoryDto
import com.rex.batch.skeleton.dto.webhook.JobExecutionDto
import com.rex.batch.skeleton.dto.webhook.StepExecutionDto
import com.rex.batch.skeleton.persistence.jpa.service.batch.JobErrorLogJpaEntityService
import com.rex.batch.skeleton.persistence.jpa.service.batch.JobExecutionSummaryJpaEntityService
import com.rex.batch.skeleton.persistence.jpa.service.batch.JobScheduleConfigJpaEntityService
import com.rex.batch.skeleton.persistence.jpa.service.batch.WebhookTriggerHistoryJpaEntityService
import com.rex.batch.skeleton.util.requireJobExecution
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.repository.explore.JobExplorer
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * 관리자 대시보드 조회·집계 비즈니스 서비스.
 *
 * `AdminMappingService` 가 `JobExplorer`·EntityService 를 직접 부르던 것을 이 계층으로 내렸다(계층 규약: MappingService 는 BizService·Mapper·Properties 만).
 * 배치 메타 테이블(Spring Batch `BATCH_*`·`job_*`)은 전부 default 데이터소스라 읽기 트랜잭션은 `batchTransactionManager`.
 */
@Service
@Transactional(readOnly = true, transactionManager = "batchTransactionManager")
class JobAdminBizService(
    private val jobExplorer: JobExplorer,
    private val jobExecutionSummaryJpaEntityService: JobExecutionSummaryJpaEntityService,
    private val jobErrorLogJpaEntityService: JobErrorLogJpaEntityService,
    private val jobScheduleConfigJpaEntityService: JobScheduleConfigJpaEntityService,
    private val webhookTriggerHistoryJpaEntityService: WebhookTriggerHistoryJpaEntityService,
) {
    /**
     * 대시보드 요약 통계 조회
     */
    fun getDashboardSummary(): DashboardSummaryDto {
        val jobNames = jobExplorer.jobNames
        val today = LocalDate.now()
        val todayStart = today.atStartOfDay()
        val todayEnd = today.atTime(LocalTime.MAX)

        var totalExecutionsToday = 0
        var completedToday = 0
        var failedToday = 0
        var runningNow = 0

        jobNames.forEach { jobName ->
            jobExplorer.findJobInstancesByName(jobName, 0, 100).forEach { jobInstance ->
                jobExplorer.getJobExecutions(jobInstance).forEach { execution ->
                    val startTime = execution.startTime
                    if (startTime != null && startTime >= todayStart && startTime <= todayEnd) {
                        totalExecutionsToday++
                        when (execution.status) {
                            BatchStatus.COMPLETED -> completedToday++
                            BatchStatus.FAILED -> failedToday++
                            else -> {}
                        }
                    }
                    // 실행 중은 날짜와 무관하게 한 번만 센다 — 이전엔 오늘 시작한 실행이 위 분기에서도 세어져 두 번 잡혔다
                    if (execution.status == BatchStatus.STARTED || execution.status == BatchStatus.STARTING) {
                        runningNow++
                    }
                }
            }
        }

        val scheduledJobs = jobScheduleConfigJpaEntityService.getAllEnabled().size

        return DashboardSummaryDto(
            totalJobs = jobNames.size,
            totalExecutionsToday = totalExecutionsToday,
            completedToday = completedToday,
            failedToday = failedToday,
            runningNow = runningNow,
            scheduledJobs = scheduledJobs,
            lastUpdated = LocalDateTime.now(),
        )
    }

    /**
     * 일별 통계 조회 (최근 N일)
     */
    fun getDailyStats(days: Int = 7): List<DailyStatsDto> {
        val endDate = LocalDate.now()
        val startDate = endDate.minusDays(days.toLong() - 1)

        val summaries = jobExecutionSummaryJpaEntityService.getAllBySummaryDateBetween(startDate, endDate)

        // 날짜별로 그룹화하여 집계
        val statsByDate =
            summaries
                .filter { it.summaryHour == null } // 일별 통계만
                .groupBy { it.summaryDate }
                .mapValues { (_, list) ->
                    DailyStatsDto(
                        date = list.first().summaryDate,
                        totalExecutions = list.sumOf { it.totalExecutions },
                        completedCount = list.sumOf { it.completedCount },
                        failedCount = list.sumOf { it.failedCount },
                        avgDurationMs = list.mapNotNull { it.avgDurationMs }.average().takeIf { !it.isNaN() }?.toLong(),
                    )
                }

        // 날짜 범위 내 모든 날짜에 대해 결과 생성 (데이터 없는 날짜도 포함)
        return (0 until days).map { offset ->
            val date = startDate.plusDays(offset.toLong())
            statsByDate[date] ?: DailyStatsDto(date = date)
        }
    }

    /**
     * Job 실행 목록 조회 (페이지네이션)
     */
    fun getJobExecutions(
        page: Int = 0,
        size: Int = 20,
        jobName: String? = null,
        status: String? = null,
    ): List<JobExecutionDto> {
        val jobNames = if (jobName != null) listOf(jobName) else jobExplorer.jobNames.toList()
        val executions = mutableListOf<JobExecutionDto>()

        jobNames.forEach { name ->
            jobExplorer.findJobInstancesByName(name, 0, 1000).forEach { instance ->
                jobExplorer.getJobExecutions(instance).forEach { execution ->
                    if (status == null || execution.status.name == status) {
                        executions.add(toJobExecutionDto(execution))
                    }
                }
            }
        }

        // 최신순 정렬 후 페이지네이션 적용
        return executions
            .sortedByDescending { it.startTime }
            .drop(page * size)
            .take(size)
    }

    /**
     * Job 실행 상세 조회
     */
    fun getJobExecution(jobExecutionId: Long): JobExecutionDto {
        val execution = jobExplorer.requireJobExecution(jobExecutionId)
        return toJobExecutionDto(execution)
    }

    /**
     * 에러 로그 목록 조회
     */
    fun getErrorLogs(
        page: Int = 0,
        size: Int = 20,
        jobName: String? = null,
    ): List<JobErrorLogDto> {
        val startTime = LocalDateTime.now().minusDays(30)
        val endTime = LocalDateTime.now()

        val logs =
            if (jobName != null) {
                jobErrorLogJpaEntityService.getAllByJobNameAndOccurredAtBetween(jobName, startTime, endTime)
            } else {
                jobErrorLogJpaEntityService.getAllByOccurredAtBetween(startTime, endTime)
            }

        return logs
            .sortedByDescending { it.occurredAt }
            .drop(page * size)
            .take(size)
    }

    /**
     * 스케줄 설정 목록 조회
     */
    fun getScheduleConfigs(): List<JobScheduleConfigDto> = jobScheduleConfigJpaEntityService.getAll()

    /**
     * Webhook 트리거 이력 조회
     */
    fun getWebhookTriggerHistory(
        page: Int = 0,
        size: Int = 20,
        jobName: String? = null,
    ): List<WebhookTriggerHistoryDto> {
        val startTime = LocalDateTime.now().minusDays(30)
        val endTime = LocalDateTime.now()

        val histories = webhookTriggerHistoryJpaEntityService.getAllByTriggeredAtBetween(startTime, endTime)

        return histories
            .filter { jobName == null || it.jobName == jobName }
            .sortedByDescending { it.triggeredAt }
            .drop(page * size)
            .take(size)
    }

    /**
     * 사용 가능한 Job 목록 조회
     */
    private fun toJobExecutionDto(execution: org.springframework.batch.core.job.JobExecution): JobExecutionDto {
        val traceId = execution.jobParameters.getString("traceId")

        val parameters =
            execution.jobParameters.parameters()
                .filter { it.name() != "traceId" && it.name() != "timestamp" }
                .associate { it.name() to (it.value()?.toString() ?: "") }

        return JobExecutionDto(
            jobExecutionId = execution.id,
            jobName = execution.jobInstance.jobName,
            status = execution.status.name,
            traceId = traceId,
            startTime = execution.startTime,
            endTime = execution.endTime,
            exitCode = execution.exitStatus.exitCode,
            exitDescription = execution.exitStatus.exitDescription.takeIf { it.isNotBlank() },
            parameters = parameters,
            stepExecutions =
            execution.stepExecutions.map { step ->
                StepExecutionDto(
                    stepExecutionId = step.id,
                    stepName = step.stepName,
                    status = step.status.name,
                    readCount = step.readCount,
                    writeCount = step.writeCount,
                    commitCount = step.commitCount,
                    rollbackCount = step.rollbackCount,
                    filterCount = step.filterCount,
                    skipCount = step.readSkipCount + step.processSkipCount + step.writeSkipCount,
                    startTime = step.startTime,
                    endTime = step.endTime,
                    exitCode = step.exitStatus.exitCode,
                    exitDescription = step.exitStatus.exitDescription.takeIf { it.isNotBlank() },
                )
            },
        )
    }
}