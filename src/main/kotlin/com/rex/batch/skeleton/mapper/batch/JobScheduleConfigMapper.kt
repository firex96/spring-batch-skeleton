package com.rex.batch.skeleton.mapper.batch

import com.rex.batch.skeleton.config.MapstructConfiguration
import com.rex.batch.skeleton.dto.batch.JobScheduleConfigDto
import com.rex.batch.skeleton.persistence.jpa.entity.batch.JobScheduleConfigJpaEntity
import org.mapstruct.Mapper
import org.mapstruct.Mapping

/**
 * Job 스케줄 설정 매퍼
 *
 * Entity ↔ DTO 변환을 담당합니다.
 */
@Mapper(config = MapstructConfiguration::class)
interface JobScheduleConfigMapper {
    // is-prefix Boolean: getter isEnabled() → 소스 프로퍼티명 enabled, 생성자 파라미터 isEnabled — 명시하지 않으면 미매핑으로 항상 false
    @Mapping(source = "enabled", target = "isEnabled")
    fun toDto(entity: JobScheduleConfigJpaEntity): JobScheduleConfigDto

    fun toEntity(dto: JobScheduleConfigDto): JobScheduleConfigJpaEntity

    fun toDtoList(entities: List<JobScheduleConfigJpaEntity>): List<JobScheduleConfigDto>
}