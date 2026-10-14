package com.rex.batch.skeleton.config

import org.slf4j.MDC
import org.springframework.core.task.TaskDecorator

/**
 * MDC 컨텍스트를 비동기 스레드로 전파하는 TaskDecorator
 *
 * 비동기 스레드 풀에서 MDC 컨텍스트(traceId 등)를 유지합니다.
 */
class MdcTaskDecorator : TaskDecorator {
    override fun decorate(runnable: Runnable): Runnable {
        val copyOfContextMap = MDC.getCopyOfContextMap()
        return Runnable {
            if (copyOfContextMap != null) {
                MDC.setContextMap(copyOfContextMap)
            }
            try {
                runnable.run()
            } finally {
                MDC.clear()
            }
        }
    }
}