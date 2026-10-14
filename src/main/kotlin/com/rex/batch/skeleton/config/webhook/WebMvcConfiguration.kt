package com.rex.batch.skeleton.config.webhook

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 * Web MVC 설정
 *
 * API Key 인증 인터셉터를 등록합니다.
 */
@Configuration
@ConditionalOnProperty(prefix = "webhook", name = ["enabled"], havingValue = "true", matchIfMissing = true)
class WebMvcConfiguration(
    private val apiKeyAuthInterceptor: ApiKeyAuthInterceptor,
) : WebMvcConfigurer {
    override fun addInterceptors(registry: InterceptorRegistry) {
        // /admin/api/** 전체 — 관리자 대시보드의 조회·트리거 경로.
        // /admin 의 HTML 셸은 열어 둔다(막으면 키를 입력할 화면 자체에 도달할 수 없다).
        //
        // excludePathPatterns 2줄(/api/health, /api/v1/jobs/available)은 대응 매핑이 없는
        // 죽은 설정이라 제거했다 — 남기면 "무엇이 열려 있나" 를 읽는 사람이 오독한다.
        registry.addInterceptor(apiKeyAuthInterceptor)
            .addPathPatterns("/api/**", "/admin/api/**")
    }
}