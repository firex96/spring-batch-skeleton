package com.rex.batch.skeleton.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Job 트리거 파라미터 화이트리스트.
 *
 * Job 이 어떤 파라미터를 받을 수 있는지는 **Job 의 성질**이지 호출자의 권한이 아니므로,
 * API Key 별 스코프(`webhook.security.api-keys[].allowed-jobs`)와 달리 프로파일 공통
 * (base `application.yml`)에 둔다.
 *
 * **`traceId`·`timestamp` 는 어떤 Job 목록에도 넣지 않는다** — 시스템이 주입하는 값을
 * 호출자가 덮어쓰면 추적이 위조된다.
 *
 * ★ `BatchSkeletonApplication` 의 `@EnableConfigurationProperties` 명시 목록에 등록해야
 * 빈이 생성된다. batch 는 자동 스캔이 아니라 명시 목록을 쓴다.
 */
@ConfigurationProperties(prefix = "batch-skeleton.job-trigger")
data class JobTriggerProperties(
    /** Job 이름 → 허용 파라미터 키 목록. 미선언 Job 은 파라미터를 받지 않는다. */
    val parameters: Map<String, List<String>> = emptyMap(),
) {
    /**
     * 키 비교는 **정확 일치(대소문자 구분)** 다 — Job 쪽 `getString("procDate")` 와 어긋나면
     * 조용히 무시되는 파라미터가 생긴다.
     */
    fun allowedParametersOf(jobName: String): Set<String> = parameters[jobName]?.toSet() ?: emptySet()
}