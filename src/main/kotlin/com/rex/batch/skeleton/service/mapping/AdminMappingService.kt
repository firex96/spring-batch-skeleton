package com.rex.batch.skeleton.service.mapping

import com.rex.batch.skeleton.mapper.AdminMapper
import com.rex.batch.skeleton.model.admin.DailyStatsModel
import com.rex.batch.skeleton.model.admin.DashboardSummaryModel
import com.rex.batch.skeleton.model.admin.JobErrorLogModel
import com.rex.batch.skeleton.model.admin.JobExecutionModel
import com.rex.batch.skeleton.model.admin.JobScheduleConfigModel
import com.rex.batch.skeleton.model.admin.WebhookTriggerHistoryModel
import com.rex.batch.skeleton.model.webhook.JobTriggerResponseModel
import com.rex.batch.skeleton.service.biz.JobAdminBizService
import com.rex.batch.skeleton.service.biz.JobLauncherBizService
import org.springframework.stereotype.Service
import java.time.LocalDateTime

/**
 * Admin API용 매핑 서비스
 *
 * Admin Dashboard REST API 응답을 위한 매핑 서비스입니다 — 조회·집계는 `JobAdminBizService`, 기동은 `JobLauncherBizService`, 여기서는 Model 변환만 한다.
 */
@Service
class AdminMappingService(
    private val jobAdminBizService: JobAdminBizService,
    private val jobLauncherBizService: JobLauncherBizService,
    private val mapper: AdminMapper,
) {
    fun getDashboardSummary(): DashboardSummaryModel = mapper.toModel(jobAdminBizService.getDashboardSummary())

    fun getDailyStats(days: Int = 7): List<DailyStatsModel> = mapper.toDailyStatsModelList(jobAdminBizService.getDailyStats(days))

    fun getJobExecutions(
        page: Int = 0,
        size: Int = 20,
        jobName: String? = null,
        status: String? = null,
    ): List<JobExecutionModel> = mapper.toJobExecutionModelList(jobAdminBizService.getJobExecutions(page, size, jobName, status))

    fun getJobExecution(jobExecutionId: Long): JobExecutionModel? =
        jobAdminBizService.getJobExecution(jobExecutionId)?.let { mapper.toJobExecutionModel(it) }

    fun getErrorLogs(
        page: Int = 0,
        size: Int = 20,
        jobName: String? = null,
    ): List<JobErrorLogModel> = mapper.toJobErrorLogModelList(jobAdminBizService.getErrorLogs(page, size, jobName))

    fun getScheduleConfigs(): List<JobScheduleConfigModel> = mapper.toJobScheduleConfigModelList(jobAdminBizService.getScheduleConfigs())

    fun getWebhookTriggerHistory(
        page: Int = 0,
        size: Int = 20,
        jobName: String? = null,
    ): List<WebhookTriggerHistoryModel> = mapper.toWebhookTriggerHistoryModelList(jobAdminBizService.getWebhookTriggerHistory(page, size, jobName))

    fun getAvailableJobs(): List<String> = jobLauncherBizService.getAvailableJobs()

    /** 기동 결과를 트리거 응답 Model 로 — `JobMappingService.triggerJob` 과 같은 조립(컨트롤러에서 이관). */
    fun triggerJob(
        jobName: String,
        parameters: Map<String, String>?,
    ): JobTriggerResponseModel {
        val dto = jobLauncherBizService.triggerJob(jobName, parameters)
        return JobTriggerResponseModel(
            jobExecutionId = dto.jobExecutionId,
            jobName = dto.jobName,
            traceId = dto.traceId ?: "",
            triggeredAt = dto.startTime ?: LocalDateTime.now(),
        )
    }
}