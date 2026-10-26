package com.rex.batch.skeleton.exception

import com.rex.batch.skeleton.enums.ErrorCode

/**
 * 아이템을 찾을 수 없을 때 발생하는 예외 — 404 `COMMON_002`.
 */
class ItemNotFoundException(
    message: String,
    cause: Throwable? = null,
) : CodedException(ErrorCode.ITEM_NOT_FOUND, message, cause = cause) {
    constructor(entityName: String, id: Any) : this("$entityName not found with id: $id")
}