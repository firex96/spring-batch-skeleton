package com.rex.batch.skeleton.persistence.jpa.entity.backbone

import jakarta.persistence.Column
import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

/**
 * 생성 감사 필드만 포함하는 기본 엔티티.
 *
 * 생성 시점의 시간과 사용자 정보만 기록합니다.
 * 수정 정보가 필요 없는 불변 엔티티에 사용합니다.
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class BaseCreateAuditedEntity : BaseEntity() {
    @CreatedBy
    @Column(name = "created_by", updatable = false, nullable = false)
    var createdBy: Long = 0L

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now() // NOT NULL — 리스너가 persist 시 덮는다
}