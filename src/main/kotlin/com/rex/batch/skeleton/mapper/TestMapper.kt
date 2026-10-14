package com.rex.batch.skeleton.mapper

import com.rex.batch.skeleton.config.MapstructConfiguration
import com.rex.batch.skeleton.dto.TestDto
import com.rex.batch.skeleton.persistence.jpa.entity.sub.TestJpaEntity
import org.mapstruct.Mapper

@Mapper(config = MapstructConfiguration::class)
interface TestMapper {
    fun toDto(entity: TestJpaEntity): TestDto

    fun toDtoList(entityList: List<TestJpaEntity>): List<TestDto>

    fun toEntity(dto: TestDto): TestJpaEntity
}