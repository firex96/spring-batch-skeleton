package com.rex.batch.skeleton.listener

import com.rex.batch.skeleton.constant.Constant
import com.rex.batch.skeleton.util.logger
import org.slf4j.MDC
import org.springframework.batch.core.job.JobExecution
import org.springframework.batch.core.listener.JobExecutionListener
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.LocalDateTime
import java.util.UUID

/**
 * Job 실행 리스너
 *
 * Job 시작/종료 시 traceId 설정 및 실행 정보 로깅.
 * MDC를 활용하여 전체 배치 처리 과정에서 추적 가능.
 */
@Component
class JobTraceListener : JobExecutionListener {
    private val log = logger()

    override fun beforeJob(jobExecution: JobExecution) {
        val traceId =
            jobExecution.jobParameters.getString(Constant.KEY_TRACE_ID)
                ?: UUID.randomUUID().toString()

        MDC.put(Constant.KEY_TRACE_ID, traceId)
        MDC.put(Constant.KEY_JOB_NAME, jobExecution.jobInstance.jobName)
        MDC.put(Constant.KEY_JOB_EXECUTION_ID, jobExecution.id.toString())

        log.info(
            "Job 시작 - jobName={}, jobExecutionId={}, parameters={}",
            jobExecution.jobInstance.jobName,
            jobExecution.id,
            jobExecution.jobParameters,
        )
    }

    override fun afterJob(jobExecution: JobExecution) {
        val startTime = jobExecution.startTime ?: LocalDateTime.now()
        val endTime = jobExecution.endTime ?: LocalDateTime.now()
        val duration = Duration.between(startTime, endTime)

        log.info(
            "Job 종료 - jobName={}, status={}, duration={}ms, exitStatus={}",
            jobExecution.jobInstance.jobName,
            jobExecution.status,
            duration.toMillis(),
            jobExecution.exitStatus.exitCode,
        )

        if (jobExecution.status.isUnsuccessful) {
            jobExecution.allFailureExceptions.forEach { ex: Throwable ->
                log.error("Job 실패 원인: {}", ex.message, ex)
            }
        }

        MDC.remove(Constant.KEY_TRACE_ID)
        MDC.remove(Constant.KEY_JOB_NAME)
        MDC.remove(Constant.KEY_JOB_EXECUTION_ID)
    }
}