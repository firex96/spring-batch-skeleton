package com.rex.batch.skeleton.util

import com.rex.batch.skeleton.exception.JobExecutionNotFoundException
import org.springframework.batch.core.job.JobExecution
import org.springframework.batch.core.repository.explore.JobExplorer
import org.springframework.dao.EmptyResultDataAccessException

/**
 * 실행 정보를 가져온다. 없는 id 면 [JobExecutionNotFoundException](404 `JOB_002`)을 던진다.
 *
 * Spring Batch 6 의 `JobExplorer.getJobExecution` 은 없는 id 에 `null` 대신 `EmptyResultDataAccessException` 을 던진다.
 * 그대로 두면 「없음」이 500 으로 나가므로 그 예외와 `null` 을 「없음」으로 다룬다. 다른 데이터 접근 예외는 그대로 전파한다.
 * 실행 조회는 `getJobExecution` 을 직접 부르지 않고 이 함수를 쓴다.
 */
fun JobExplorer.requireJobExecution(jobExecutionId: Long): JobExecution =
    try {
        getJobExecution(jobExecutionId)
    } catch (_: EmptyResultDataAccessException) {
        null
    } ?: throw JobExecutionNotFoundException(jobExecutionId)