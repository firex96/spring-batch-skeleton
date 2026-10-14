package com.rex.batch.skeleton.mapper

import com.rex.batch.skeleton.config.MapstructConfiguration
import com.rex.batch.skeleton.dto.admin.DailyStatsDto
import com.rex.batch.skeleton.dto.admin.DashboardSummaryDto
import com.rex.batch.skeleton.dto.batch.JobErrorLogDto
import com.rex.batch.skeleton.dto.batch.JobScheduleConfigDto
import com.rex.batch.skeleton.dto.batch.WebhookTriggerHistoryDto
import com.rex.batch.skeleton.dto.webhook.JobExecutionDto
import com.rex.batch.skeleton.dto.webhook.StepExecutionDto
import com.rex.batch.skeleton.model.admin.DailyStatsModel
import com.rex.batch.skeleton.model.admin.DashboardSummaryModel
import com.rex.batch.skeleton.model.admin.JobErrorLogModel
import com.rex.batch.skeleton.model.admin.JobExecutionModel
import com.rex.batch.skeleton.model.admin.JobScheduleConfigModel
import com.rex.batch.skeleton.model.admin.StepExecutionModel
import com.rex.batch.skeleton.model.admin.WebhookTriggerHistoryModel
import org.mapstruct.Mapper
import org.mapstruct.Mapping

/**
 * 관리자 API 매퍼 — DTO ↔ Model. Entity ↔ DTO 는 각 `*JpaEntityService` 의 매퍼가 맡는다.
 */
@Mapper(config = MapstructConfiguration::class)
interface AdminMapper {
    fun toModel(dto: DashboardSummaryDto): DashboardSummaryModel

    fun toModel(dto: DailyStatsDto): DailyStatsModel

    fun toDailyStatsModelList(dtoList: List<DailyStatsDto>): List<DailyStatsModel>

    fun toJobExecutionModel(dto: JobExecutionDto): JobExecutionModel

    fun toJobExecutionModelList(dtoList: List<JobExecutionDto>): List<JobExecutionModel>

    fun toStepExecutionModel(dto: StepExecutionDto): StepExecutionModel

    fun toStepExecutionModelList(dtoList: List<StepExecutionDto>): List<StepExecutionModel>

    fun toModel(dto: JobErrorLogDto): JobErrorLogModel

    fun toJobErrorLogModelList(dtoList: List<JobErrorLogDto>): List<JobErrorLogModel>

    // is-prefix Boolean: getter isEnabled() → 소스 프로퍼티명 enabled, 생성자 파라미터 isEnabled — 명시하지 않으면 미매핑
    @Mapping(source = "enabled", target = "isEnabled")
    fun toModel(dto: JobScheduleConfigDto): JobScheduleConfigModel

    fun toJobScheduleConfigModelList(dtoList: List<JobScheduleConfigDto>): List<JobScheduleConfigModel>

    fun toModel(dto: WebhookTriggerHistoryDto): WebhookTriggerHistoryModel

    fun toWebhookTriggerHistoryModelList(dtoList: List<WebhookTriggerHistoryDto>): List<WebhookTriggerHistoryModel>
}