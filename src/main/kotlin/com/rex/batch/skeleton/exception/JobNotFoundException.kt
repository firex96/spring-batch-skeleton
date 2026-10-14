package com.rex.batch.skeleton.exception

import com.rex.batch.skeleton.enums.ErrorCode

/**
 * Job을 찾을 수 없을 때 발생하는 예외
 */
class JobNotFoundException(
    val jobName: String,
    message: String = "Job not found: $jobName",
    cause: Throwable? = null,
) : RuntimeException(message, cause) {
    val errorCode: ErrorCode = ErrorCode.JOB_NOT_FOUND
}