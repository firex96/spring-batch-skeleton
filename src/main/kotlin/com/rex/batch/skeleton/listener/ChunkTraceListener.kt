package com.rex.batch.skeleton.listener

import com.rex.batch.skeleton.util.logger
import org.springframework.batch.core.listener.ChunkListener
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.stereotype.Component

/**
 * Chunk 처리 리스너
 *
 * Chunk 단위 처리 시작/완료/에러 로깅.
 */
@Component
class ChunkTraceListener : ChunkListener<Any, Any> {
    private val log = logger()

    override fun beforeChunk(context: ChunkContext) {
        log.debug(
            "Chunk 처리 시작 - stepName={}, readCount={}",
            context.stepContext.stepName,
            context.stepContext.stepExecution.readCount,
        )
    }

    override fun afterChunk(context: ChunkContext) {
        log.debug(
            "Chunk 처리 완료 - stepName={}, writeCount={}",
            context.stepContext.stepName,
            context.stepContext.stepExecution.writeCount,
        )
    }

    override fun afterChunkError(context: ChunkContext) {
        log.warn(
            "Chunk 처리 에러 - stepName={}, rollbackCount={}",
            context.stepContext.stepName,
            context.stepContext.stepExecution.rollbackCount,
        )
    }
}