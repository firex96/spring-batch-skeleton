package com.rex.batch.skeleton.mapper.batch

import com.rex.batch.skeleton.config.MapstructConfiguration
import com.rex.batch.skeleton.dto.batch.JobExecutionSummaryDto
import com.rex.batch.skeleton.persistence.jpa.entity.batch.JobExecutionSummaryJpaEntity
import org.mapstruct.Mapper

/**
 * Job 실행 통계 요약 매퍼
 *
 * Entity ↔ DTO 변환을 담당합니다.
 */
@Mapper(config = MapstructConfiguration::class)
interface JobExecutionSummaryMapper {
    fun toDto(entity: JobExecutionSummaryJpaEntity): JobExecutionSummaryDto

    fun toEntity(dto: JobExecutionSummaryDto): JobExecutionSummaryJpaEntity

    fun toDtoList(entities: List<JobExecutionSummaryJpaEntity>): List<JobExecutionSummaryDto>
}