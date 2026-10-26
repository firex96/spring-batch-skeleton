package com.rex.batch.skeleton.handler

import com.rex.batch.skeleton.enums.ErrorCode
import com.rex.batch.skeleton.exception.CodedException
import com.rex.batch.skeleton.exception.InvalidJobParameterException
import com.rex.batch.skeleton.model.ErrorResponseModel
import com.rex.batch.skeleton.util.logger
import org.springframework.batch.core.launch.JobInstanceAlreadyCompleteException
import org.springframework.batch.core.launch.JobRestartException
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.ErrorResponse
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

/**
 * 전역 예외 처리 핸들러.
 *
 * 응답 상태는 언제나 응답에 실은 [ErrorCode] 의 status 다. 응답 `message`·`details` 에는 사용자에게 보여도 되는 문장만 싣는다 —
 * 내부 메시지(Job 파라미터 값이 들어간 Spring Batch 메시지, 예외 메시지)는 [respond] 가 로그에만 남긴다.
 * 업무 예외는 [CodedException] 핸들러 하나가 받는다(Spring 이 더 구체적인 타입의 핸들러를 고른다). 전용 핸들러는 세 범주다 —
 * 응답에 예외 메시지를 내면 안 되는 업무 예외(`InvalidJobParameterException`), Spring Batch 예외, 상태만으로는 400 이 되지 않는
 * 요청 오류(타입 불일치·본문 형식 오류). 그 밖의 Spring 예외(`ErrorResponse` — 404·405·406·413·415·503 등)는 그 상태와
 * 그 상태의 코드([ErrorCode.fromStatus])로 응답한다.
 */
@RestControllerAdvice
class GlobalExceptionHandler {
    private val log = logger()

    /** 업무 예외 — 4xx 는 예외가 실은 `message`·`details`, 5xx 는 코드의 고정 문구만 싣는다. */
    @ExceptionHandler(CodedException::class)
    fun handleCodedException(ex: CodedException): ResponseEntity<ErrorResponseModel> =
        respond(ex.errorCode, ex.message ?: ex.errorCode.message, ex.details, ex)

    /**
     * 응답 message 에는 Job 이름이 든 예외 메시지 대신 코드의 고정 문구를 싣는다.
     * details 에는 거부된 키 **이름만** 담는다. 값은 담지 않는다.
     */
    @ExceptionHandler(InvalidJobParameterException::class)
    fun handleInvalidJobParameterException(ex: InvalidJobParameterException): ResponseEntity<ErrorResponseModel> =
        respond(ex.errorCode, details = ex.details, ex = ex)

    @ExceptionHandler(JobInstanceAlreadyCompleteException::class)
    fun handleJobInstanceAlreadyCompleteException(
        ex: JobInstanceAlreadyCompleteException,
    ): ResponseEntity<ErrorResponseModel> = respond(ErrorCode.JOB_ALREADY_RUNNING, "Job instance already complete", details = null, ex)

    @ExceptionHandler(JobRestartException::class)
    fun handleJobRestartException(ex: JobRestartException): ResponseEntity<ErrorResponseModel> =
        respond(ErrorCode.JOB_ALREADY_RUNNING, "Job cannot be restarted", details = null, ex)

    /**
     * 경로 변수·쿼리 값이 타입에 맞지 않음 — details 에 파라미터 **이름만** 싣는다(값은 싣지 않는다).
     * 상위 `TypeMismatchException` 으로 잡지 않는다 — 그 하위 `ConversionNotSupportedException`(변환기 부재, 서버 결함)은 캐치올 500 이다.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleMethodArgumentTypeMismatchException(ex: MethodArgumentTypeMismatchException): ResponseEntity<ErrorResponseModel> =
        respond(ErrorCode.INVALID_REQUEST, details = "parameter=${ex.name}", ex = ex)

    /** 본문 형식 오류 — 예외 메시지(타입·위치)는 로그에만 남긴다. */
    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadableException(ex: HttpMessageNotReadableException): ResponseEntity<ErrorResponseModel> =
        respond(ErrorCode.INVALID_REQUEST, details = null, ex = ex)

    /**
     * 전용 핸들러가 없는 Spring 예외(`ErrorResponse`)는 그 상태와 그 상태의 코드로 응답한다. 상태가 보존되면 `Allow`·`Accept` 같은
     * 예외 헤더도 싣는다(코드가 없는 상태로 대체되면 싣지 않는다). 그 밖의 예외는 500 이다.
     */
    @ExceptionHandler(Exception::class)
    fun handleException(ex: Exception): ResponseEntity<ErrorResponseModel> {
        if (ex is ErrorResponse) {
            val status = ex.statusCode.value()
            val errorCode = ErrorCode.fromStatus(status)
            val headers = if (errorCode.status.value() == status) ex.headers else HttpHeaders.EMPTY
            return respond(errorCode, details = null, ex = ex, headers = headers)
        }
        return respond(ErrorCode.INTERNAL_ERROR, details = null, ex = ex)
    }

    /**
     * 응답 상태를 [errorCode] 의 status 로 정하고 원래 예외를 로그에 남긴다 — 4xx 는 `warn`(메시지), 5xx 는 `error`(스택).
     * 5xx 는 넘어온 [message]·[details] 와 무관하게 코드의 고정 문구만 싣는다 — 어느 핸들러가 무엇을 넘겨도 내부 상세가 나가지 않는다.
     * 이 클래스에서 HTTP 상태·응답 문구를 확정하는 유일한 곳이다.
     */
    private fun respond(
        errorCode: ErrorCode,
        message: String = errorCode.message,
        details: String?,
        ex: Exception,
        headers: HttpHeaders = HttpHeaders.EMPTY,
    ): ResponseEntity<ErrorResponseModel> {
        val serverError = errorCode.status.is5xxServerError
        if (serverError) {
            log.error("{} {}", errorCode.code, ex.javaClass.simpleName, ex)
        } else {
            log.warn("{} {}: {}", errorCode.code, ex.javaClass.simpleName, ex.message)
        }
        val body =
            if (serverError) {
                ErrorResponseModel(code = errorCode.code, message = errorCode.message)
            } else {
                ErrorResponseModel(code = errorCode.code, message = message, details = details)
            }
        return ResponseEntity.status(errorCode.status).headers(headers).body(body)
    }
}