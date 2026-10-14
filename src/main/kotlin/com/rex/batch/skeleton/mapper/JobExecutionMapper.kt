package com.rex.batch.skeleton.mapper

import com.rex.batch.skeleton.config.MapstructConfiguration
import com.rex.batch.skeleton.dto.webhook.JobExecutionDto
import com.rex.batch.skeleton.dto.webhook.StepExecutionDto
import com.rex.batch.skeleton.model.webhook.JobStatusResponseModel
import com.rex.batch.skeleton.model.webhook.StepStatusModel
import org.mapstruct.Mapper
import org.mapstruct.Mapping

/**
 * Job 실행 정보 매퍼
 *
 * DTO ↔ Model 변환을 담당합니다.
 */
@Mapper(config = MapstructConfiguration::class)
interface JobExecutionMapper {
    @Mapping(source = "stepExecutions", target = "steps")
    fun toModel(dto: JobExecutionDto): JobStatusResponseModel

    fun toModel(dto: StepExecutionDto): StepStatusModel

    fun toModelList(dtoList: List<StepExecutionDto>): List<StepStatusModel>
}