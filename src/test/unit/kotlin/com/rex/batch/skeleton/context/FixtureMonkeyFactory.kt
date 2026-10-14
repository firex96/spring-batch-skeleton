package com.rex.batch.skeleton.context

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.giveMeBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne

/**
 * FixtureMonkey 팩토리
 *
 * 테스트 객체 생성을 위한 유틸리티.
 */
object FixtureMonkeyFactory {
    val fixtureMonkey: FixtureMonkey =
        FixtureMonkey
            .builder()
            .plugin(KotlinPlugin())
            .build()

    /**
     * 지정된 타입의 테스트 객체 생성
     */
    inline fun <reified T : Any> create(): T = fixtureMonkey.giveMeOne()

    /**
     * 지정된 타입의 테스트 객체 리스트 생성
     */
    inline fun <reified T : Any> createList(size: Int = 3): List<T> = fixtureMonkey.giveMe(T::class.java, size)

    /**
     * 빌더를 통한 커스텀 객체 생성
     */
    inline fun <reified T : Any> builder() = fixtureMonkey.giveMeBuilder<T>()
}