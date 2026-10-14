package com.rex.batch.skeleton.quartz

import com.rex.batch.skeleton.constant.Constant
import com.rex.batch.skeleton.util.logger
import org.quartz.JobExecutionContext
import org.springframework.batch.core.job.Job
import org.springframework.batch.core.job.parameters.JobParametersBuilder
import org.springframework.batch.core.launch.JobOperator
import org.springframework.stereotype.Component
import java.util.UUID

/**
 * Quartz에서 Spring Batch Job을 실행하기 위한 런처
 *
 * Quartz Job에서 Spring Batch Job을 실행할 때 사용합니다.
 */
@Component
class QuartzJobLauncher(
    private val jobLauncher: JobOperator,
) {
    private val log = logger()

    /**
     * Spring Batch Job 실행
     *
     * @param job 실행할 배치 Job
     * @param context Quartz 실행 컨텍스트
     */
    fun run(
        job: Job,
        context: JobExecutionContext,
    ) {
        val traceId = UUID.randomUUID().toString()

        val jobParameters =
            JobParametersBuilder()
                .addString(Constant.KEY_TRACE_ID, traceId)
                .addLong("timestamp", System.currentTimeMillis())
                .addString("triggeredBy", "quartz")
                .addString("fireInstanceId", context.fireInstanceId)
                .toJobParameters()

        log.info(
            "Quartz에서 배치 Job 실행 시작 - jobName={}, traceId={}, fireTime={}",
            job.name,
            traceId,
            context.fireTime,
        )

        try {
            val execution = jobLauncher.run(job, jobParameters)
            log.info(
                "배치 Job 실행 완료 - jobExecutionId={}, status={}",
                execution.id,
                execution.status,
            )
        } catch (e: Exception) {
            log.error("배치 Job 실행 실패 - jobName={}, error={}", job.name, e.message, e)
            throw e
        }
    }
}