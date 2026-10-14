package com.rex.batch.skeleton.exception

/**
 * 배치 처리 중 발생하는 예외
 */
class BatchProcessException(
    val errorCode: String,
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause) {
    constructor(message: String) : this("BATCH_ERROR", message)

    constructor(message: String, cause: Throwable) : this("BATCH_ERROR", message, cause)
}