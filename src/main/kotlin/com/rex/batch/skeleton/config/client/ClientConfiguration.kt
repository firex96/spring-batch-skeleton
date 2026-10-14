package com.rex.batch.skeleton.config.client

import com.rex.batch.skeleton.service.biz.client.ExternalApi
import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.support.WebClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory
import reactor.netty.http.client.HttpClient
import reactor.netty.resources.ConnectionProvider
import java.time.Duration

/**
 * HTTP 클라이언트 설정
 *
 * WebClient를 사용한 외부 API 호출 설정.
 * ConnectionProvider로 커넥션 풀 관리.
 */
@Configuration
class ClientConfiguration(
    private val clients: ClientDestinations,
    private val properties: ClientProperties,
) {
    private fun webClientBuilder(): WebClient.Builder {
        val httpClient =
            HttpClient
                .create(connectionProvider())
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, properties.httpClient.connectionTimeout)
                .responseTimeout(Duration.ofMillis(properties.httpClient.connectionTimeout.toLong()))
                .doOnConnected { connection ->
                    connection.addHandlerLast(ReadTimeoutHandler(properties.httpClient.readTimeout))
                    connection.addHandlerLast(WriteTimeoutHandler(properties.httpClient.writeTimeout))
                }

        return WebClient
            .builder()
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .codecs { configurer ->
                configurer.defaultCodecs().maxInMemorySize(properties.webClient.memorySize)
            }.clientConnector(ReactorClientHttpConnector(httpClient))
    }

    private fun connectionProvider(): ConnectionProvider =
        ConnectionProvider
            .builder("batch-provider")
            .maxConnections(properties.connection.maxConnections)
            .maxIdleTime(Duration.ofSeconds(properties.connection.maxIdleTime.toLong()))
            .maxLifeTime(Duration.ofSeconds(properties.connection.maxLifeTime.toLong()))
            .pendingAcquireTimeout(Duration.ofSeconds(properties.connection.pendingAcquireTimeout.toLong()))
            .pendingAcquireMaxCount(properties.connection.pendingAcquireMaxCount)
            .evictInBackground(Duration.ofSeconds(properties.connection.evictInBackground.toLong()))
            .lifo()
            .build()

    @Bean
    fun externalApi(): ExternalApi {
        val webClient = webClientBuilder().baseUrl(clients.external.url).build()
        val adapter = WebClientAdapter.create(webClient)
        val factory = HttpServiceProxyFactory.builderFor(adapter).build()
        return factory.createClient(ExternalApi::class.java)
    }

    @Bean
    fun webClientBuilder(properties: ClientProperties): WebClient.Builder = webClientBuilder()
}