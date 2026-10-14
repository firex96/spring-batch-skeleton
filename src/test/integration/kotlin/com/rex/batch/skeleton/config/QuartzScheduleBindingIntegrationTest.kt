package com.rex.batch.skeleton.config

import com.rex.batch.skeleton.config.properties.SampleJobScheduleProperties
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import org.quartz.CronTrigger
import org.quartz.JobDetail
import org.quartz.Trigger
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.context.annotation.Configuration

/**
 * 샘플 Job 스케줄이 설정(`app.batch.jobs.sample.*`)을 실제로 읽는지 확인한다.
 *
 * 과거에는 yml 에 cron 을 적어도 `QuartzConfiguration` 이 하드코딩한 값을 써서 설정이 조용히 무시됐다.
 * DB·Docker 없이 설정 클래스만 올린다.
 */
class QuartzScheduleBindingIntegrationTest : BehaviorSpec({
    val runner =
        ApplicationContextRunner()
            .withUserConfiguration(ScheduleProperties::class.java, QuartzConfiguration::class.java)

    Given("설정이 없으면") {
        Then("기본 cron(매일 02:00)으로 트리거를 등록한다") {
            runner.run { context ->
                (context.getBean("sampleJobTrigger", Trigger::class.java) as CronTrigger).cronExpression shouldBe "0 0 2 * * ?"
            }
        }
    }

    Given("app.batch.jobs.sample.cron 을 주면") {
        Then("그 cron 으로 트리거를 등록한다") {
            runner.withPropertyValues("app.batch.jobs.sample.cron=0 */5 * * * ?").run { context ->
                (context.getBean("sampleJobTrigger", Trigger::class.java) as CronTrigger).cronExpression shouldBe "0 */5 * * * ?"
            }
        }
    }

    Given("app.batch.jobs.sample.enabled=false 면") {
        Then("트리거는 등록하지 않고 JobDetail 은 남긴다") {
            runner.withPropertyValues("app.batch.jobs.sample.enabled=false").run { context ->
                context.getBeansOfType(Trigger::class.java).isEmpty() shouldBe true
                context.getBeansOfType(JobDetail::class.java).keys shouldBe setOf("sampleJobDetail")
            }
        }
    }
}) {
    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(SampleJobScheduleProperties::class)
    class ScheduleProperties
}