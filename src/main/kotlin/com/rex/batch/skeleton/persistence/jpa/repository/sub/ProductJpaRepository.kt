package com.rex.batch.skeleton.persistence.jpa.repository.sub

import com.rex.batch.skeleton.persistence.jpa.entity.sub.ProductJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ProductJpaRepository : JpaRepository<ProductJpaEntity, Long> {
    fun getAllByCategoryId(categoryId: Long): List<ProductJpaEntity>
}