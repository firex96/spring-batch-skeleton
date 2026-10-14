package com.rex.batch.skeleton.mapper

import com.rex.batch.skeleton.config.MapstructConfiguration
import com.rex.batch.skeleton.dto.ProductDto
import com.rex.batch.skeleton.persistence.jdbc.entity.sub.ProductJdbcEntity
import com.rex.batch.skeleton.persistence.jpa.entity.sub.ProductJpaEntity
import org.mapstruct.Mapper

@Mapper(config = MapstructConfiguration::class)
interface ProductMapper {
    fun toDto(entity: ProductJpaEntity): ProductDto

    fun toDto(entity: ProductJdbcEntity): ProductDto

    fun toEntity(dto: ProductDto): ProductJpaEntity

    fun toDtoList(entityList: List<ProductJpaEntity>): List<ProductDto>
}