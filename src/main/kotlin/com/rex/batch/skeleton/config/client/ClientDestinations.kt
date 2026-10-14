package com.rex.batch.skeleton.config.client

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * 외부 API 목적지 설정
 *
 * application.yml의 app.client.destinations 설정을 매핑합니다.
 */
@ConfigurationProperties(prefix = "app.client.destinations")
class ClientDestinations(
    val external: ClientInfo,
) {
    class ClientInfo(
        val url: String,
    )
}