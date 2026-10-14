package com.rex.batch.skeleton.service.biz.client

import com.rex.batch.skeleton.model.SampleModel
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.HttpExchange
import org.springframework.web.service.annotation.PostExchange

/**
 * 외부 API 클라이언트 인터페이스
 *
 * @HttpExchange 어노테이션을 사용하여 선언적으로 HTTP 요청을 정의합니다.
 * HttpServiceProxyFactory를 통해 구현체가 자동 생성됩니다.
 */
@HttpExchange
interface ExternalApi {
    /**
     * 샘플 데이터 조회
     */
    @GetExchange("/samples/{id}")
    fun getSample(
        @PathVariable id: Long,
    ): SampleModel

    /**
     * 샘플 데이터 목록 조회
     */
    @GetExchange("/samples")
    fun getSamples(): List<SampleModel>

    /**
     * 샘플 데이터 생성
     */
    @PostExchange("/samples")
    fun createSample(
        @RequestBody sample: SampleModel,
    ): SampleModel
}