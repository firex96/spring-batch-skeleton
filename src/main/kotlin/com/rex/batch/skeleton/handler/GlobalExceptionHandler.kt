package com.rex.batch.skeleton.handler

import com.rex.batch.skeleton.enums.ErrorCode
import com.rex.batch.skeleton.exception.ForbiddenException
import com.rex.batch.skeleton.exception.InvalidApiKeyException
import com.rex.batch.skeleton.exception.InvalidJobParameterException
import com.rex.batch.skeleton.exception.JobExecutionNotFoundException
import com.rex.batch.skeleton.exception.JobNotFoundException
import com.rex.batch.skeleton.model.ErrorResponseModel
import com.rex.batch.skeleton.util.logger
import org.springframework.batch.core.launch.JobInstanceAlreadyCompleteException
import org.springframework.batch.core.launch.JobRestartException
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

/**
 * 전역 예외 처리 핸들러
 */
@RestControllerAdvice
class GlobalExceptionHandler {
    private val log = logger()

    @ExceptionHandler(InvalidApiKeyException::class)
    fun handleInvalidApiKeyException(ex: InvalidApiKeyException): ResponseEntity<ErrorResponseModel> {
        log.warn("API Key 인증 실패: {}", ex.message)

        return ResponseEntity
            .status(ex.errorCode.status)
            .body(
                ErrorResponseModel(
                    code = ex.errorCode.code,
                    message = ex.message ?: ex.errorCode.message,
                ),
            )
    }

    @ExceptionHandler(ForbiddenException::class)
    fun handleForbiddenException(ex: ForbiddenException): ResponseEntity<ErrorResponseModel> {
        // 상세 사유(어느 키가 어느 Job 을 시도했는지)는 인가자가 이미 구조화 WARN 으로 남겼다.
        // 응답에는 담지 않는다 — 어떤 Job 이 존재하는지 알려 주는 단서가 된다.
        log.warn("Job 트리거 인가 실패: {}", ex.message)

        return ResponseEntity
            .status(ex.errorCode.status)
            .body(
                ErrorResponseModel(
                    code = ex.errorCode.code,
                    message = ex.errorCode.message,
                ),
            )
    }

    @ExceptionHandler(InvalidJobParameterException::class)
    fun handleInvalidJobParameterException(ex: InvalidJobParameterException): ResponseEntity<ErrorResponseModel> {
        log.warn("허용되지 않은 Job 파라미터: jobName={}, rejectedKeys={}", ex.jobName, ex.rejectedKeys.sorted())

        return ResponseEntity
            .status(ex.errorCode.status)
            .body(
                ErrorResponseModel(
                    code = ex.errorCode.code,
                    message = ex.errorCode.message,
                    // 거부된 키 **이름만** 담는다. 값은 담지 않는다.
                    details = "rejectedKeys=${ex.rejectedKeys.sorted()}",
                ),
            )
    }

    @ExceptionHandler(JobNotFoundException::class)
    fun handleJobNotFoundException(ex: JobNotFoundException): ResponseEntity<ErrorResponseModel> {
        log.warn("Job을 찾을 수 없음: {}", ex.jobName)

        return ResponseEntity
            .status(ex.errorCode.status)
            .body(
                ErrorResponseModel(
                    code = ex.errorCode.code,
                    message = ex.message ?: ex.errorCode.message,
                    details = "jobName: ${ex.jobName}",
                ),
            )
    }

    @ExceptionHandler(JobExecutionNotFoundException::class)
    fun handleJobExecutionNotFoundException(ex: JobExecutionNotFoundException): ResponseEntity<ErrorResponseModel> {
        log.warn("Job 실행 정보를 찾을 수 없음: {}", ex.jobExecutionId)

        return ResponseEntity
            .status(ex.errorCode.status)
            .body(
                ErrorResponseModel(
                    code = ex.errorCode.code,
                    message = ex.message ?: ex.errorCode.message,
                    details = "jobExecutionId: ${ex.jobExecutionId}",
                ),
            )
    }

    @ExceptionHandler(JobInstanceAlreadyCompleteException::class)
    fun handleJobInstanceAlreadyCompleteException(
        ex: JobInstanceAlreadyCompleteException,
    ): ResponseEntity<ErrorResponseModel> {
        log.warn("Job이 이미 완료됨: {}", ex.message)

        return ResponseEntity
            .status(ErrorCode.JOB_ALREADY_RUNNING.status)
            .body(
                ErrorResponseModel(
                    code = ErrorCode.JOB_ALREADY_RUNNING.code,
                    message = "Job instance already complete",
                    details = ex.message,
                ),
            )
    }

    @ExceptionHandler(JobRestartException::class)
    fun handleJobRestartException(ex: JobRestartException): ResponseEntity<ErrorResponseModel> {
        log.warn("Job 재시작 불가: {}", ex.message)

        return ResponseEntity
            .status(ErrorCode.JOB_ALREADY_RUNNING.status)
            .body(
                ErrorResponseModel(
                    code = ErrorCode.JOB_ALREADY_RUNNING.code,
                    message = "Job cannot be restarted",
                    details = ex.message,
                ),
            )
    }

    @ExceptionHandler(Exception::class)
    fun handleException(ex: Exception): ResponseEntity<ErrorResponseModel> {
        log.error("예상치 못한 오류 발생", ex)

        return ResponseEntity
            .status(ErrorCode.INTERNAL_ERROR.status)
            .body(
                ErrorResponseModel(
                    code = ErrorCode.INTERNAL_ERROR.code,
                    message = ErrorCode.INTERNAL_ERROR.message,
                    details = ex.message,
                ),
            )
    }
}