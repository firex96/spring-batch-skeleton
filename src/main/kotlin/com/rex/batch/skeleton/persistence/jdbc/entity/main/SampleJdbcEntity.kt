package com.rex.batch.skeleton.persistence.jdbc.entity.main

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

/**
 * 샘플 JDBC 엔티티
 *
 * Spring Data JDBC를 통한 데이터 접근용 엔티티입니다.
 */
@Table("sample")
data class SampleJdbcEntity(
    @Id
    val id: Long = 0L,
    @Column("name")
    val name: String,
    @Column("status")
    val status: String,
    @Column("processed_at")
    val processedAt: LocalDateTime? = null,
    @Column("description")
    val description: String? = null,
    @Column("created_by")
    val createdBy: Long = 0L,
    @Column("created_at")
    val createdAt: LocalDateTime,
    @Column("updated_by")
    val updatedBy: Long = 0L,
    @Column("updated_at")
    val updatedAt: LocalDateTime,
)