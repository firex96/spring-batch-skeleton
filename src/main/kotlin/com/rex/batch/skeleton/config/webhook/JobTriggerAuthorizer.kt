package com.rex.batch.skeleton.config.webhook

import com.rex.batch.skeleton.config.properties.JobTriggerProperties
import com.rex.batch.skeleton.exception.ForbiddenException
import com.rex.batch.skeleton.exception.InvalidJobParameterException
import com.rex.batch.skeleton.util.logger
import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Component

/**
 * Job 트리거 인가 — 두 진입점(`JobController`, `AdminApiController`)이 공유한다.
 *
 * 검사 순서: ① 인증 주체 존재 → ② 키 엔트리 유일성 → ③ 키의 `allowedJobs` 포함 여부(403)
 * → ④ Job 별 허용 파라미터 키(400).
 *
 * ③이 앞에 서므로 **설정에 적히지 않은 Job 은 등록 여부와 무관하게 실행되지 않는다**
 * (`JobLauncherBizService.findJob` 의 원시 빈 조회 문제도 함께 닫힌다).
 *
 * **`@ConditionalOnProperty` 를 붙이지 않는다.** `webhook.enabled=false` 면 인터셉터가
 * 미등록되어 attribute 가 없으므로 트리거는 403 으로 닫힌다 — 조회는 열리되 실행은 막는다.
 * 이 빈을 조건부로 만들면 **플래그 하나로 임의 Job 실행이 무인증 개방된다.**
 *
 * 이 클래스는 웹 계층(`config/webhook/`) 컴포넌트다. DTO·Entity·Repository 를 건드리지
 * 않으며, 업무 경로 `Controller → MappingService → BizService → EntityService` 는 그대로다.
 */
@Component
class JobTriggerAuthorizer(
    private val webhookProperties: WebhookProperties,
    private val jobTriggerProperties: JobTriggerProperties,
) {
    private val log = logger()

    /**
     * 인가 통과 시 인증된 키 이름을 반환한다(호출부 로깅용).
     *
     * @param parameters 본문이 없으면 `null` — 파라미터 0개로 취급한다
     *   (`@RequestBody(required = false)` 계약을 보존한다).
     * @throws ForbiddenException 인증 attribute 부재 · 엔트리 모호 · 해당 Job 미허용
     * @throws InvalidJobParameterException 허용 목록 밖 파라미터 키 포함
     */
    fun authorize(
        request: HttpServletRequest,
        jobName: String,
        parameters: Map<String, String>?,
    ): String {
        val keyName = request.getAttribute(ApiKeyAuthInterceptor.ATTRIBUTE_AUTHENTICATED_KEY_NAME) as? String
        if (keyName.isNullOrBlank()) {
            log.warn(
                "인증 정보 없는 Job 트리거 시도 - jobName={}, uri={}, remoteAddr={}",
                jobName,
                request.requestURI,
                request.remoteAddr,
            )
            throw ForbiddenException()
        }

        // 인터셉터는 key 로 매칭해 name 만 넘기므로, 같은 name 이 둘 이상이면 어느 엔트리가
        // 인증했는지 알 수 없다. 첫 매칭을 쓰면 인증하지 않은 엔트리의 넓은 스코프가
        // 적용될 수 있어 fail-closed 로 거부한다.
        val entry = webhookProperties.security.apiKeys.filter { it.name == keyName }.singleOrNull()
        if (entry == null || jobName !in entry.allowedJobs) {
            log.warn(
                "허용되지 않은 Job 트리거 - keyName={}, jobName={}, uri={}, remoteAddr={}",
                keyName,
                jobName,
                request.requestURI,
                request.remoteAddr,
            )
            throw ForbiddenException()
        }

        val rejectedKeys = parameters.orEmpty().keys - jobTriggerProperties.allowedParametersOf(jobName)
        if (rejectedKeys.isNotEmpty()) {
            log.warn(
                "허용되지 않은 Job 파라미터 - keyName={}, jobName={}, rejectedKeys={}, uri={}, remoteAddr={}",
                keyName,
                jobName,
                rejectedKeys.sorted(),
                request.requestURI,
                request.remoteAddr,
            )
            throw InvalidJobParameterException(jobName, rejectedKeys)
        }

        return keyName
    }
}