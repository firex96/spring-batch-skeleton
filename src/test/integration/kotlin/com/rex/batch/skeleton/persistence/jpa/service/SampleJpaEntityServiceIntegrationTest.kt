package com.rex.batch.skeleton.persistence.jpa.service

import com.rex.batch.skeleton.config.IntegrationTestConfiguration
import com.rex.batch.skeleton.config.TestcontainersConfiguration
import com.rex.batch.skeleton.dto.SampleDto
import com.rex.batch.skeleton.persistence.jpa.service.main.SampleJpaEntityService
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.comparables.shouldBeGreaterThanOrEqualTo
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(initializers = [TestcontainersConfiguration.Initializer::class])
@Import(IntegrationTestConfiguration::class)
@org.springframework.boot.autoconfigure.EnableAutoConfiguration(
    exclude = [org.springframework.boot.quartz.autoconfigure.QuartzAutoConfiguration::class],
)
@Transactional
class SampleJpaEntityServiceIntegrationTest(
    private val sampleJpaEntityService: SampleJpaEntityService,
) : BehaviorSpec({
    Given("SampleDto를 저장할 때") {
        val dto =
            SampleDto(
                id = 0,
                name = "테스트 엔티티",
                description = "테스트 설명",
                status = "ACTIVE",
            )

        When("create를 호출하면") {
            val saved = sampleJpaEntityService.create(dto)

            Then("ID가 생성된다") {
                saved.id shouldNotBe 0
            }

            Then("엔티티가 저장된다") {
                saved.name shouldBe "테스트 엔티티"
                saved.status shouldBe "ACTIVE"
            }

            Then("Auditing 필드가 설정된다") {
                saved.createdAt shouldNotBe null
            }
        }
    }

    Given("저장된 엔티티가 있을 때") {
        val dto =
            SampleDto(
                id = 0,
                name = "조회 테스트",
                description = "조회용 엔티티",
                status = "ACTIVE",
            )
        val saved = sampleJpaEntityService.create(dto)

        When("ID로 조회하면") {
            val found = sampleJpaEntityService.get(saved.id)

            Then("DTO가 반환된다") {
                found shouldNotBe null
                found!!.name shouldBe "조회 테스트"
            }
        }

        When("상태로 조회하면") {
            val list = sampleJpaEntityService.getAllByStatus("ACTIVE")

            Then("해당 상태의 DTO들이 반환된다") {
                list.shouldNotBeEmpty()
                list.all { it.status == "ACTIVE" } shouldBe true
            }
        }
    }

    Given("여러 엔티티가 저장되어 있을 때") {
        val dtos =
            listOf(
                SampleDto(id = 0, name = "엔티티1", status = "ACTIVE"),
                SampleDto(id = 0, name = "엔티티2", status = "ACTIVE"),
                SampleDto(id = 0, name = "엔티티3", status = "INACTIVE"),
            )
        sampleJpaEntityService.saveAll(dtos)

        When("ACTIVE 상태로 조회하면") {
            val active = sampleJpaEntityService.getAllByStatus("ACTIVE")

            Then("ACTIVE 엔티티만 반환된다") {
                // 상태별 조회 기능 검증 - 모든 결과가 ACTIVE 상태여야 함
                active.shouldNotBeEmpty()
                active.all { it.status == "ACTIVE" } shouldBe true
                // 이 블록에서 생성한 엔티티가 포함되어 있는지 확인
                val blockEntities = active.filter { it.name.startsWith("엔티티") }
                blockEntities.size shouldBeGreaterThanOrEqualTo 2
            }
        }

        When("INACTIVE 상태로 조회하면") {
            val inactive = sampleJpaEntityService.getAllByStatus("INACTIVE")

            Then("INACTIVE 엔티티만 반환된다") {
                // 상태별 조회 기능 검증 - 모든 결과가 INACTIVE 상태여야 함
                inactive.shouldNotBeEmpty()
                inactive.all { it.status == "INACTIVE" } shouldBe true
                // 이 블록에서 생성한 엔티티가 포함되어 있는지 확인
                val blockEntities = inactive.filter { it.name.startsWith("엔티티") }
                blockEntities.size shouldBeGreaterThanOrEqualTo 1
            }
        }
    }
})