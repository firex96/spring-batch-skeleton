package com.rex.batch.skeleton.config

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.core.task.SyncTaskExecutor
import org.springframework.core.task.TaskExecutor

/**
 * 통합 테스트용 설정
 *
 * 테스트 환경에 필요한 빈들을 정의.
 */
@TestConfiguration
class IntegrationTestConfiguration {
    /**
     * 테스트용 동기 TaskExecutor
     *
     * 비동기 작업을 동기로 처리하여 테스트 안정성 확보.
     */
    @Bean
    @Primary
    fun testTaskExecutor(): TaskExecutor = SyncTaskExecutor()
}