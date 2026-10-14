package com.rex.batch.skeleton.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.TaskExecutor
import org.springframework.core.task.support.CompositeTaskDecorator
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor

/**
 * 비동기 처리 설정
 *
 * Spring Batch의 병렬 처리 및 비동기 작업에 사용.
 * MDC(로깅)와 감사 주체를 각각의 데코레이터로 전파한다 — 채널이 다르므로 합성한다.
 */
@Configuration
@EnableAsync
class AsyncConfiguration {
    @Bean("batchTaskExecutor")
    fun batchTaskExecutor(): TaskExecutor =
        ThreadPoolTaskExecutor().apply {
            corePoolSize = 10
            maxPoolSize = 20
            queueCapacity = 100
            setThreadNamePrefix("batch-")
            setTaskDecorator(CompositeTaskDecorator(listOf(MdcTaskDecorator(), AuditorContextTaskDecorator())))
            initialize()
        }

    @Bean("taskExecutor")
    fun taskExecutor(): TaskExecutor =
        ThreadPoolTaskExecutor().apply {
            corePoolSize = 5
            maxPoolSize = 10
            queueCapacity = 50
            setThreadNamePrefix("async-")
            setTaskDecorator(CompositeTaskDecorator(listOf(MdcTaskDecorator(), AuditorContextTaskDecorator())))
            initialize()
        }
}