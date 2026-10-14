package com.rex.batch.skeleton.util

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * SLF4J Logger 생성 확장 함수
 *
 * 사용 예시:
 * ```kotlin
 * class MyClass {
 *     private val log = logger()
 * }
 * ```
 */
inline fun <reified T> T.logger(): Logger = LoggerFactory.getLogger(T::class.java)