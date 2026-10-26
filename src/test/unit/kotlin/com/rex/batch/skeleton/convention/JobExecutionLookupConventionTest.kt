package com.rex.batch.skeleton.convention

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.ints.shouldBeGreaterThanOrEqual
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.extension
import kotlin.io.path.invariantSeparatorsPathString
import kotlin.io.path.readText

/**
 * 실행 조회 규약 검사 (README 「코딩 규약」 예외).
 *
 * Spring Batch 6 의 `JobExplorer.getJobExecution` 은 없는 id 에 `null` 대신 `EmptyResultDataAccessException` 을 던진다 — 그대로 부르면
 * 「없음」이 500 으로 나간다. 그래서 `src/main/kotlin` 에서 `JobExplorer` 의 `getJobExecution(` 호출은 [ALLOWED_FILE] 의
 * `requireJobExecution` 에서만 허용한다. BizService 자신의 `getJobExecution` 메서드와 구분하려고, 파일마다 `JobExplorer` 타입으로
 * 선언된 이름을 모은 뒤 그 이름의 호출만 찾는다. `JobExplorer` 확장 함수 안의 암묵 수신자 호출도 찾는다.
 * 한계: `with(jobExplorer) { getJobExecution(…) }` 형태는 잡지 않는다.
 */
class JobExecutionLookupConventionTest : BehaviorSpec({

    Given("src/main/kotlin 의 .kt 파일") {
        val sources = mainSources()
        val holders = sources.filter { (_, text) -> explorerNames(stripComments(text)).isNotEmpty() }
        println("JE-0 files=${sources.size} jobExplorerHolders=${holders.size} (min $MIN_MAIN/$MIN_HOLDERS)")

        Then("JE-0 스캔 파일 수와 JobExplorer 보유 파일 수가 최소 수 이상이다 (스캐너·경로 고장으로 공허 통과하지 않는다)") {
            sources.size shouldBeGreaterThanOrEqual MIN_MAIN
            holders.size shouldBeGreaterThanOrEqual MIN_HOLDERS
        }

        Then("JE-1 JobExplorer.getJobExecution 직접 호출은 requireJobExecution 밖에 없다") {
            val violations = sources.flatMap { (path, text) -> violations(path, text) }
            violations.forEach { println("JE-1 violation: $it") }
            violations.shouldBeEmpty()
        }
    }

    Given("검출 규칙 자체 (픽스처)") {
        Then("JE-2 JobExplorer 프로퍼티의 getJobExecution 호출은 위반이다") {
            val src = "class A(private val jobExplorer: JobExplorer) {\n    fun f() = jobExplorer.getJobExecution(1L)\n}\n"
            violations("service/A.kt", src) shouldHaveSize 1
        }

        Then("JE-3 BizService 의 getJobExecution 메서드 호출은 위반이 아니다") {
            val src = "class B(private val jobAdminBizService: JobAdminBizService) {\n    fun f() = jobAdminBizService.getJobExecution(1L)\n}\n"
            violations("service/B.kt", src).shouldBeEmpty()
        }

        Then("JE-4 JobExplorer 확장 함수의 암묵 수신자 호출은 허용 파일 밖에서 위반이다") {
            val src = "fun JobExplorer.x() = getJobExecution(1L)\n"
            violations("util/OtherExtensions.kt", src) shouldHaveSize 1
            violations(ALLOWED_FILE, src).shouldBeEmpty()
        }

        Then("JE-5 주석 안의 호출은 세지 않는다") {
            val src = "class C(private val jobExplorer: JobExplorer) {\n    // jobExplorer.getJobExecution(1L)\n    /* jobExplorer.getJobExecution(2L) */\n}\n"
            violations("service/C.kt", src).shouldBeEmpty()
        }
    }
}) {
    companion object {
        private const val MIN_MAIN = 150
        private const val MIN_HOLDERS = 2
        private const val ALLOWED_FILE = "util/JobExplorerExtensions.kt"
        private val MAIN_ROOT: Path = Paths.get("src/main/kotlin/com/rex/batch/skeleton")
        private val EXPLORER_NAME = Regex("""\b(\w+)\s*:\s*JobExplorer\b""")
        private val EXPLORER_EXTENSION = Regex("""\bfun\s+JobExplorer\.""")
        private val IMPLICIT_CALL = Regex("""(?<![.\w])getJobExecution\s*\(""")
        private val COMMENT = Regex("""//[^\n]*|/\*[\s\S]*?\*/""")

        /** 패키지 루트 기준 상대 경로 → 소스. */
        private fun mainSources(): List<Pair<String, String>> =
            Files.walk(MAIN_ROOT).use { stream ->
                stream
                    .filter { it.extension == "kt" }
                    .map { MAIN_ROOT.relativize(it).invariantSeparatorsPathString to it.readText() }
                    .toList()
            }

        private fun stripComments(text: String): String = COMMENT.replace(text) { " ".repeat(it.value.length) }

        /** [code] 는 주석을 지운 소스다. */
        private fun explorerNames(code: String): Set<String> = EXPLORER_NAME.findAll(code).map { it.groupValues[1] }.toSet()

        private fun violations(
            path: String,
            text: String,
        ): List<String> {
            if (path == ALLOWED_FILE) return emptyList()
            val code = stripComments(text)
            val named =
                explorerNames(code).flatMap { name ->
                    Regex("""\b${Regex.escape(name)}\s*\??\.\s*getJobExecution\s*\(""").findAll(code).map { "$path: $name.getJobExecution(" }
                }
            val implicit =
                if (EXPLORER_EXTENSION.containsMatchIn(code)) {
                    IMPLICIT_CALL.findAll(code).map { "$path: implicit getJobExecution(" }.toList()
                } else {
                    emptyList()
                }
            return named + implicit
        }
    }
}