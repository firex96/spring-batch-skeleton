package com.rex.batch.skeleton.config.client

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * HTTP 클라이언트 속성 설정
 *
 * application.yml의 app.client.properties 설정을 매핑합니다.
 */
@ConfigurationProperties(prefix = "app.client.properties")
class ClientProperties(
    val httpClient: HttpClientProperties,
    val webClient: WebClientProperties,
    val connection: ConnectionPoolProperties,
) {
    class HttpClientProperties(
        val connectionTimeout: Int,
        val readTimeout: Int = 30,
        val writeTimeout: Int = 30,
    )

    class WebClientProperties(
        val memorySize: Int,
    )

    class ConnectionPoolProperties(
        val maxConnections: Int,
        val maxIdleTime: Int,
        val maxLifeTime: Int,
        val pendingAcquireTimeout: Int,
        val pendingAcquireMaxCount: Int,
        val evictInBackground: Int,
    )
}