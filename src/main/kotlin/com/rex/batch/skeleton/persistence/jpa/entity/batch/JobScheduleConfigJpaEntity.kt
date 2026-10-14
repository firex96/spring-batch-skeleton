package com.rex.batch.skeleton.persistence.jpa.entity.batch

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

/**
 * Job 스케줄 설정 JPA 엔티티
 *
 * Job 스케줄 설정 (동적 스케줄 관리용)을 저장합니다.
 */
@Entity
@Table(name = "JOB_SCHEDULE_CONFIG")
class JobScheduleConfigJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SCHEDULE_CONFIG_ID")
    var id: Long = 0L

    @Column(name = "JOB_NAME", nullable = false, unique = true, length = 100)
    var jobName: String = ""

    @Column(name = "CRON_EXPRESSION", nullable = false, length = 100)
    var cronExpression: String = ""

    @Column(name = "DESCRIPTION", length = 500)
    var description: String? = null

    @Column(name = "IS_ENABLED", nullable = false)
    var isEnabled: Boolean = true

    @Column(name = "TIMEZONE", nullable = false, length = 50)
    var timezone: String = "Asia/Seoul"

    @Column(name = "LAST_TRIGGERED_AT")
    var lastTriggeredAt: LocalDateTime? = null

    @Column(name = "NEXT_FIRE_TIME")
    var nextFireTime: LocalDateTime? = null

    @Column(name = "CREATED_BY", nullable = false)
    var createdBy: Long = 0L

    @Column(name = "UPDATED_BY", nullable = false)
    var updatedBy: Long = 0L

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()

    @Column(name = "UPDATED_AT", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
}