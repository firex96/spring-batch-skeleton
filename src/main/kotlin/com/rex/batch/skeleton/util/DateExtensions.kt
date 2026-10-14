package com.rex.batch.skeleton.util

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 날짜 유틸리티 확장 함수
 */

/**
 * 날짜가 주어진 기간 내에 있는지 확인합니다.
 *
 * @param startDate 시작일
 * @param endDate 종료일
 * @return 기간 내에 있으면 true
 */
fun LocalDate.inPeriod(
    startDate: LocalDate,
    endDate: LocalDate,
): Boolean = this == startDate || this == endDate || (this.isAfter(startDate) && this.isBefore(endDate))

/**
 * LocalDateTime을 Job 파라미터 문자열로 변환합니다.
 *
 * @return yyyyMMddHHmmss 형식의 문자열
 */
fun LocalDateTime.toJobParameterString(): String = this.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))

/**
 * Job 파라미터 문자열을 LocalDateTime으로 변환합니다.
 *
 * @return LocalDateTime
 */
fun String.toLocalDateTime(): LocalDateTime = LocalDateTime.parse(this, DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))

/**
 * LocalDate를 Job 파라미터 문자열로 변환합니다.
 *
 * @return yyyyMMdd 형식의 문자열
 */
fun LocalDate.toJobParameterString(): String = this.format(DateTimeFormatter.ofPattern("yyyyMMdd"))

/**
 * Job 파라미터 문자열을 LocalDate로 변환합니다.
 *
 * @return LocalDate
 */
fun String.toLocalDate(): LocalDate = LocalDate.parse(this, DateTimeFormatter.ofPattern("yyyyMMdd"))