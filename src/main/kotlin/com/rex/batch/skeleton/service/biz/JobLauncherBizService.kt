package com.rex.batch.skeleton.service.biz

import com.rex.batch.skeleton.constant.Constant
import com.rex.batch.skeleton.dto.webhook.JobExecutionDto
import com.rex.batch.skeleton.dto.webhook.StepExecutionDto
import com.rex.batch.skeleton.exception.JobExecutionNotFoundException
import com.rex.batch.skeleton.exception.JobNotFoundException
import com.rex.batch.skeleton.filter.TraceIdSanitizer
import com.rex.batch.skeleton.util.logger
import com.rex.batch.skeleton.util.requireJobExecution
import org.slf4j.MDC
import org.springframework.batch.core.job.Job
import org.springframework.batch.core.job.JobExecution
import org.springframework.batch.core.job.parameters.JobParametersBuilder
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.batch.core.repository.explore.JobExplorer
import org.springframework.batch.core.step.StepExecution
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import java.util.UUID

/**
 * Job 실행 비즈니스 서비스
 *
 * Job 실행 및 상태 조회 로직을 처리합니다.
 */
@Service
class JobLauncherBizService(
    @Qualifier("asyncJobLauncher")
    private val asyncJobLauncher: JobLauncher,
    private val jobExplorer: JobExplorer,
    /**
     * 등록된 Job 빈 목록.
     *
     * 과거에는 `applicationContext.getBean(jobName, Job::class.java)` 로 **이름만 알면
     * 임의의 빈을 조회**했다. 인가(`JobTriggerAuthorizer`)가 앞에 서므로 실질 방어는
     * 화이트리스트지만, `getBean` 을 남겨 두면 다음 사람이 인가를 우회하는 경로를 만든다.
     */
    private val jobs: Map<String, Job>,
) {
    private val log = logger()

    /**
     * Job 비동기 트리거
     *
     * @param jobName Job 이름
     * @param parameters Job 파라미터
     * @return Job 실행 정보 DTO
     */
    fun triggerJob(
        jobName: String,
        parameters: Map<String, String>?,
    ): JobExecutionDto {
        val job = findJob(jobName)

        // 웹훅으로 들어온 요청이면 TraceIdFilter 가 MDC 에 정규화된 traceId 를 넣어 뒀다.
        // 그 값을 JobParameters 로 넘겨야 호출자의 traceId 가 Job 실행 로그까지 이어진다
        // (JobTraceListener 가 JobParameters 에서 MDC 로 복원한다).
        // Quartz 스케줄 경로처럼 인바운드가 없으면 새로 만든다.
        val mdcTraceId = MDC.get(Constant.KEY_TRACE_ID)
        val traceId =
            if (TraceIdSanitizer.isValid(mdcTraceId)) mdcTraceId else UUID.randomUUID().toString()

        val jobParametersBuilder =
            JobParametersBuilder()
                .addString(Constant.KEY_TRACE_ID, traceId)
                .addLong("timestamp", System.currentTimeMillis())

        parameters?.forEach { (key, value) ->
            jobParametersBuilder.addString(key, value)
        }

        val jobParameters = jobParametersBuilder.toJobParameters()

        log.info("Job 트리거 - jobName={}, traceId={}, parameters={}", jobName, traceId, parameters)

        val jobExecution = asyncJobLauncher.run(job, jobParameters)

        return toDto(jobExecution, traceId)
    }

    /**
     * Job 실행 상태 조회
     *
     * @param jobExecutionId Job 실행 ID
     * @return Job 실행 정보 DTO
     */
    fun getJobExecution(jobExecutionId: Long): JobExecutionDto {
        val jobExecution = jobExplorer.requireJobExecution(jobExecutionId)

        return toDto(jobExecution)
    }

    /**
     * 사용 가능한 Job 목록 조회
     *
     * @return Job 이름 목록
     */
    fun getAvailableJobs(): List<String> = jobExplorer.jobNames.toList()

    private fun findJob(jobName: String): Job = jobs[jobName] ?: throw JobNotFoundException(jobName)

    private fun toDto(
        jobExecution: JobExecution,
        traceId: String? = null,
    ): JobExecutionDto {
        val resolvedTraceId =
            traceId
                ?: jobExecution.jobParameters.getString(Constant.KEY_TRACE_ID)

        // Spring Batch 6.0: parameters() returns Set<JobParameter<?>>
        val parameters =
            jobExecution.jobParameters.parameters()
                .filter { it.name() != Constant.KEY_TRACE_ID && it.name() != "timestamp" }
                .associate { it.name() to (it.value()?.toString() ?: "") }

        return JobExecutionDto(
            jobExecutionId = jobExecution.id,
            jobName = jobExecution.jobInstance.jobName,
            status = jobExecution.status.name,
            traceId = resolvedTraceId,
            startTime = jobExecution.startTime,
            endTime = jobExecution.endTime,
            exitCode = jobExecution.exitStatus.exitCode,
            exitDescription = jobExecution.exitStatus.exitDescription.takeIf { it.isNotBlank() },
            parameters = parameters,
            stepExecutions = jobExecution.stepExecutions.map { toStepDto(it) },
        )
    }

    private fun toStepDto(stepExecution: StepExecution): StepExecutionDto =
        StepExecutionDto(
            stepExecutionId = stepExecution.id,
            stepName = stepExecution.stepName,
            status = stepExecution.status.name,
            readCount = stepExecution.readCount,
            writeCount = stepExecution.writeCount,
            commitCount = stepExecution.commitCount,
            rollbackCount = stepExecution.rollbackCount,
            filterCount = stepExecution.filterCount,
            skipCount = stepExecution.readSkipCount + stepExecution.processSkipCount + stepExecution.writeSkipCount,
            startTime = stepExecution.startTime,
            endTime = stepExecution.endTime,
            exitCode = stepExecution.exitStatus.exitCode,
            exitDescription = stepExecution.exitStatus.exitDescription.takeIf { it.isNotBlank() },
        )
}