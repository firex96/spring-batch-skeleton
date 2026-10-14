package com.rex.batch.skeleton.mapper

import com.rex.batch.skeleton.mapper.batch.JobScheduleConfigMapper
import com.rex.batch.skeleton.persistence.jpa.entity.batch.JobScheduleConfigJpaEntity
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import org.mapstruct.factory.Mappers

/**
 * `is`-prefix Boolean 매핑 회귀 — 스케줄 목록·`/admin/api/schedules` 의 `isEnabled` 가 항상 false 로 내려가던 결함을 고정한다.
 * `@Mapping(source = "enabled", target = "isEnabled")` 을 지우면 실패한다.
 */
class JobScheduleConfigMapperTest : BehaviorSpec({
    val mapper = Mappers.getMapper(JobScheduleConfigMapper::class.java)

    Given("isEnabled = true 인 JobScheduleConfigJpaEntity") {
        val entity =
            JobScheduleConfigJpaEntity().apply {
                id = 3L
                jobName = "sampleJob"
                cronExpression = "0 0 * * * ?"
                isEnabled = true
            }

        Then("toDto 의 isEnabled 가 true 다 — 이전엔 항상 false") {
            val dto = mapper.toDto(entity)
            dto.isEnabled shouldBe true
            dto.jobName shouldBe "sampleJob"
        }
    }
})