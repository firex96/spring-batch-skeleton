package com.rex.batch.skeleton.persistence.jpa.entity.main

import com.rex.batch.skeleton.persistence.jpa.entity.backbone.BaseAuditedEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.time.LocalDateTime

/**
 * 샘플 JPA 엔티티
 *
 * 배치 처리 예제용 엔티티입니다.
 */
@Entity
@Table(name = "sample")
class SampleJpaEntity : BaseAuditedEntity() {
    @Column(name = "name", nullable = false, length = 100)
    var name: String = ""

    @Column(name = "status", nullable = false, length = 20)
    var status: String = "PENDING"

    @Column(name = "processed_at")
    var processedAt: LocalDateTime? = null

    @Column(name = "description", length = 500)
    var description: String? = null
}