package com.rex.batch.skeleton.config

import com.rex.batch.skeleton.config.properties.SampleJobScheduleProperties
import com.rex.batch.skeleton.quartz.job.SampleQuartzJob
import org.quartz.CronScheduleBuilder
import org.quartz.JobBuilder
import org.quartz.JobDetail
import org.quartz.Trigger
import org.quartz.TriggerBuilder
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Quartz 스케줄러 설정
 *
 * Spring Boot의 auto-configuration을 활용하되,
 * 배치 작업별 JobDetail과 Trigger를 명시적으로 정의.
 * 스케줄(cron·사용 여부)은 Job 별 Properties 로 받는다 — 코드에 cron 을 박지 않는다.
 */
@Configuration
@ConditionalOnProperty(prefix = "spring.quartz", name = ["enabled"], havingValue = "true", matchIfMissing = true)
class QuartzConfiguration {
    // ========== Sample Job ==========

    @Bean
    fun sampleJobDetail(): JobDetail =
        JobBuilder
            .newJob(SampleQuartzJob::class.java)
            .withIdentity("sampleJob", "batch-jobs")
            .withDescription("샘플 배치 Job")
            .storeDurably()
            .requestRecovery(true)
            .build()

    @Bean
    @ConditionalOnProperty(prefix = "app.batch.jobs.sample", name = ["enabled"], havingValue = "true", matchIfMissing = true)
    fun sampleJobTrigger(
        sampleJobDetail: JobDetail,
        schedule: SampleJobScheduleProperties,
    ): Trigger =
        TriggerBuilder
            .newTrigger()
            .forJob(sampleJobDetail)
            .withIdentity("sampleJobTrigger", "batch-triggers")
            .withDescription("샘플 Job 스케줄 (${schedule.cron})")
            .withSchedule(
                CronScheduleBuilder
                    .cronSchedule(schedule.cron)
                    .withMisfireHandlingInstructionFireAndProceed(),
            ).build()
}