package com.rex.batch.skeleton.handler

import com.rex.batch.skeleton.enums.ErrorCode
import com.rex.batch.skeleton.exception.CodedException
import com.rex.batch.skeleton.exception.ForbiddenException
import com.rex.batch.skeleton.exception.InvalidJobParameterException
import com.rex.batch.skeleton.model.ErrorResponseModel
import com.rex.batch.skeleton.util.logger
import org.springframework.batch.core.launch.JobInstanceAlreadyCompleteException
import org.springframework.batch.core.launch.JobRestartException
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

/**
 * 전역 예외 처리 핸들러.
 *
 * 응답 상태는 언제나 응답에 실은 [ErrorCode] 의 status 다. 업무 예외는 [CodedException] 핸들러 하나가 받고,
 * 응답에 예외 메시지를 내보내면 안 되는 예외만 전용 핸들러를 둔다(Spring 이 더 구체적인 타입의 핸들러를 고른다).
 */
@RestControllerAdvice
class GlobalExceptionHandler {
    private val log = logger()

    /** 업무 예외 — 상태·코드는 예외의 `errorCode`, 응답 `message`·`details` 는 예외가 실은 값이다. */
    @ExceptionHandler(CodedException::class)
    fun handleCodedException(ex: CodedException): ResponseEntity<ErrorResponseModel> {
        if (ex.errorCode.status.is5xxServerError) {
            log.error("{} {}", ex.errorCode.code, ex.javaClass.simpleName, ex)
        } else {
            log.warn("{} {}: {}", ex.errorCode.code, ex.javaClass.simpleName, ex.message)
        }

        return respond(ex.errorCode, ex.message ?: ex.errorCode.message, ex.details)
    }

    @ExceptionHandler(ForbiddenException::class)
    fun handleForbiddenException(ex: ForbiddenException): ResponseEntity<ErrorResponseModel> {
        // 상세 사유(어느 키가 어느 Job 을 시도했는지)는 인가자가 이미 구조화 WARN 으로 남겼다.
        // 응답에는 담지 않는다 — 어떤 Job 이 존재하는지 알려 주는 단서가 된다.
        log.warn("Job 트리거 인가 실패: {}", ex.message)

        return respond(ex.errorCode, ex.errorCode.message, details = null)
    }

    @ExceptionHandler(InvalidJobParameterException::class)
    fun handleInvalidJobParameterException(ex: InvalidJobParameterException): ResponseEntity<ErrorResponseModel> {
        log.warn("허용되지 않은 Job 파라미터: jobName={}, {}", ex.jobName, ex.details)

        // 응답 message 에는 Job 이름이 든 예외 메시지 대신 코드의 고정 문구를 싣는다.
        // details 에는 거부된 키 **이름만** 담는다. 값은 담지 않는다.
        return respond(ex.errorCode, ex.errorCode.message, ex.details)
    }

    @ExceptionHandler(JobInstanceAlreadyCompleteException::class)
    fun handleJobInstanceAlreadyCompleteException(
        ex: JobInstanceAlreadyCompleteException,
    ): ResponseEntity<ErrorResponseModel> {
        log.warn("Job이 이미 완료됨: {}", ex.message)

        return respond(ErrorCode.JOB_ALREADY_RUNNING, "Job instance already complete", ex.message)
    }

    @ExceptionHandler(JobRestartException::class)
    fun handleJobRestartException(ex: JobRestartException): ResponseEntity<ErrorResponseModel> {
        log.warn("Job 재시작 불가: {}", ex.message)

        return respond(ErrorCode.JOB_ALREADY_RUNNING, "Job cannot be restarted", ex.message)
    }

    @ExceptionHandler(Exception::class)
    fun handleException(ex: Exception): ResponseEntity<ErrorResponseModel> {
        log.error("예상치 못한 오류 발생", ex)

        return respond(ErrorCode.INTERNAL_ERROR, ErrorCode.INTERNAL_ERROR.message, ex.message)
    }

    /** 응답 상태를 [errorCode] 의 status 로 정한다 — 이 클래스에서 HTTP 상태를 정하는 유일한 곳이다. */
    private fun respond(
        errorCode: ErrorCode,
        message: String,
        details: String?,
    ): ResponseEntity<ErrorResponseModel> =
        ResponseEntity
            .status(errorCode.status)
            .body(ErrorResponseModel(code = errorCode.code, message = message, details = details))
}