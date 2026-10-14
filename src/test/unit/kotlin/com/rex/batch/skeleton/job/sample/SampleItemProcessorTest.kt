package com.rex.batch.skeleton.job.sample

import com.rex.batch.skeleton.context.FixtureMonkeyFactory
import com.rex.batch.skeleton.dto.SampleDto
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class SampleItemProcessorTest :
    BehaviorSpec({
        val processor = SampleItemProcessor()

        Given("유효한 SampleDto가 주어졌을 때") {
            val dto =
                FixtureMonkeyFactory
                    .builder<SampleDto>()
                    .set("id", 1L)
                    .set("name", "테스트")
                    .set("status", "PENDING")
                    .sample()

            When("processor가 처리하면") {
                val result = processor.process(dto)

                Then("상태가 COMPLETED로 변경된 DTO가 반환된다") {
                    result shouldNotBe null
                    result!!.status shouldBe "COMPLETED"
                    result.processedAt shouldNotBe null
                }
            }
        }

        Given("이름이 빈 문자열인 DTO가 주어졌을 때") {
            val dto =
                FixtureMonkeyFactory
                    .builder<SampleDto>()
                    .set("id", 2L)
                    .set("name", "")
                    .set("status", "PENDING")
                    .sample()

            When("processor가 처리하면") {
                val result = processor.process(dto)

                Then("null이 반환되어 필터링된다") {
                    result shouldBe null
                }
            }
        }

        Given("이름이 공백만 있는 DTO가 주어졌을 때") {
            val dto =
                FixtureMonkeyFactory
                    .builder<SampleDto>()
                    .set("id", 3L)
                    .set("name", "   ")
                    .set("status", "PENDING")
                    .sample()

            When("processor가 처리하면") {
                val result = processor.process(dto)

                Then("null이 반환되어 필터링된다") {
                    result shouldBe null
                }
            }
        }
    })