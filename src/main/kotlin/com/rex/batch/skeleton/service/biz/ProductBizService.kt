package com.rex.batch.skeleton.service.biz

import com.rex.batch.skeleton.dto.ProductDto
import com.rex.batch.skeleton.persistence.jdbc.service.sub.ProductJdbcEntityService
import com.rex.batch.skeleton.persistence.jpa.service.sub.ProductJpaEntityService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true, transactionManager = "subTransactionManager")
@Service
class ProductBizService(
    private val productJpaEntityService: ProductJpaEntityService,
    private val productJdbcEntityService: ProductJdbcEntityService,
) {
    fun find(id: Long): ProductDto? = productJpaEntityService.get(id)

    @Transactional(transactionManager = "subTransactionManager")
    fun add(productDto: ProductDto): ProductDto = productJpaEntityService.create(productDto)

    @Transactional(transactionManager = "subTransactionManager")
    fun modify(productDto: ProductDto): ProductDto = productJpaEntityService.update(productDto)

    @Transactional(transactionManager = "subTransactionManager")
    fun remove(productId: Long) = productJpaEntityService.delete(productId)

    fun findAllProductIdByCategoryId(categoryId: Long): List<Long> = productJdbcEntityService.getAllProductIdByCategoryId(categoryId)
}