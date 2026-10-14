package com.rex.batch.skeleton.job.sample

import com.rex.batch.skeleton.dto.SampleDto
import com.rex.batch.skeleton.util.logger
import org.springframework.batch.infrastructure.item.ItemProcessor
import org.springframework.stereotype.Component
import java.time.LocalDateTime

/**
 * 샘플 ItemProcessor
 *
 * 읽어온 데이터에 대한 비즈니스 로직 처리.
 * null 반환 시 해당 아이템은 Writer로 전달되지 않음.
 */
@Component
class SampleItemProcessor : ItemProcessor<SampleDto, SampleDto> {
    private val log = logger()

    override fun process(item: SampleDto): SampleDto? {
        log.debug("Processing item: id={}", item.id)

        // 필터링 조건: 특정 조건에 맞지 않으면 null 반환하여 스킵
        if (!isValid(item)) {
            log.debug("Item filtered out: id={}", item.id)
            return null
        }

        // 데이터 변환/가공
        return item.copy(
            status = "COMPLETED",
            processedAt = LocalDateTime.now(),
        )
    }

    private fun isValid(item: SampleDto): Boolean = item.name.isNotBlank()
}