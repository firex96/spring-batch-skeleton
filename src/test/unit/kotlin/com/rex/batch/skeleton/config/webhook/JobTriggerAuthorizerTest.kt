package com.rex.batch.skeleton.config.webhook

import com.rex.batch.skeleton.config.properties.JobTriggerProperties
import com.rex.batch.skeleton.exception.ForbiddenException
import com.rex.batch.skeleton.exception.InvalidJobParameterException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import jakarta.servlet.http.HttpServletRequest

/**
 * Job 트리거 인가 판정 로직.
 *
 * 이 클래스가 없던 시절에는 **API Key 가 허용 목록에 있기만 하면 등록된 아무 Job 이나**
 * 실행할 수 있었다(OPTI KI-002). 아래 케이스가 그 구멍을 닫는다.
 */
class JobTriggerAuthorizerTest : BehaviorSpec({

    fun requestWith(keyName: String?): HttpServletRequest =
        mockk<HttpServletRequest>(relaxed = true).also {
            every { it.getAttribute(ApiKeyAuthInterceptor.ATTRIBUTE_AUTHENTICATED_KEY_NAME) } returns keyName
            every { it.requestURI } returns "/api/v1/jobs/sampleJob/trigger"
            every { it.remoteAddr } returns "127.0.0.1"
        }

    fun authorizer(
        entries: List<WebhookProperties.ApiKeyEntry>,
        parameters: Map<String, List<String>> = mapOf("sampleJob" to emptyList()),
    ) = JobTriggerAuthorizer(
        WebhookProperties(security = WebhookProperties.SecurityProperties(apiKeys = entries)),
        JobTriggerProperties(parameters = parameters),
    )

    Given("인증 주체가 없는 요청") {
        Then("403 으로 거부한다 — 인터셉터가 미등록된 경우(webhook.enabled=false)가 여기 걸린다") {
            shouldThrow<ForbiddenException> {
                authorizer(listOf(WebhookProperties.ApiKeyEntry("k", "v", listOf("sampleJob"))))
                    .authorize(requestWith(null), "sampleJob", null)
            }
        }
    }

    Given("allowedJobs 가 빈 키") {
        Then("어떤 Job 도 트리거하지 못한다 — 기본값이 fail-closed 다") {
            shouldThrow<ForbiddenException> {
                authorizer(listOf(WebhookProperties.ApiKeyEntry("narrow", "v", emptyList())))
                    .authorize(requestWith("narrow"), "sampleJob", null)
            }
        }
    }

    Given("허용 목록에 없는 Job") {
        Then("403 으로 거부한다 — 등록된 빈이어도 설정에 없으면 실행되지 않는다") {
            shouldThrow<ForbiddenException> {
                authorizer(listOf(WebhookProperties.ApiKeyEntry("k", "v", listOf("otherJob"))))
                    .authorize(requestWith("k"), "sampleJob", null)
            }
        }
    }

    Given("같은 name 을 가진 엔트리가 둘 (스코프가 다름)") {
        Then("403 으로 거부한다 — 좁은 키로 인증하고 넓은 스코프를 얻을 수 없다") {
            val entries =
                listOf(
                    WebhookProperties.ApiKeyEntry("dup", "narrow-key", emptyList()),
                    WebhookProperties.ApiKeyEntry("dup", "wide-key", listOf("sampleJob")),
                )
            shouldThrow<ForbiddenException> {
                authorizer(entries).authorize(requestWith("dup"), "sampleJob", null)
            }
        }
    }

    Given("허용 목록 밖 파라미터") {
        Then("400 으로 거부하고 거부된 키 이름을 남긴다") {
            val ex =
                shouldThrow<InvalidJobParameterException> {
                    authorizer(listOf(WebhookProperties.ApiKeyEntry("k", "v", listOf("sampleJob"))))
                        .authorize(requestWith("k"), "sampleJob", mapOf("traceId" to "forged"))
                }
            ex.jobName shouldBe "sampleJob"
            ex.rejectedKeys.sorted() shouldContainExactly listOf("traceId")
        }

        Then("예외 메시지에 파라미터 **값** 이 들어가지 않는다") {
            val ex =
                shouldThrow<InvalidJobParameterException> {
                    authorizer(listOf(WebhookProperties.ApiKeyEntry("k", "v", listOf("sampleJob"))))
                        .authorize(requestWith("k"), "sampleJob", mapOf("traceId" to "forged-value-must-not-leak"))
                }
            ex.message!!.contains("forged-value-must-not-leak") shouldBe false
        }
    }

    Given("허용된 키·Job·파라미터") {
        Then("인증 키 이름을 반환한다") {
            authorizer(listOf(WebhookProperties.ApiKeyEntry("k", "v", listOf("sampleJob"))))
                .authorize(requestWith("k"), "sampleJob", emptyMap()) shouldBe "k"
        }

        Then("본문이 없어도(parameters=null) 통과한다 — @RequestBody(required = false) 계약") {
            authorizer(listOf(WebhookProperties.ApiKeyEntry("k", "v", listOf("sampleJob"))))
                .authorize(requestWith("k"), "sampleJob", null) shouldBe "k"
        }

        Then("허용 목록에 있는 파라미터는 값이 있어도 통과한다") {
            // 거부 경로만 검증하면 화이트리스트가 「전부 거부」로 잘못 구현돼도 통과한다.
            authorizer(
                entries = listOf(WebhookProperties.ApiKeyEntry("k", "v", listOf("sampleJob"))),
                parameters = mapOf("sampleJob" to listOf("procDate")),
            ).authorize(requestWith("k"), "sampleJob", mapOf("procDate" to "2026-08-27")) shouldBe "k"
        }
    }
})