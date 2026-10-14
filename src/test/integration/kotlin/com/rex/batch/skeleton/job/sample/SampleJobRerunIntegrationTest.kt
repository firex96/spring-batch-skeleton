package com.rex.batch.skeleton.job.sample

import com.rex.batch.skeleton.config.IntegrationTestConfiguration
import com.rex.batch.skeleton.config.TestcontainersConfiguration
import com.rex.batch.skeleton.constant.Constant
import com.rex.batch.skeleton.dto.SampleDto
import com.rex.batch.skeleton.service.biz.SampleBizService
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.job.Job
import org.springframework.batch.core.job.parameters.JobParameters
import org.springframework.batch.core.job.parameters.JobParametersBuilder
import org.springframework.batch.test.JobLauncherTestUtils
import org.springframework.batch.test.context.SpringBatchTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration

/**
 * Job 재실행 멱등 계약(`.claude/rules/conventions.md` 「레이어드 아키텍처」).
 *
 * `sampleJob` 은 처리 완료 상태를 **데이터에** 남기고(`COMPLETED`·`processed_at`) Reader 술어
 * (`status = 'PENDING' AND processed_at IS NULL`)가 그것을 걸러 낸다. 그래서 같은 데이터에 다시 돌려도
 * 2회차는 0건을 읽고 부작용이 중복되지 않아야 한다. 실행 단위 파라미터(`timestamp`)가 JobInstance 를
 * 분리하므로 재실행 자체는 항상 허용된다 — `JobInstanceAlreadyCompleteException` 에 의존하지 않는다.
 *
 * **동기 실행**(`JobLauncherTestUtils.launchJob`)으로 본다 — 웹훅 경로의 `asyncJobLauncher` 는 반환 직후
 * 상태 단언이 경합하므로 이 테스트의 대상이 아니다. `timestamp` 는 고정값 `1L`·`2L` 로 두 JobInstance 를
 * 결정적으로 분리한다(`System.currentTimeMillis()` 두 번은 같은 ms 가 될 수 있다).
 *
 * 테스트 실행:
 * ```bash
 * ./gradlew integrationTest --tests "*.job.sample.SampleJobRerunIntegrationTest"
 * ```
 */
@SpringBatchTest
@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(initializers = [TestcontainersConfiguration.Initializer::class])
@Import(IntegrationTestConfiguration::class)
@org.springframework.boot.autoconfigure.EnableAutoConfiguration(
    exclude = [org.springframework.boot.quartz.autoconfigure.QuartzAutoConfiguration::class],
)
class SampleJobRerunIntegrationTest(
    @Autowired private val jobLauncherTestUtils: JobLauncherTestUtils,
    @Autowired @Qualifier("sampleJob") private val sampleJob: Job,
    @Autowired private val sampleBizService: SampleBizService,
) : BehaviorSpec({
    beforeSpec {
        jobLauncherTestUtils.job = sampleJob
    }

    fun params(run: Int): JobParameters =
        JobParametersBuilder()
            .addString(Constant.KEY_TRACE_ID, "rerun-$run")
            .addLong("timestamp", run.toLong())
            .addString("triggeredBy", "rerun-test")
            .toJobParameters()

    fun snapshot(ids: List<Long>) = ids.map { id -> sampleBizService.find(id)!!.let { Triple(it.id, it.status, it.processedAt) } }

    // 통합 테스트는 IsolationMode.InstancePerLeaf 다 — Then 마다 스펙 인스턴스가 새로 만들어져 컨테이너 지역 변수를
    // 공유할 수 없다. 재실행 계약은 1회차·2회차가 한 흐름이므로 하나의 Then 안에서 순서대로 검증한다.
    Given("PENDING 상태의 sample 행 3개가 있을 때") {
        When("같은 데이터에 sampleJob 을 두 번 실행하면") {
            Then("R-1~R-4 1회차는 처리하고 2회차는 0건을 읽으며 데이터는 그대로다 — 재실행 멱등") {
                // 다른 통합 테스트가 남긴 PENDING 행이 있을 수 있으므로 행 수를 세지 않고, 시드 3행의 상태 전이만 본다.
                val seededIds = (1..3).map { sampleBizService.add(SampleDto(name = "rerun-seed-$it")).id }
                try {
                    // R-1 1회차 — 시드 3행이 전부 COMPLETED·processed_at 채워짐
                    val first = jobLauncherTestUtils.launchJob(params(1))
                    val firstStep = first.stepExecutions.single()
                    first.status shouldBe BatchStatus.COMPLETED
                    firstStep.writeCount shouldBe firstStep.readCount
                    val afterFirst = snapshot(seededIds)
                    afterFirst.all { it.second == "COMPLETED" && it.third != null } shouldBe true

                    // R-2 2회차 — 새 JobInstance, 재실행 자체는 막지 않는다
                    val second = jobLauncherTestUtils.launchJob(params(2))
                    val secondStep = second.stepExecutions.single()
                    second.jobInstance.instanceId shouldNotBe first.jobInstance.instanceId
                    second.status shouldBe BatchStatus.COMPLETED

                    // R-3 2회차는 0건을 읽고 0건을 쓴다
                    secondStep.readCount shouldBe 0L
                    secondStep.writeCount shouldBe 0L

                    // R-4 데이터 스냅샷이 1회차와 같다 — 부작용이 중복되지 않는다 (processedAt 불변)
                    snapshot(seededIds) shouldBe afterFirst
                } finally {
                    seededIds.forEach { id -> sampleBizService.remove(id) }
                }
            }
        }
    }
})