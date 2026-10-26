package com.rex.batch.skeleton.util

import com.rex.batch.skeleton.exception.JobExecutionNotFoundException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.springframework.batch.core.job.JobExecution
import org.springframework.batch.core.repository.explore.JobExplorer
import org.springframework.dao.DataAccessResourceFailureException
import org.springframework.dao.EmptyResultDataAccessException

class JobExplorerExtensionsTest : DescribeSpec({

    val jobExplorer = mockk<JobExplorer>()

    describe("requireJobExecution") {

        it("있으면 그 실행을 돌려준다") {
            val execution = mockk<JobExecution>()
            every { jobExplorer.getJobExecution(1L) } returns execution

            jobExplorer.requireJobExecution(1L) shouldBe execution
        }

        it("없는 id 에 EmptyResultDataAccessException 이 나면 JobExecutionNotFoundException 이다") {
            every { jobExplorer.getJobExecution(2L) } throws EmptyResultDataAccessException(1)

            shouldThrow<JobExecutionNotFoundException> { jobExplorer.requireJobExecution(2L) }.details shouldBe "jobExecutionId: 2"
        }

        it("null 이 와도 JobExecutionNotFoundException 이다") {
            every { jobExplorer.getJobExecution(4L) } returns null

            shouldThrow<JobExecutionNotFoundException> { jobExplorer.requireJobExecution(4L) }
        }

        it("다른 데이터 접근 예외는 그대로 전파한다") {
            every { jobExplorer.getJobExecution(3L) } throws DataAccessResourceFailureException("db down")

            shouldThrow<DataAccessResourceFailureException> { jobExplorer.requireJobExecution(3L) }
        }
    }
})