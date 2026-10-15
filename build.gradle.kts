@file:Suppress("UnstableApiUsage")

plugins {
    val kotlinVersion = "2.3.21"

    kotlin("jvm") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion
    kotlin("plugin.jpa") version kotlinVersion
    kotlin("plugin.allopen") version kotlinVersion
    kotlin("kapt") version kotlinVersion

    idea

    id("org.springframework.boot") version "4.1.1"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.jlleitschuh.gradle.ktlint") version "13.0.0"
}

group = "com.rex"
version = "1.0.0-SNAPSHOT"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

ktlint {
    version.set("1.7.1")
}

// Version constants
val mapstructVersion = "1.6.3"

// 비밀값 출처 프로필(aws·azure·gcp)용 클라우드 BOM — 파생 프로젝트는 쓰지 않는 클라우드의 BOM·starter 를 지운다
val springCloudAwsVersion = "4.2.0"
val springCloudAzureVersion = "7.4.0"
val springCloudGcpVersion = "8.2.4"

dependencies {
    // 클라우드 BOM — 비밀값 출처 프로필용
    implementation(platform("io.awspring.cloud:spring-cloud-aws-dependencies:$springCloudAwsVersion"))
    implementation(platform("com.azure.spring:spring-cloud-azure-dependencies:$springCloudAzureVersion"))
    implementation(platform("com.google.cloud:spring-cloud-gcp-dependencies:$springCloudGcpVersion"))

    kapt("org.mapstruct:mapstruct-processor:$mapstructVersion")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    developmentOnly("org.springframework.boot:spring-boot-devtools")

    // Spring Boot Core
    // Note: AOP is now included in spring-boot-starter via spring-context in Spring Boot 4.0
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")

    // Thymeleaf (Admin Dashboard)
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("nz.net.ultraq.thymeleaf:thymeleaf-layout-dialect:3.4.0")

    // OpenAPI Documentation
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.14")

    // Spring Batch 6.0 with JDBC Job Repository
    implementation("org.springframework.boot:spring-boot-starter-batch")
    implementation("org.springframework.batch:spring-batch-core")
    implementation("org.springframework.batch:spring-batch-infrastructure")

    // Quartz Scheduler
    implementation("org.springframework.boot:spring-boot-starter-quartz")

    // JPA & JDBC
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")

    // WebFlux (for WebClient)
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    // Jackson 3 (Spring Boot 4.0)
    implementation("tools.jackson.module:jackson-module-kotlin")

    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")

    // MapStruct
    implementation("org.mapstruct:mapstruct:$mapstructVersion")

    // Jakarta
    implementation("jakarta.annotation:jakarta.annotation-api")
    implementation("jakarta.persistence:jakarta.persistence-api")

    implementation("com.github.gavlyukovskiy:datasource-proxy-spring-boot-starter:2.0.0")

    // Logging
    implementation("com.github.loki4j:loki-logback-appender:2.0.1")

    // Flyway
    implementation("org.springframework.boot:spring-boot-starter-flyway")
    implementation("org.flywaydb:flyway-mysql")

    // Database Drivers
    runtimeOnly("com.mysql:mysql-connector-j")

    // 비밀값 출처 — 기본은 꺼져 있고 aws·azure·gcp 프로필이 각자 켠다
    runtimeOnly("io.awspring.cloud:spring-cloud-aws-starter-secrets-manager")
    runtimeOnly("com.azure.spring:spring-cloud-azure-starter-keyvault")
    runtimeOnly("com.google.cloud:spring-cloud-gcp-starter-secretmanager")

    // Micrometer
    runtimeOnly("io.micrometer:micrometer-registry-prometheus")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    // batch 도 @RestController 를 노출한다(Job 트리거 웹훅·관리자 API). 형제 API 스켈레톤과
    // 달리 이 슬라이스 의존성이 없어 **컨트롤러 테스트를 쓸 수 없는 상태**였다.
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.batch:spring-batch-test")
    testImplementation("com.navercorp.fixturemonkey:fixture-monkey-starter-kotlin:1.1.15")
    testImplementation("com.ninja-squad:springmockk:5.0.1")
    testImplementation("io.kotest:kotest-runner-junit5:6.2.4")
    testImplementation("io.kotest:kotest-extensions-spring:6.2.4")
    testImplementation("io.mockk:mockk:1.14.11")
    testImplementation("io.github.classgraph:classgraph:4.8.186") // MapStruct 애노테이션(CLASS retention)을 클래스 파일에서 읽는 규약 검사용
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:testcontainers-junit-jupiter")
    testImplementation("org.testcontainers:testcontainers-mysql")
    testImplementation("org.testcontainers:testcontainers-localstack")
    testImplementation("software.amazon.awssdk:secretsmanager")
    testImplementation("com.github.nagyesta.lowkey-vault:lowkey-vault-testcontainers:7.3.127") // azure 프로필 검증용 Key Vault 에뮬레이터

    testRuntimeOnly("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

// Unit Test Source Set
sourceSets {
    test {
        kotlin.srcDir("src/test/unit/kotlin")
        resources.srcDir("src/test/unit/resources")
    }
}

// Integration Test Source Set
sourceSets.register("integrationTest") {
    compileClasspath += sourceSets.main.get().output + sourceSets.test.get().output
    runtimeClasspath += sourceSets.main.get().output + sourceSets.test.get().output
    kotlin.srcDir("src/test/integration/kotlin")
    resources.srcDir("src/test/integration/resources")
}

val integrationTestImplementation: Configuration by configurations.getting {
    extendsFrom(configurations.testImplementation.get())
}

val integrationTestRuntimeOnly: Configuration by configurations.getting {
    extendsFrom(configurations.testRuntimeOnly.get())
}

tasks.withType<Test> {
    useJUnitPlatform()

    jvmArgs("-XX:+EnableDynamicAgentLoading")
}

// Integration Test Resources - handle duplicates
tasks.named<Copy>("processIntegrationTestResources") {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

// Integration Test Task
// Kotest 6 은 AbstractProjectConfig 를 자동 탐색하지 않는다 — FQN 을 명시해야 한다.
// 소스셋마다 설정이 다르고 integrationTest 클래스패스가 test 출력을 포함하므로
// 클래스 이름을 나누고 태스크별로 가리킨다.
tasks.test {
    systemProperty(
        "kotest.framework.config.fqn",
        "com.rex.batch.skeleton.config.KotestUnitProjectConfig",
    )
}

val integrationTest by tasks.registering(Test::class) {
    description = "Runs integration tests"
    group = "verification"
    // @ActiveProfiles 를 빠뜨린 테스트도 기본 yml 의 논리 키가 해석되도록 test 를 기본 프로필로 둔다
    systemProperty("spring.profiles.active", "test")
    testClassesDirs = sourceSets["integrationTest"].output.classesDirs
    classpath = sourceSets["integrationTest"].runtimeClasspath
    useJUnitPlatform()
    jvmArgs("-XX:+EnableDynamicAgentLoading")
    shouldRunAfter(tasks.test)

    systemProperty(
        "kotest.framework.config.fqn",
        "com.rex.batch.skeleton.config.KotestIntegrationProjectConfig",
    )

    // build 태스크 실행 시에는 스킵 (형제 API 스켈레톤과 동일)
    // 같은 명령의 검증 범위가 레포마다 다르면 "build 가 통과했다" 의 의미가 달라진다.
    onlyIf { !gradle.startParameter.taskNames.any { it.contains("build") } }
}

// bootRun 은 SPRING_PROFILES_ACTIVE 가 없으면 local 로 뜬다 (docker compose up -d 선행)
tasks.named<org.springframework.boot.gradle.tasks.run.BootRun>("bootRun") {
    if (System.getenv("SPRING_PROFILES_ACTIVE").isNullOrBlank()) {
        systemProperty("spring.profiles.active", "local")
    }
}

tasks.check {
    dependsOn(integrationTest)
}