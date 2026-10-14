package com.rex.batch.skeleton.listener

import com.rex.batch.skeleton.constant.Constant
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.slf4j.MDC
import org.springframework.batch.core.job.JobExecution
import org.springframework.batch.core.job.JobInstance
import org.springframework.batch.core.job.parameters.JobParameters
import org.springframework.batch.core.job.parameters.JobParametersBuilder
import java.util.UUID

/**
 * `JobTraceListener` 가 Job 실행 경계에서 MDC 를 채우고 지우는지.
 *
 * **J-1 이 traceId 사슬을 반대편에서 고정한다** — `sibling-convergence` 는
 * HTTP 헤더 → MDC → `JobParameters` 까지 봤고, 여기서 `JobParameters` → MDC 를 본다.
 * 둘이 이어져야 웹훅 호출자의 traceId 가 Job 실행 로그까지 간다.
 *
 * **J-4 가 없으면** 스레드 재사용 시 다음 Job 이 남의 traceId 로 로깅된다.
 */
class JobTraceListenerTest : BehaviorSpec({

    afterTest { MDC.clear() }

    fun execution(parameters: JobParameters = JobParameters()) =
        JobExecution(7L, JobInstance(1L, "sampleJob"), parameters)

    val listener = JobTraceListener()

    Given("JobParameters 에 traceId 가 있는 실행") {
        Then("J-1 그 값을 MDC 에 그대로 넣는다") {
            val parameters =
                JobParametersBuilder()
                    .addString(Constant.KEY_TRACE_ID, "verify-abc-123")
                    .toJobParameters()

            listener.beforeJob(execution(parameters))

            MDC.get(Constant.KEY_TRACE_ID) shouldBe "verify-abc-123"
        }
    }

    Given("traceId 가 없는 실행") {
        Then("J-2 매번 새 UUID 를 만들어 넣는다") {
            listener.beforeJob(execution())
            val first = MDC.get(Constant.KEY_TRACE_ID)
            MDC.clear()
            listener.beforeJob(execution())
            val second = MDC.get(Constant.KEY_TRACE_ID)

            first shouldNotBe null
            first.isNotBlank() shouldBe true
            // 「비어 있지 않다」만 보면 고정 상수로 회귀해도 통과한다.
            first shouldNotBe second
            shouldNotThrowAny { UUID.fromString(first) }
            shouldNotThrowAny { UUID.fromString(second) }
        }
    }

    Given("어떤 실행이든") {
        Then("J-3 jobName 과 jobExecutionId 도 MDC 에 넣는다") {
            listener.beforeJob(execution())

            MDC.get(Constant.KEY_JOB_NAME) shouldBe "sampleJob"
            MDC.get(Constant.KEY_JOB_EXECUTION_ID) shouldBe "7"
        }
    }

    Given("beforeJob 으로 MDC 가 채워진 상태") {
        Then("J-4 afterJob 이 세 키를 모두 지운다") {
            val jobExecution = execution()
            listener.beforeJob(jobExecution)

            listener.afterJob(jobExecution)

            // 남으면 스레드 풀 재사용 시 다음 Job 이 남의 traceId 로 로깅된다.
            MDC.get(Constant.KEY_TRACE_ID).shouldBeNull()
            MDC.get(Constant.KEY_JOB_NAME).shouldBeNull()
            MDC.get(Constant.KEY_JOB_EXECUTION_ID).shouldBeNull()
        }
    }
})