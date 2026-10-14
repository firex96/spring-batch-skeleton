package com.rex.batch.skeleton.controller

import com.rex.batch.skeleton.config.webhook.JobTriggerAuthorizer
import com.rex.batch.skeleton.model.webhook.JobStatusResponseModel
import com.rex.batch.skeleton.model.webhook.JobTriggerRequestModel
import com.rex.batch.skeleton.model.webhook.JobTriggerResponseModel
import com.rex.batch.skeleton.service.mapping.JobMappingService
import com.rex.batch.skeleton.util.logger
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Job 트리거 및 상태 조회 Controller
 */
@Tag(name = "Job API", description = "배치 Job 트리거 및 상태 조회 API")
@RestController
@RequestMapping("/api/v1/jobs")
class JobController(
    private val jobTriggerAuthorizer: JobTriggerAuthorizer,
    private val jobMappingService: JobMappingService,
) {
    private val log = logger()

    @Operation(
        summary = "Job 비동기 트리거",
        description = "지정된 Job을 비동기로 실행합니다. 즉시 jobExecutionId를 반환하고 백그라운드에서 Job이 실행됩니다.",
        security = [SecurityRequirement(name = "apiKey")],
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "202", description = "Job 트리거 성공"),
            ApiResponse(responseCode = "401", description = "인증 실패"),
            ApiResponse(responseCode = "404", description = "Job을 찾을 수 없음"),
            ApiResponse(responseCode = "500", description = "서버 오류"),
        ],
    )
    @PostMapping("/{jobName}/trigger")
    fun triggerJob(
        @Parameter(description = "Job 이름", required = true)
        @PathVariable jobName: String,
        @RequestBody(required = false) request: JobTriggerRequestModel?,
        httpRequest: HttpServletRequest,
    ): ResponseEntity<JobTriggerResponseModel> {
        // 인가가 가장 먼저다. 아래 로깅보다 앞선다 — 거부된 요청의 파라미터 값이 로그에 남으면 안 된다.
        val keyName = jobTriggerAuthorizer.authorize(httpRequest, jobName, request?.parameters)

        log.info(
            "Job 트리거 요청 - keyName={}, jobName={}, parameterKeys={}",
            keyName,
            jobName,
            request?.parameters?.keys?.sorted(),
        )

        val response = jobMappingService.triggerJob(jobName, request?.parameters)

        log.info(
            "Job 트리거 완료 - jobName={}, jobExecutionId={}, traceId={}",
            jobName,
            response.jobExecutionId,
            response.traceId,
        )

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response)
    }

    @Operation(
        summary = "Job 실행 상태 조회",
        description = "Job 실행 ID로 상태를 조회합니다.",
        security = [SecurityRequirement(name = "apiKey")],
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "조회 성공"),
            ApiResponse(responseCode = "401", description = "인증 실패"),
            ApiResponse(responseCode = "404", description = "Job 실행 정보를 찾을 수 없음"),
            ApiResponse(responseCode = "500", description = "서버 오류"),
        ],
    )
    @GetMapping("/executions/{jobExecutionId}")
    fun getJobStatus(
        @Parameter(description = "Job 실행 ID", required = true)
        @PathVariable jobExecutionId: Long,
    ): ResponseEntity<JobStatusResponseModel> {
        log.debug("Job 상태 조회 요청 - jobExecutionId={}", jobExecutionId)

        val response = jobMappingService.getJobStatus(jobExecutionId)

        return ResponseEntity.ok(response)
    }

    @Operation(
        summary = "사용 가능한 Job 목록 조회",
        description = "실행 가능한 배치 Job 목록을 조회합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "조회 성공"),
            ApiResponse(responseCode = "500", description = "서버 오류"),
        ],
    )
    @GetMapping
    fun getAvailableJobs(): ResponseEntity<List<String>> {
        val jobs = jobMappingService.getAvailableJobs()
        return ResponseEntity.ok(jobs)
    }
}