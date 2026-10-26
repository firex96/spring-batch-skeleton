package com.rex.batch.skeleton.exception

import com.rex.batch.skeleton.enums.ErrorCode

/**
 * 허용 목록 밖의 Job 파라미터가 요청에 포함된 경우.
 *
 * @param rejectedKeys 거부된 파라미터 **키 이름만** 담는다. 값은 담지 않는다 —
 *   위조 시도 값이 예외 메시지를 타고 로그로 새어나가면 안 된다.
 */
class InvalidJobParameterException(
    val jobName: String,
    val rejectedKeys: Set<String>,
    message: String = "Parameters not allowed for job $jobName: ${rejectedKeys.sorted()}",
) : CodedException(ErrorCode.INVALID_REQUEST, message, "rejectedKeys=${rejectedKeys.sorted()}")