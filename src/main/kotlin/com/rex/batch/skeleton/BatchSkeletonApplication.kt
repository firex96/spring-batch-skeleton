package com.rex.batch.skeleton

import com.rex.batch.skeleton.config.audit.AuditProperties
import com.rex.batch.skeleton.config.client.ClientDestinations
import com.rex.batch.skeleton.config.client.ClientProperties
import com.rex.batch.skeleton.config.properties.JobTriggerProperties
import com.rex.batch.skeleton.config.properties.SampleJobScheduleProperties
import com.rex.batch.skeleton.config.webhook.WebhookProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(
    ClientDestinations::class,
    ClientProperties::class,
    AuditProperties::class,
    WebhookProperties::class,
    // batch 는 자동 스캔이 아니라 명시 목록을 쓴다 — 빠뜨리면 빈이 생성되지 않아
    // 인가자가 파라미터 화이트리스트를 얻지 못한다.
    JobTriggerProperties::class,
    SampleJobScheduleProperties::class,
)
class BatchSkeletonApplication

fun main(args: Array<String>) {
    runApplication<BatchSkeletonApplication>(*args)
}