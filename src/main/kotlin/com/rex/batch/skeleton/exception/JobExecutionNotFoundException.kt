package com.rex.batch.skeleton.exception

import com.rex.batch.skeleton.enums.ErrorCode

/**
 * Job 실행 정보를 찾을 수 없을 때 발생하는 예외 — 404 `JOB_002`.
 */
class JobExecutionNotFoundException(
    jobExecutionId: Long,
    message: String = "Job execution not found: $jobExecutionId",
    cause: Throwable? = null,
) : CodedException(ErrorCode.JOB_EXECUTION_NOT_FOUND, message, "jobExecutionId: $jobExecutionId", cause)