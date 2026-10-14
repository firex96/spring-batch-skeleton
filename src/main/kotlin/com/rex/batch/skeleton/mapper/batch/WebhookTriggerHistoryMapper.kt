package com.rex.batch.skeleton.mapper.batch

import com.rex.batch.skeleton.config.MapstructConfiguration
import com.rex.batch.skeleton.dto.batch.WebhookTriggerHistoryDto
import com.rex.batch.skeleton.persistence.jpa.entity.batch.WebhookTriggerHistoryJpaEntity
import org.mapstruct.Mapper

/**
 * Webhook 트리거 이력 매퍼
 *
 * Entity ↔ DTO 변환을 담당합니다.
 */
@Mapper(config = MapstructConfiguration::class)
interface WebhookTriggerHistoryMapper {
    fun toDto(entity: WebhookTriggerHistoryJpaEntity): WebhookTriggerHistoryDto

    fun toEntity(dto: WebhookTriggerHistoryDto): WebhookTriggerHistoryJpaEntity

    fun toDtoList(entities: List<WebhookTriggerHistoryJpaEntity>): List<WebhookTriggerHistoryDto>
}