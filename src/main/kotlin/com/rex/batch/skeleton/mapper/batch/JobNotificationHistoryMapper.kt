package com.rex.batch.skeleton.mapper.batch

import com.rex.batch.skeleton.config.MapstructConfiguration
import com.rex.batch.skeleton.dto.batch.JobNotificationHistoryDto
import com.rex.batch.skeleton.persistence.jpa.entity.batch.JobNotificationHistoryJpaEntity
import org.mapstruct.Mapper

/**
 * Job 알림 발송 이력 매퍼
 *
 * Entity ↔ DTO 변환을 담당합니다.
 */
@Mapper(config = MapstructConfiguration::class)
interface JobNotificationHistoryMapper {
    fun toDto(entity: JobNotificationHistoryJpaEntity): JobNotificationHistoryDto

    fun toEntity(dto: JobNotificationHistoryDto): JobNotificationHistoryJpaEntity

    fun toDtoList(entities: List<JobNotificationHistoryJpaEntity>): List<JobNotificationHistoryDto>
}