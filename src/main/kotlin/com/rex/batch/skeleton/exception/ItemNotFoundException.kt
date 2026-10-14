package com.rex.batch.skeleton.exception

/**
 * 아이템을 찾을 수 없을 때 발생하는 예외
 */
class ItemNotFoundException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause) {
    constructor(entityName: String, id: Any) : this("$entityName not found with id: $id")
}