package com.rex.batch.skeleton.persistence

import com.rex.batch.skeleton.config.IntegrationTestConfiguration
import com.rex.batch.skeleton.config.TestcontainersConfiguration
import com.rex.batch.skeleton.dto.SampleDto
import com.rex.batch.skeleton.persistence.jdbc.service.main.SampleJdbcEntityService
import com.rex.batch.skeleton.service.biz.SampleBizService
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.springframework.batch.infrastructure.item.Chunk
import org.springframework.batch.infrastructure.item.database.JdbcBatchItemWriter
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate

@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(initializers = [TestcontainersConfiguration.Initializer::class])
@Import(IntegrationTestConfiguration::class)
@org.springframework.boot.autoconfigure.EnableAutoConfiguration(
    exclude = [org.springframework.boot.quartz.autoconfigure.QuartzAutoConfiguration::class],
)
class SampleDatabaseRoutingIntegrationTest(
    private val business: SampleBizService,
    private val jdbc: SampleJdbcEntityService,
    private val writer: JdbcBatchItemWriter<SampleDto>,
    @Qualifier("mainTransactionManager") private val transactions: PlatformTransactionManager,
) : StringSpec({
    "JPA로 만든 업무 행을 JDBC가 조회하고 수정한다" {
        val row = business.add(SampleDto(name = "routing-crud"))
        try {
            jdbc.get(row.id)?.name shouldBe "routing-crud"
            jdbc.updateStatusToCompleted(listOf(row.id)) shouldBe 1
            business.find(row.id)?.status shouldBe "COMPLETED"
        } finally {
            business.remove(row.id)
        }
    }

    "업무 트랜잭션 롤백은 배치 Writer 변경도 취소한다" {
        val row = business.add(SampleDto(name = "routing-rollback"))
        try {
            TransactionTemplate(transactions).executeWithoutResult { status ->
                writer.write(Chunk(listOf(row.copy(status = "COMPLETED"))))
                status.setRollbackOnly()
            }
            business.find(row.id)?.status shouldBe "PENDING"
        } finally {
            business.remove(row.id)
        }
    }
})