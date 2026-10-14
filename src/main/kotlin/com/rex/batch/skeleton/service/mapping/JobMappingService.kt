package com.rex.batch.skeleton.service.mapping

import com.rex.batch.skeleton.mapper.JobExecutionMapper
import com.rex.batch.skeleton.model.webhook.JobStatusResponseModel
import com.rex.batch.skeleton.model.webhook.JobTriggerResponseModel
import com.rex.batch.skeleton.service.biz.JobLauncherBizService
import org.springframework.stereotype.Service
import java.time.LocalDateTime

/**
 * Job 매핑 서비스
 *
 * Controller용 매핑 서비스로 DTO → Model 변환을 담당합니다.
 */
@Service
class JobMappingService(
    private val jobLauncherBizService: JobLauncherBizService,
    private val jobExecutionMapper: JobExecutionMapper,
) {
    /**
     * Job 비동기 트리거
     *
     * @param jobName Job 이름
     * @param parameters Job 파라미터
     * @return Job 트리거 응답 모델
     */
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

    /**
     * Job 실행 상태 조회
     *
     * @param jobExecutionId Job 실행 ID
     * @return Job 상태 응답 모델
     */
    fun getJobStatus(jobExecutionId: Long): JobStatusResponseModel {
        val dto = jobLauncherBizService.getJobExecution(jobExecutionId)
        return jobExecutionMapper.toModel(dto)
    }

    /**
     * 사용 가능한 Job 목록 조회
     *
     * @return Job 이름 목록
     */
    fun getAvailableJobs(): List<String> = jobLauncherBizService.getAvailableJobs()
}