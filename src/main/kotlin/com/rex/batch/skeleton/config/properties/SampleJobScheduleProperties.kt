package com.rex.batch.skeleton.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * 샘플 Job 의 Quartz 스케줄.
 *
 * 스케줄은 환경마다 다를 수 있어 코드가 아니라 설정(`app.batch.jobs.sample.*`)으로 둔다. `enabled = false` 면
 * Quartz 트리거만 등록하지 않는다 — Job 자체와 웹훅 수동 실행(`POST /api/v1/jobs/sampleJob/trigger`)은 그대로 동작한다.
 *
 * ★ `BatchSkeletonApplication` 의 `@EnableConfigurationProperties` 명시 목록에 등록해야 빈이 생성된다.
 */
@ConfigurationProperties(prefix = "app.batch.jobs.sample")
data class SampleJobScheduleProperties(
    /** Quartz cron 표현식 (초 분 시 일 월 요일). 기본은 매일 02:00. */
    val cron: String = "0 0 2 * * ?",
    /** false 면 트리거를 등록하지 않는다. */
    val enabled: Boolean = true,
)