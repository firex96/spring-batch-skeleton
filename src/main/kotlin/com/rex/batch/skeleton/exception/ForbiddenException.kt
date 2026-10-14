package com.rex.batch.skeleton.exception

import com.rex.batch.skeleton.enums.ErrorCode

/**
 * 인증은 됐으나 그 키에 허용되지 않은 작업을 요청한 경우.
 *
 * 인증 실패(401, [InvalidApiKeyException])와 구분한다 — 소비자가 「키가 틀렸다」와
 * 「이 키로는 안 된다」를 구분할 수 있어야 한다.
 */
class ForbiddenException(
    message: String = "Operation not permitted for this API key",
) : RuntimeException(message) {
    val errorCode: ErrorCode = ErrorCode.FORBIDDEN
}