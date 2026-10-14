package com.rex.batch.skeleton.config.webhook

import com.rex.batch.skeleton.exception.InvalidApiKeyException
import com.rex.batch.skeleton.util.logger
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

/**
 * API Key 인증 인터셉터
 *
 * X-API-Key 헤더를 검증하여 인증된 요청만 허용합니다.
 */
@Component
class ApiKeyAuthInterceptor(
    private val webhookProperties: WebhookProperties,
) : HandlerInterceptor {
    private val log = logger()

    companion object {
        /**
         * 인증에 성공한 키의 **이름**을 담는 request attribute.
         *
         * 원문 키는 절대 넣지 않는다 — attribute 는 로그·에러 페이지 등으로 새어나갈 수 있다.
         */
        const val ATTRIBUTE_AUTHENTICATED_KEY_NAME = "webhook.authenticatedKeyName"
    }

    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean {
        val apiKeyHeader = webhookProperties.security.apiKeyHeader
        val apiKey = request.getHeader(apiKeyHeader)

        if (apiKey.isNullOrBlank()) {
            log.warn("API Key 누락 - uri={}, remoteAddr={}", request.requestURI, request.remoteAddr)
            throw InvalidApiKeyException("API Key is required")
        }

        val validApiKeys = webhookProperties.security.apiKeys.map { it.key }
        if (apiKey !in validApiKeys) {
            log.warn("유효하지 않은 API Key - uri={}, remoteAddr={}", request.requestURI, request.remoteAddr)
            throw InvalidApiKeyException("Invalid API Key")
        }

        val keyName = webhookProperties.security.apiKeys.find { it.key == apiKey }?.name
        // 인가자(JobTriggerAuthorizer)가 「누가 인증했는지」를 알아야 스코프를 판정할 수 있다.
        request.setAttribute(ATTRIBUTE_AUTHENTICATED_KEY_NAME, keyName)
        log.debug("API Key 인증 성공 - keyName={}, uri={}", keyName, request.requestURI)

        return true
    }
}