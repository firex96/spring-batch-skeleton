package com.rex.batch.skeleton.config

import io.kotest.core.config.AbstractProjectConfig
import io.kotest.core.extensions.Extension
import io.kotest.core.spec.IsolationMode
import io.kotest.engine.concurrency.SpecExecutionMode
import io.kotest.extensions.spring.SpringAutowireConstructorExtension
import io.kotest.extensions.spring.SpringExtension

/**
 * Kotest 프로젝트 설정 (Integration Test)
 *
 * Spring Extension을 사용하여 Spring Context를 지원.
 */
object KotestIntegrationProjectConfig : AbstractProjectConfig() {
    override val isolationMode = IsolationMode.InstancePerLeaf

    // Kotest 6 에서 parallelism 이 제거됐다. 통합테스트는 순차 실행이어야 한다 —
    // Testcontainers 가 동시에 뜨면 컨테이너·포트가 경합한다.
    override val specExecutionMode = SpecExecutionMode.Sequential

    override val extensions: List<Extension> = listOf(
        // Kotest 6 에서 SpringExtension 이 생성자 주입을 더 이상 하지 않는다.
        // 이 확장을 함께 등록하지 않으면 생성자 파라미터가 있는 Spec 이
        // "should have a zero-arg constructor" 로 전부 실패한다.
        SpringAutowireConstructorExtension,
        SpringExtension(),
    )
}