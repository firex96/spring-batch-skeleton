package com.rex.batch.skeleton.service.biz

import com.rex.batch.skeleton.context.FixtureMonkeyFactory
import com.rex.batch.skeleton.dto.SampleDto
import com.rex.batch.skeleton.persistence.jdbc.service.main.SampleJdbcEntityService
import com.rex.batch.skeleton.persistence.jpa.service.main.SampleJpaEntityService
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class SampleBizServiceUnitTest :
    BehaviorSpec({
        val sampleJpaEntityService = mockk<SampleJpaEntityService>()
        val sampleJdbcEntityService = mockk<SampleJdbcEntityService>()
        val bizService = SampleBizService(sampleJpaEntityService, sampleJdbcEntityService)

        Given("ID로 샘플을 조회할 때") {
            val dto =
                FixtureMonkeyFactory
                    .builder<SampleDto>()
                    .set("id", 1L)
                    .set("name", "테스트")
                    .set("status", "ACTIVE")
                    .sample()

            every { sampleJpaEntityService.get(1L) } returns dto

            When("find를 호출하면") {
                val result = bizService.find(1L)

                Then("DTO가 반환된다") {
                    result shouldBe dto
                }

                Then("JpaEntityService.get이 호출된다") {
                    verify(exactly = 1) { sampleJpaEntityService.get(1L) }
                }
            }
        }

        Given("상태별로 샘플 목록을 조회할 때") {
            val dtoList =
                listOf(
                    FixtureMonkeyFactory
                        .builder<SampleDto>()
                        .set("id", 1L)
                        .set("name", "테스트1")
                        .set("status", "ACTIVE")
                        .sample(),
                    FixtureMonkeyFactory
                        .builder<SampleDto>()
                        .set("id", 2L)
                        .set("name", "테스트2")
                        .set("status", "ACTIVE")
                        .sample(),
                )

            every { sampleJpaEntityService.getAllByStatus("ACTIVE") } returns dtoList

            When("findAllByStatus를 호출하면") {
                val result = bizService.findAllByStatus("ACTIVE")

                Then("DTO 목록이 반환된다") {
                    result.size shouldBe 2
                }
            }
        }

        Given("처리 대상 ID 목록을 조회할 때") {
            val ids = listOf(1L, 2L, 3L)

            every { sampleJdbcEntityService.getPendingSampleIds() } returns ids

            When("findPendingSampleIds를 호출하면") {
                val result = bizService.findPendingSampleIds()

                Then("ID 목록이 반환된다") {
                    result shouldBe ids
                }

                Then("JdbcEntityService.getPendingSampleIds가 호출된다") {
                    verify(exactly = 1) { sampleJdbcEntityService.getPendingSampleIds() }
                }
            }
        }

        Given("샘플을 생성할 때") {
            val inputDto =
                FixtureMonkeyFactory
                    .builder<SampleDto>()
                    .set("id", 0L)
                    .set("name", "새 항목")
                    .set("status", "ACTIVE")
                    .sample()

            val savedDto = inputDto.copy(id = 1L)

            every { sampleJpaEntityService.create(inputDto) } returns savedDto

            When("add를 호출하면") {
                val result = bizService.add(inputDto)

                Then("저장된 DTO가 반환된다") {
                    result.id shouldBe 1L
                    result.name shouldBe inputDto.name
                }

                Then("JpaEntityService.create가 호출된다") {
                    verify(exactly = 1) { sampleJpaEntityService.create(inputDto) }
                }
            }
        }

        Given("처리 완료 상태를 업데이트할 때") {
            val ids = listOf(1L, 2L, 3L)

            every { sampleJdbcEntityService.updateStatusToCompleted(ids) } returns 3

            When("markAsCompleted를 호출하면") {
                val result = bizService.markAsCompleted(ids)

                Then("업데이트된 건수가 반환된다") {
                    result shouldBe 3
                }

                Then("JdbcEntityService.updateStatusToCompleted가 호출된다") {
                    verify(exactly = 1) { sampleJdbcEntityService.updateStatusToCompleted(ids) }
                }
            }
        }
    })