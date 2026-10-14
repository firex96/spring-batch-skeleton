package com.rex.batch.skeleton.job.sample

import com.rex.batch.skeleton.config.IntegrationTestConfiguration
import com.rex.batch.skeleton.config.TestcontainersConfiguration
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.job.Job
import org.springframework.batch.core.job.parameters.JobParametersBuilder
import org.springframework.batch.test.JobLauncherTestUtils
import org.springframework.batch.test.context.SpringBatchTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration

@SpringBatchTest
@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(initializers = [TestcontainersConfiguration.Initializer::class])
@Import(IntegrationTestConfiguration::class)
@org.springframework.boot.autoconfigure.EnableAutoConfiguration(
    exclude = [org.springframework.boot.quartz.autoconfigure.QuartzAutoConfiguration::class],
)
class SampleJobIntegrationTest(
    @Autowired private val jobLauncherTestUtils: JobLauncherTestUtils,
    @Autowired @Qualifier("sampleJob") private val sampleJob: Job,
) : BehaviorSpec({
    beforeSpec {
        jobLauncherTestUtils.job = sampleJob
    }

    Given("SampleJob이 구성되어 있을 때") {
        When("Job을 실행하면") {
            val jobParameters =
                JobParametersBuilder()
                    .addString("traceId", "test-trace-id")
                    .addLong("timestamp", System.currentTimeMillis())
                    .addString("triggeredBy", "test")
                    .toJobParameters()

            val jobExecution = jobLauncherTestUtils.launchJob(jobParameters)

            Then("Job이 성공적으로 완료된다") {
                jobExecution.status shouldBe BatchStatus.COMPLETED
                jobExecution.exitStatus.exitCode shouldBe "COMPLETED"
            }

            Then("Step이 실행된다") {
                val stepExecutions = jobExecution.stepExecutions
                stepExecutions shouldNotBe emptyList<Any>()
                stepExecutions.first().status shouldBe BatchStatus.COMPLETED
            }
        }
    }

    Given("sampleStep만 테스트할 때") {
        When("Step을 실행하면") {
            val jobParameters =
                JobParametersBuilder()
                    .addString("traceId", "test-step-trace-id")
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters()

            val jobExecution = jobLauncherTestUtils.launchStep("sampleStep", jobParameters)

            Then("Step이 성공적으로 완료된다") {
                jobExecution.status shouldBe BatchStatus.COMPLETED
            }

            Then("읽기/쓰기 건수가 기록된다") {
                val stepExecution = jobExecution.stepExecutions.first()
                stepExecution.readCount shouldNotBe null
                stepExecution.writeCount shouldNotBe null
            }
        }
    }
})