package com.rex.batch.skeleton.handler

import com.rex.batch.skeleton.exception.BatchProcessException
import com.rex.batch.skeleton.exception.ForbiddenException
import com.rex.batch.skeleton.exception.InvalidApiKeyException
import com.rex.batch.skeleton.exception.InvalidJobParameterException
import com.rex.batch.skeleton.exception.ItemNotFoundException
import com.rex.batch.skeleton.exception.JobExecutionNotFoundException
import com.rex.batch.skeleton.exception.JobNotFoundException
import org.springframework.batch.core.launch.JobInstanceAlreadyCompleteException
import org.springframework.batch.core.launch.JobRestartException
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/** `GlobalExceptionHandlerTest` 가 쓰는 예외 발생용 컨트롤러 — 업무 예외를 하나씩 던진다. */
@RestController
@RequestMapping("/probe")
internal class ExceptionProbeController {
    @GetMapping("/item-not-found")
    fun itemNotFound(): String = throw ItemNotFoundException("Product", 1L)

    @GetMapping("/batch-process")
    fun batchProcess(): String = throw BatchProcessException("chunk write failed")

    @GetMapping("/job-not-found")
    fun jobNotFound(): String = throw JobNotFoundException("sampleJob")

    @GetMapping("/job-execution-not-found")
    fun jobExecutionNotFound(): String = throw JobExecutionNotFoundException(7L)

    @GetMapping("/invalid-api-key")
    fun invalidApiKey(): String = throw InvalidApiKeyException("Invalid API Key")

    @GetMapping("/forbidden")
    fun forbidden(): String = throw ForbiddenException()

    @GetMapping("/invalid-job-parameter")
    fun invalidJobParameter(): String = throw InvalidJobParameterException("sampleJob", setOf("b", "a"))

    @GetMapping("/job-instance-complete")
    fun jobInstanceComplete(): String = throw JobInstanceAlreadyCompleteException("identifying parameters={secret=1}")

    @GetMapping("/job-restart")
    fun jobRestart(): String = throw JobRestartException("identifying parameters={secret=1}")

    @GetMapping("/unexpected")
    fun unexpected(): String = throw IllegalStateException("internal secret detail")
}