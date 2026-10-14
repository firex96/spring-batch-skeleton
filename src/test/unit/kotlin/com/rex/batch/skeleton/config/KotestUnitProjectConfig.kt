package com.rex.batch.skeleton.config

import io.kotest.core.config.AbstractProjectConfig
import io.kotest.core.spec.IsolationMode
import io.kotest.engine.concurrency.SpecExecutionMode

/**
 * Kotest 프로젝트 설정 (Unit Test)
 *
 * Unit 테스트에서는 Spring Context를 사용하지 않으므로 확장을 등록하지 않는다.
 * (형제 API 스켈레톤도 오버라이드하지 않는다 — 같은 의도를 같은 방식으로 표현한다.)
 */
object KotestUnitProjectConfig : AbstractProjectConfig() {
    override val isolationMode = IsolationMode.InstancePerLeaf

    // Kotest 6 에서 parallelism 이 제거됐다. 같은 의도(CPU 수 상한 병렬)를
    // SpecExecutionMode 로 표현한다. Concurrent 는 무제한이라 의도가 달라진다.
    override val specExecutionMode = SpecExecutionMode.LimitedConcurrency(Runtime.getRuntime().availableProcessors())
}