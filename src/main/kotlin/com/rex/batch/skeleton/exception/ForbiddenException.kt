package com.rex.batch.skeleton.exception

import com.rex.batch.skeleton.enums.ErrorCode

/**
 * 인증은 됐으나 그 키에 허용되지 않은 작업을 요청한 경우.
 *
 * 인증 실패(401, [InvalidApiKeyException])와 구분한다 — 소비자가 「키가 틀렸다」와
 * 「이 키로는 안 된다」를 구분할 수 있어야 한다.
 *
 * 메시지 인자를 받지 않는다. 응답은 언제나 코드의 고정 문구이고, 어느 키가 어느 Job 을 시도했는지는
 * 인가자(`JobTriggerAuthorizer`)가 구조화 로그로 남긴다 — 응답에 실으면 어떤 Job 이 있는지 알려 주는 단서가 된다.
 */
class ForbiddenException : CodedException(ErrorCode.FORBIDDEN)