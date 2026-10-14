package com.rex.batch.skeleton.config.webhook

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Webhook 설정 프로퍼티
 */
@ConfigurationProperties(prefix = "webhook")
data class WebhookProperties(
    /** Webhook 기능 활성화 여부 */
    val enabled: Boolean = true,
    /** 보안 설정 */
    val security: SecurityProperties = SecurityProperties(),
) {
    data class SecurityProperties(
        /** API Key 헤더 이름 */
        val apiKeyHeader: String = "X-API-Key",
        /** 등록된 API Keys */
        val apiKeys: List<ApiKeyEntry> = emptyList(),
    )

    data class ApiKeyEntry(
        /** API Key 이름 (식별용) */
        val name: String,
        /** API Key 값 */
        val key: String,
        /**
         * 이 키로 트리거할 수 있는 Job 이름 목록.
         *
         * **기본값이 빈 목록이다 — fail-closed.** 설정에 `allowed-jobs` 를 적지 않은 키는
         * 아무 Job 도 트리거하지 못한다. 키를 가졌다는 사실만으로 등록된 모든 Job 을
         * 돌릴 수 있으면 웹훅 키 하나가 곧 전체 실행 권한이 된다.
         */
        val allowedJobs: List<String> = emptyList(),
    )
}