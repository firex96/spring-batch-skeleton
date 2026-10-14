package com.rex.batch.skeleton.config

import com.rex.batch.skeleton.config.webhook.WebhookProperties
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * OpenAPI(Swagger) 설정
 *
 * API Key 인증을 Swagger UI에서 사용할 수 있도록 설정합니다.
 */
@Configuration
class OpenApiConfiguration(
    private val webhookProperties: WebhookProperties,
) {
    companion object {
        private const val SECURITY_SCHEME_NAME = "ApiKeyAuth"
    }

    @Bean
    fun openAPI(): OpenAPI {
        val apiKeyHeader = webhookProperties.security.apiKeyHeader

        return OpenAPI()
            .info(
                Info()
                    .title("Batch Skeleton API")
                    .description("Spring Batch 스켈레톤 프로젝트 API")
                    .version("1.0.0"),
            )
            .components(
                Components()
                    .addSecuritySchemes(
                        SECURITY_SCHEME_NAME,
                        SecurityScheme()
                            .type(SecurityScheme.Type.APIKEY)
                            .`in`(SecurityScheme.In.HEADER)
                            .name(apiKeyHeader),
                    ),
            )
            .addSecurityItem(SecurityRequirement().addList(SECURITY_SCHEME_NAME))
    }
}