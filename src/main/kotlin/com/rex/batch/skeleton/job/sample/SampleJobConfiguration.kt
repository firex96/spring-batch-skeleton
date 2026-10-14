package com.rex.batch.skeleton.job.sample

import com.rex.batch.skeleton.constant.Constant
import com.rex.batch.skeleton.dto.SampleDto
import com.rex.batch.skeleton.listener.ChunkTraceListener
import com.rex.batch.skeleton.listener.JobTraceListener
import com.rex.batch.skeleton.listener.StepTraceListener
import org.springframework.batch.core.job.Job
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.Step
import org.springframework.batch.core.step.builder.ChunkOrientedStepBuilder
import org.springframework.batch.infrastructure.item.ItemProcessor
import org.springframework.batch.infrastructure.item.ItemReader
import org.springframework.batch.infrastructure.item.ItemWriter
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager

/**
 * 샘플 배치 Job 설정
 *
 * Chunk 기반 배치 처리 예제.
 * ItemReader -> ItemProcessor -> ItemWriter 패턴.
 */
@Configuration
class SampleJobConfiguration(
    private val jobRepository: JobRepository,
    @param:Qualifier("mainTransactionManager")
    private val transactionManager: PlatformTransactionManager,
    private val jobTraceListener: JobTraceListener,
    private val stepTraceListener: StepTraceListener,
    private val chunkTraceListener: ChunkTraceListener,
    private val sampleItemReader: ItemReader<SampleDto>,
    private val sampleItemProcessor: ItemProcessor<SampleDto, SampleDto>,
    private val sampleItemWriter: ItemWriter<SampleDto>,
) {
    companion object {
        const val JOB_NAME = "sampleJob"
        const val STEP_NAME = "sampleStep"
    }

    @Bean
    fun sampleJob(): Job = JobBuilder(JOB_NAME, jobRepository)
        .listener(jobTraceListener)
        .start(sampleStep())
        .build()

    @Bean
    fun sampleStep(): Step = ChunkOrientedStepBuilder<SampleDto, SampleDto>(
        STEP_NAME,
        jobRepository,
        Constant.Batch.DEFAULT_CHUNK_SIZE,
    )
        .transactionManager(transactionManager)
        .reader(sampleItemReader)
        .processor(sampleItemProcessor)
        .writer(sampleItemWriter)
        .listener(stepTraceListener)
        .listener(chunkTraceListener)
        .faultTolerant()
        .skipLimit(Constant.Batch.DEFAULT_SKIP_LIMIT.toLong())
        .skip(Exception::class.java)
        .retryLimit(Constant.Batch.DEFAULT_RETRY_LIMIT.toLong())
        .retry(Exception::class.java)
        .build()
}