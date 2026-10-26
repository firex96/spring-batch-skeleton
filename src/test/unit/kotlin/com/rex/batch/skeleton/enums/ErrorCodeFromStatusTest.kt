package com.rex.batch.skeleton.enums

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

/**
 * `ErrorCode.fromStatus` — 상태 기반 응답의 코드 선택. 일반 코드에서만 고른다(404 는 JOB_NOT_FOUND 가 아니다).
 * 기대 표 전체로 최종 매핑을 확인하고(목록 중복은 `BY_STATUS` 초기화의 `check` 가 거부한다), 목록 밖 상태는 400·500 코드로 낮춘다.
 */
class ErrorCodeFromStatusTest : DescribeSpec({

    describe("fromStatus") {
        val expected =
            mapOf(
                400 to ErrorCode.INVALID_REQUEST,
                404 to ErrorCode.PATH_NOT_FOUND,
                405 to ErrorCode.METHOD_NOT_ALLOWED,
                406 to ErrorCode.NOT_ACCEPTABLE,
                413 to ErrorCode.PAYLOAD_TOO_LARGE,
                415 to ErrorCode.UNSUPPORTED_MEDIA_TYPE,
                500 to ErrorCode.INTERNAL_ERROR,
                503 to ErrorCode.SERVICE_UNAVAILABLE,
            )

        expected.forEach { (status, errorCode) ->
            it("$status 은 ${errorCode.name} 다") {
                ErrorCode.fromStatus(status) shouldBe errorCode
                ErrorCode.fromStatus(status).status.value() shouldBe status
            }
        }

        it("목록 밖 4xx(418)는 INVALID_REQUEST 다") {
            ErrorCode.fromStatus(418) shouldBe ErrorCode.INVALID_REQUEST
        }

        it("목록 밖 5xx(504)는 INTERNAL_ERROR 다") {
            ErrorCode.fromStatus(504) shouldBe ErrorCode.INTERNAL_ERROR
        }
    }
})