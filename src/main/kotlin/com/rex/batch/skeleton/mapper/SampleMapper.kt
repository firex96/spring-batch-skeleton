package com.rex.batch.skeleton.mapper

import com.rex.batch.skeleton.config.MapstructConfiguration
import com.rex.batch.skeleton.dto.SampleDto
import com.rex.batch.skeleton.model.SampleModel
import com.rex.batch.skeleton.persistence.jdbc.entity.main.SampleJdbcEntity
import com.rex.batch.skeleton.persistence.jpa.entity.main.SampleJpaEntity
import org.mapstruct.Mapper
import org.mapstruct.Mapping

/**
 * 샘플 데이터 매퍼
 *
 * Entity ↔ DTO ↔ Model 변환을 담당합니다.
 */
@Mapper(config = MapstructConfiguration::class)
interface SampleMapper {
    // JPA Entity <-> DTO
    fun toDto(entity: SampleJpaEntity): SampleDto

    fun toEntity(dto: SampleDto): SampleJpaEntity

    fun jpaEntitiesToDtoList(entities: List<SampleJpaEntity>): List<SampleDto>

    // JDBC Entity <-> DTO
    fun toDto(entity: SampleJdbcEntity): SampleDto

    fun jdbcEntitiesToDtoList(entities: List<SampleJdbcEntity>): List<SampleDto>

    // DTO <-> Model
    fun toModel(dto: SampleDto): SampleModel

    // SampleModel 에는 감사 타임스탬프가 없다 — DTO 는 non-null(sample NOT NULL)이라 자리표시자, persist 시 감사 리스너가 덮는다
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "updatedAt", expression = "java(java.time.LocalDateTime.now())")
    // 감사 주체도 Model 에 없다 — non-null Long 이라 ignore 대신 시스템 사용자 자리표시자(persist 시 감사 리스너가 덮는다)
    @Mapping(target = "createdBy", expression = "java(com.rex.batch.skeleton.constant.Constant.SYSTEM_USER_ID)")
    @Mapping(target = "updatedBy", expression = "java(com.rex.batch.skeleton.constant.Constant.SYSTEM_USER_ID)")
    fun toDto(model: SampleModel): SampleDto

    fun toModelList(dtoList: List<SampleDto>): List<SampleModel>
}