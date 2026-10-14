package com.rex.batch.skeleton.persistence.jpa.repository.sub

import com.rex.batch.skeleton.persistence.jpa.entity.sub.TestJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TestJpaRepository : JpaRepository<TestJpaEntity, Long>