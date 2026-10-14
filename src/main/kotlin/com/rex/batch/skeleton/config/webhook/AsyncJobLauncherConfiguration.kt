package com.rex.batch.skeleton.config.webhook

import org.springframework.batch.core.launch.JobLauncher
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher
import org.springframework.batch.core.repository.JobRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.SimpleAsyncTaskExecutor

/**
 * 비동기 Job Launcher 설정
 *
 * Webhook 호출 시 즉시 응답하고 백그라운드에서 Job을 실행합니다.
 *
 * **`webhook.enabled` 에 걸지 않는다.** 그 플래그는 **웹 표면**(인터셉터·트리거 노출)을
 * 켜고 끄는 것이고, 비동기 런처는 웹 표면이 아니다 — Quartz 스케줄 경로도 Job 을 실행하며
 * 그때도 런처가 필요하다. 조건부로 두면 `JobLauncherBizService` 의 필수 주입이 실패해
 * **플래그를 끄는 순간 애플리케이션이 기동하지 못한다.**
 */
@Configuration
class AsyncJobLauncherConfiguration {
    @Bean("asyncJobLauncher")
    fun asyncJobLauncher(jobRepository: JobRepository): JobLauncher {
        val jobLauncher = TaskExecutorJobLauncher()
        jobLauncher.setJobRepository(jobRepository)
        jobLauncher.setTaskExecutor(SimpleAsyncTaskExecutor("async-job-"))
        jobLauncher.afterPropertiesSet()
        return jobLauncher
    }
}