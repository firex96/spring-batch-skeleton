package com.rex.batch.skeleton.exception

import com.rex.batch.skeleton.enums.ErrorCode

/**
 * Job을 찾을 수 없을 때 발생하는 예외 — 404 `JOB_001`.
 */
class JobNotFoundException(
    jobName: String,
    message: String = "Job not found: $jobName",
    cause: Throwable? = null,
) : CodedException(ErrorCode.JOB_NOT_FOUND, message, "jobName: $jobName", cause)