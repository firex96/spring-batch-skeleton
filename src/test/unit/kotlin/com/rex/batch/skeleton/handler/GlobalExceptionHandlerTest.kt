package com.rex.batch.skeleton.handler

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.string.shouldNotContain
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders

/**
 * 업무 예외는 `CodedException` 핸들러 하나가 `errorCode` 로 응답하고(`InvalidJobParameterException` 만 전용 핸들러),
 * 응답 `message`·`details` 에 내부 메시지가 나가지 않는지 본다. Spring Context 없이 standalone MockMvc 로 돈다.
 */
class GlobalExceptionHandlerTest : DescribeSpec({

    val mockMvc: MockMvc =
        MockMvcBuilders
            .standaloneSetup(ExceptionProbeController())
            .setControllerAdvice(GlobalExceptionHandler())
            .build()

    describe("CodedException 단일 핸들러") {

        it("ItemNotFoundException 은 404 COMMON_002 다") {
            mockMvc
                .perform(get("/probe/item-not-found"))
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.code").value("COMMON_002"))
                .andExpect(jsonPath("$.message").value("Product not found with id: 1"))
        }

        it("BatchProcessException 은 500 BATCH_001 이고 예외 메시지 대신 코드 문구를 싣는다") {
            mockMvc
                .perform(get("/probe/batch-process"))
                .andExpect(status().isInternalServerError)
                .andExpect(jsonPath("$.code").value("BATCH_001"))
                .andExpect(jsonPath("$.message").value("Batch process failed"))
                .andExpect(jsonPath("$.details").doesNotExist())
                .bodyShouldNotContain("chunk write failed")
        }

        it("JobNotFoundException 은 404 JOB_001 이고 details 에 jobName 을 싣는다") {
            mockMvc
                .perform(get("/probe/job-not-found"))
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.code").value("JOB_001"))
                .andExpect(jsonPath("$.message").value("Job not found: sampleJob"))
                .andExpect(jsonPath("$.details").value("jobName: sampleJob"))
        }

        it("JobExecutionNotFoundException 은 404 JOB_002 이고 details 에 jobExecutionId 를 싣는다") {
            mockMvc
                .perform(get("/probe/job-execution-not-found"))
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.code").value("JOB_002"))
                .andExpect(jsonPath("$.details").value("jobExecutionId: 7"))
        }

        it("InvalidApiKeyException 은 401 AUTH_001 이고 예외 메시지를 싣는다") {
            mockMvc
                .perform(get("/probe/invalid-api-key"))
                .andExpect(status().isUnauthorized)
                .andExpect(jsonPath("$.code").value("AUTH_001"))
                .andExpect(jsonPath("$.message").value("Invalid API Key"))
        }
    }

    describe("응답 노출 정책") {

        it("ForbiddenException 은 메시지 인자가 없어 403 AUTH_002 코드 문구만 나간다") {
            mockMvc
                .perform(get("/probe/forbidden"))
                .andExpect(status().isForbidden)
                .andExpect(jsonPath("$.code").value("AUTH_002"))
                .andExpect(jsonPath("$.message").value("Operation not permitted for this API key"))
                .andExpect(jsonPath("$.details").doesNotExist())
        }

        it("InvalidJobParameterException 은 400 COMMON_001 이고 거부 키 이름만 싣는다") {
            mockMvc
                .perform(get("/probe/invalid-job-parameter"))
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.code").value("COMMON_001"))
                .andExpect(jsonPath("$.message").value("Invalid request"))
                .andExpect(jsonPath("$.details").value("rejectedKeys=[a, b]"))
                .bodyShouldNotContain("sampleJob")
        }
    }

    describe("프레임워크 예외 — 내부 메시지를 응답에 싣지 않는다") {

        it("JobInstanceAlreadyCompleteException 은 409 JOB_003 이고 details 가 없다") {
            mockMvc
                .perform(get("/probe/job-instance-complete"))
                .andExpect(status().isConflict)
                .andExpect(jsonPath("$.code").value("JOB_003"))
                .andExpect(jsonPath("$.message").value("Job instance already complete"))
                .andExpect(jsonPath("$.details").doesNotExist())
                .bodyShouldNotContain("secret")
        }

        it("JobRestartException 은 409 JOB_003 이고 details 가 없다") {
            mockMvc
                .perform(get("/probe/job-restart"))
                .andExpect(status().isConflict)
                .andExpect(jsonPath("$.message").value("Job cannot be restarted"))
                .andExpect(jsonPath("$.details").doesNotExist())
                .bodyShouldNotContain("secret")
        }

        it("처리하지 않은 예외는 500 COMMON_999 이고 details 가 없다") {
            mockMvc
                .perform(get("/probe/unexpected"))
                .andExpect(status().isInternalServerError)
                .andExpect(jsonPath("$.code").value("COMMON_999"))
                .andExpect(jsonPath("$.details").doesNotExist())
                .bodyShouldNotContain("internal secret detail")
        }
    }
})

/** 응답 본문 어디에도 [text] 가 없는지 본다 — 내부 상세가 `message`·`details` 밖으로도 새지 않는지 확인한다. */
private fun ResultActions.bodyShouldNotContain(text: String): ResultActions = apply { andReturn().response.contentAsString shouldNotContain text }