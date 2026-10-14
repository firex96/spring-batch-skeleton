package com.rex.batch.skeleton.mapper.batch

import com.rex.batch.skeleton.config.MapstructConfiguration
import com.rex.batch.skeleton.dto.batch.JobErrorLogDto
import com.rex.batch.skeleton.persistence.jpa.entity.batch.JobErrorLogJpaEntity
import org.mapstruct.Mapper

/**
 * Job 에러 로그 매퍼
 *
 * Entity ↔ DTO 변환을 담당합니다.
 */
@Mapper(config = MapstructConfiguration::class)
interface JobErrorLogMapper {
    fun toDto(entity: JobErrorLogJpaEntity): JobErrorLogDto

    fun toEntity(dto: JobErrorLogDto): JobErrorLogJpaEntity

    fun toDtoList(entities: List<JobErrorLogJpaEntity>): List<JobErrorLogDto>
}