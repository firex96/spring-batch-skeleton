package com.rex.batch.skeleton.persistence

import org.quartz.Job
import org.quartz.JobExecutionContext

/** JDBC JobStore 저장/재기동 검증용 Job. */
class SchemaProbeQuartzJob : Job {
    override fun execute(context: JobExecutionContext) = Unit
}