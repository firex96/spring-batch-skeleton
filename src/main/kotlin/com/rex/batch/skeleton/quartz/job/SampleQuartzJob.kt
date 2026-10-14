package com.rex.batch.skeleton.quartz.job

import com.rex.batch.skeleton.quartz.QuartzJobLauncher
import com.rex.batch.skeleton.util.logger
import org.quartz.DisallowConcurrentExecution
import org.quartz.JobExecutionContext
import org.quartz.PersistJobDataAfterExecution
import org.springframework.batch.core.job.Job
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.scheduling.quartz.QuartzJobBean
import org.springframework.stereotype.Component

/**
 * 샘플 Quartz Job
 *
 * @DisallowConcurrentExecution: 동시 실행 방지
 * @PersistJobDataAfterExecution: JobDataMap 변경사항 저장
 */
@Component
@DisallowConcurrentExecution
@PersistJobDataAfterExecution
class SampleQuartzJob(
    private val quartzJobLauncher: QuartzJobLauncher,
    @param:Qualifier("sampleJob") private val sampleJob: Job,
) : QuartzJobBean() {
    private val log = logger()

    override fun executeInternal(context: JobExecutionContext) {
        log.info("SampleQuartzJob 실행 - fireTime={}", context.fireTime)

        try {
            quartzJobLauncher.run(sampleJob, context)
        } catch (e: Exception) {
            log.error("SampleQuartzJob 실행 중 오류 발생", e)
            // Quartz에 실패 전파 (재시도 정책에 따라 처리됨)
            throw e
        }
    }
}