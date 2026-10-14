package com.rex.batch.skeleton.exception

import com.rex.batch.skeleton.enums.ErrorCode

/**
 * Job 실행 정보를 찾을 수 없을 때 발생하는 예외
 */
class JobExecutionNotFoundException(
    val jobExecutionId: Long,
    message: String = "Job execution not found: $jobExecutionId",
    cause: Throwable? = null,
) : RuntimeException(message, cause) {
    val errorCode: ErrorCode = ErrorCode.JOB_EXECUTION_NOT_FOUND
}