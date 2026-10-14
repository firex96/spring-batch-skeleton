package com.rex.batch.skeleton.persistence.jpa.entity.backbone

import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass

/**
 * 기본 엔티티 클래스
 *
 * 모든 JPA 엔티티의 기본 클래스로, ID 필드를 제공합니다.
 */
@MappedSuperclass
abstract class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0
}