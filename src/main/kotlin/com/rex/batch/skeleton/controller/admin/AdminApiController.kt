package com.rex.batch.skeleton.controller.admin

import com.rex.batch.skeleton.config.webhook.JobTriggerAuthorizer
import com.rex.batch.skeleton.model.admin.DailyStatsModel
import com.rex.batch.skeleton.model.admin.DashboardSummaryModel
import com.rex.batch.skeleton.model.admin.JobErrorLogModel
import com.rex.batch.skeleton.model.admin.JobExecutionModel
import com.rex.batch.skeleton.model.admin.JobScheduleConfigModel
import com.rex.batch.skeleton.model.admin.WebhookTriggerHistoryModel
import com.rex.batch.skeleton.model.webhook.JobTriggerRequestModel
import com.rex.batch.skeleton.model.webhook.JobTriggerResponseModel
import com.rex.batch.skeleton.service.mapping.AdminMappingService
import com.rex.batch.skeleton.util.logger
import io.swagger.v3.oas.annotations.Hidden
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Hidden
@RestController
@RequestMapping("/admin/api")
class AdminApiController(
    private val jobTriggerAuthorizer: JobTriggerAuthorizer,
    private val adminMappingService: AdminMappingService,
) {
    private val log = logger()

    @GetMapping("/dashboard/summary")
    fun getDashboardSummary(): ResponseEntity<DashboardSummaryModel> {
        log.debug("Dashboard summary requested")
        val summary = adminMappingService.getDashboardSummary()
        return ResponseEntity.ok(summary)
    }

    @GetMapping("/dashboard/daily-stats")
    fun getDailyStats(
        @RequestParam(defaultValue = "7") days: Int,
    ): ResponseEntity<List<DailyStatsModel>> {
        log.debug("Daily stats requested - days={}", days)
        val stats = adminMappingService.getDailyStats(days.coerceIn(1, 30))
        return ResponseEntity.ok(stats)
    }

    @GetMapping("/executions")
    fun getJobExecutions(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) jobName: String?,
        @RequestParam(required = false) status: String?,
    ): ResponseEntity<List<JobExecutionModel>> {
        log.debug("Job executions requested - page={}, size={}, jobName={}, status={}", page, size, jobName, status)
        val executions = adminMappingService.getJobExecutions(page, size.coerceIn(1, 100), jobName, status)
        return ResponseEntity.ok(executions)
    }

    @GetMapping("/executions/{id}")
    fun getJobExecution(
        @PathVariable id: Long,
    ): ResponseEntity<JobExecutionModel> {
        log.debug("Job execution detail requested - id={}", id)
        return ResponseEntity.ok(adminMappingService.getJobExecution(id))
    }

    @GetMapping("/errors")
    fun getErrorLogs(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) jobName: String?,
    ): ResponseEntity<List<JobErrorLogModel>> {
        log.debug("Error logs requested - page={}, size={}, jobName={}", page, size, jobName)
        val logs = adminMappingService.getErrorLogs(page, size.coerceIn(1, 100), jobName)
        return ResponseEntity.ok(logs)
    }

    @GetMapping("/schedules")
    fun getScheduleConfigs(): ResponseEntity<List<JobScheduleConfigModel>> {
        log.debug("Schedule configs requested")
        val configs = adminMappingService.getScheduleConfigs()
        return ResponseEntity.ok(configs)
    }

    @GetMapping("/webhooks")
    fun getWebhookTriggerHistory(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) jobName: String?,
    ): ResponseEntity<List<WebhookTriggerHistoryModel>> {
        log.debug("Webhook history requested - page={}, size={}, jobName={}", page, size, jobName)
        val history = adminMappingService.getWebhookTriggerHistory(page, size.coerceIn(1, 100), jobName)
        return ResponseEntity.ok(history)
    }

    @GetMapping("/jobs")
    fun getAvailableJobs(): ResponseEntity<List<String>> {
        log.debug("Available jobs requested")
        val jobs = adminMappingService.getAvailableJobs()
        return ResponseEntity.ok(jobs)
    }

    @PostMapping("/jobs/{jobName}/trigger")
    fun triggerJob(
        @PathVariable jobName: String,
        @RequestBody(required = false) request: JobTriggerRequestModel?,
        httpRequest: HttpServletRequest,
    ): ResponseEntity<JobTriggerResponseModel> {
        // 인가가 가장 먼저다. 아래 로깅보다 앞선다 — 거부된 요청의 파라미터 값이 로그에 남으면 안 된다.
        val keyName = jobTriggerAuthorizer.authorize(httpRequest, jobName, request?.parameters)

        log.info(
            "Admin Job 트리거 요청 - keyName={}, jobName={}, parameterKeys={}",
            keyName,
            jobName,
            request?.parameters?.keys?.sorted(),
        )

        val response = adminMappingService.triggerJob(jobName, request?.parameters)

        log.info(
            "Admin Job triggered - jobName={}, jobExecutionId={}, traceId={}",
            jobName,
            response.jobExecutionId,
            response.traceId,
        )

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response)
    }
}