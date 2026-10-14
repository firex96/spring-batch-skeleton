package com.rex.batch.skeleton.config

import com.rex.batch.skeleton.constant.Constant
import com.rex.batch.skeleton.context.AuditorContext
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import org.slf4j.MDC
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * **데코레이터가 옳게 동작하는 것과 그것이 배선된 것은 다른 문제다.**
 * [AuditorContextTaskDecoratorTest] 는 앞을, 이 스펙은 뒤를 본다.
 *
 * 로깅(MDC)과 감사 주체는 채널이 다르므로 둘을 합성해 배선한다.
 * 하나만 걸리면 그 축이 조용히 전파되지 않는다.
 *
 * **이 레포는 executor 가 둘이다.** 하나만 배선하면 그 경로만 조용히 다르게
 * 동작하므로 N-2e 로 나머지도 본다.
 */
class AsyncConfigurationTest : BehaviorSpec({

    afterTest {
        AuditorContext.clear()
        MDC.clear()
    }

    /**
     * 제출 시점의 두 축을 세우고, 작업 안에서 둘 다 보이는지 본다.
     *
     * 이 레포의 `AsyncConfiguration` 은 빈 메서드 안에서 이미 `initialize()` 를 부른다.
     * 다시 부르면 스레드 풀을 새로 만들므로 여기서는 부르지 않는다.
     */
    fun observe(executor: ThreadPoolTaskExecutor): Pair<String?, Long?> {
        // 이미 초기화돼 있다 — 중복 호출하지 않는다.
        var trace: String? = null
        var auditor: Long? = null
        val done = CountDownLatch(1)
        try {
            MDC.put(Constant.KEY_TRACE_ID, "wiring-check")
            AuditorContext.set(42L)
            executor.execute {
                trace = MDC.get(Constant.KEY_TRACE_ID)
                auditor = AuditorContext.get()
                done.countDown()
            }
            done.await(5, TimeUnit.SECONDS) shouldBe true
        } finally {
            executor.shutdown()
        }
        return trace to auditor
    }

    Given("taskExecutor() 로 제출한 작업") {
        Then("N-2d MDC 와 감사 주체가 둘 다 전파된다") {
            val (trace, auditor) = observe(AsyncConfiguration().taskExecutor() as ThreadPoolTaskExecutor)

            trace shouldBe "wiring-check"
            auditor shouldBe 42L
        }
    }

    Given("batchTaskExecutor() 로 제출한 작업") {
        Then("N-2e MDC 와 감사 주체가 둘 다 전파된다") {
            val (trace, auditor) = observe(AsyncConfiguration().batchTaskExecutor() as ThreadPoolTaskExecutor)

            trace shouldBe "wiring-check"
            auditor shouldBe 42L
        }
    }
})