package com.rex.batch.skeleton.listener

import com.rex.batch.skeleton.constant.Constant
import com.rex.batch.skeleton.util.logger
import org.slf4j.MDC
import org.springframework.batch.core.ExitStatus
import org.springframework.batch.core.listener.StepExecutionListener
import org.springframework.batch.core.step.StepExecution
import org.springframework.stereotype.Component

/**
 * Step 실행 리스너
 *
 * Step 시작/종료 시 실행 정보 로깅.
 * 읽기/처리/쓰기 건수 및 스킵/롤백 건수 추적.
 */
@Component
class StepTraceListener : StepExecutionListener {
    private val log = logger()

    override fun beforeStep(stepExecution: StepExecution) {
        MDC.put(Constant.KEY_STEP_NAME, stepExecution.stepName)

        log.info(
            "Step 시작 - stepName={}, jobExecutionId={}",
            stepExecution.stepName,
            stepExecution.jobExecutionId,
        )
    }

    override fun afterStep(stepExecution: StepExecution): ExitStatus {
        log.info(
            "Step 종료 - stepName={}, status={}, " +
                "readCount={}, writeCount={}, filterCount={}, " +
                "skipCount={}, rollbackCount={}",
            stepExecution.stepName,
            stepExecution.status,
            stepExecution.readCount,
            stepExecution.writeCount,
            stepExecution.filterCount,
            stepExecution.skipCount,
            stepExecution.rollbackCount,
        )

        if (stepExecution.status.isUnsuccessful) {
            stepExecution.failureExceptions.forEach { ex: Throwable ->
                log.error("Step 실패 원인: {}", ex.message, ex)
            }
        }

        MDC.remove(Constant.KEY_STEP_NAME)

        return stepExecution.exitStatus ?: ExitStatus.COMPLETED
    }
}