# Spring Batch Skeleton

**배치·스케줄 작업 프로젝트를 시작할 때 복사하는 템플릿이다.** Spring Batch + Quartz, 다중 데이터소스, 웹훅 Job 트리거, 관리자 대시보드, 감사 필드 자동 주입, 클라우드별 비밀값 출처 프로필이 이미 배선돼 있다.

이 문서는 **이 레포를 처음 복사한 사람**을 대상으로 한다. 위에서부터 읽고 「시작하기」를 따라 하면 새 프로젝트가 뜬다.

## 형제 스켈레톤

같은 규약을 공유하는 스켈레톤이 셋이다. **어느 것을 복사할지 먼저 고른다.**

| 스켈레톤 | 쓰는 곳 | 인증 |
|---|---|---|
| `spring-boot-public-api-skeleton` | 외부에 노출되는 API | Spring Security + JWT |
| `spring-boot-internal-api-skeleton` | 게이트웨이 뒤의 내부 API | 없음 — 망 경계에 의존 |
| **`spring-batch-skeleton`** (이 레포) | 배치·스케줄 작업 | 웹훅 API Key |

세 레포의 **「코딩 규약」 절은 글자 단위로 동일**하다. 규약을 고치려면 셋을 함께 고친다.

## 기술 스택

| 항목 | 버전 |
|---|---|
| Spring Boot | 4.1.1 |
| Kotlin | 2.3.21 |
| Java | 21 |
| Gradle | 9.3.0 |
| ktlint | 1.7.1 |
| DB | MySQL 8 |

**여기 적힌 숫자는 빌드 파일이 정본이다.** 어긋나면 `build.gradle.kts` · `gradle-wrapper.properties` 를 따른다. **Spring Batch · Quartz · Jackson 은 여기 적지 않는다** — 셋 다 Spring Boot BOM 이 관리해 빌드 파일에 버전이 고정돼 있지 않다. 확인하려면 `./gradlew dependencies` 를 쓴다.

## 시작하기

### 1. 사전 준비

```bash
sdk list java && sdk use java 21.0.2-tem     # JDK 21
./gradlew --version                           # Gradle 은 wrapper 를 쓴다 — 별도 설치 불필요
docker info                                   # 로컬 실행(docker compose)과 통합 테스트(Testcontainers)에 쓴다
```

### 2. 이름을 바꾼다 — 다섯 곳

복사 직후 **반드시** 고쳐야 하는 곳이다. 하나라도 빠뜨리면 스켈레톤 이름이 그대로 남는다.

| 고칠 것 | 좌표 | 현재 값 |
|---|---|---|
| 루트 프로젝트 이름 | `settings.gradle.kts:1` | `batch-skeleton` |
| group | `build.gradle.kts` `group` | `com.rex` |
| 애플리케이션 이름 | `src/main/resources/application.yml` `spring.application.name` | `batch-skeleton` |
| base package | `src/main/kotlin/com/rex/batch/skeleton/` | — |
| **Kotest 설정 FQN 2곳** | `build.gradle.kts` `kotest.framework.config.fqn` | 아래 |

> **Kotest FQN 을 잊지 않는다.** base package 를 바꾸면 아래 두 줄이 가리키던 클래스가 사라진다. **빌드 스크립트 안의 문자열이라 IDE 의 패키지 이름 변경이 따라가지 않는다.**
>
> ```kotlin
> "com.rex.batch.skeleton.config.KotestUnitProjectConfig"
> "com.rex.batch.skeleton.config.KotestIntegrationProjectConfig"
> ```

base package 를 바꿀 때는 문자열 안의 FQN 도 함께 고친다 — `META-INF/spring.factories`, MapStruct `expression = "java(…)"`, 규약 테스트의 `BASE` 상수.

### 3. 데이터소스를 고른다

스키마 **3개**를 각각 read/write 로 나눠 갖는다 — 업무 스키마 중 안 쓰는 것은 지운다(「다중 데이터소스」 참고).

| 스키마 | 설정 클래스 | 용도 |
|---|---|---|
| `skeleton_batch` | `BatchDataSourceConfiguration.kt` (@Primary) | **Spring Batch 메타데이터 + Quartz JobStore + 커스텀 이력 테이블** — Flyway |
| `skeleton_main` | `MainDataSourceConfiguration.kt` | 업무 데이터 — 샘플 Job 의 `sample` 테이블 |
| `skeleton_sub` | `SubDataSourceConfiguration.kt` | 업무 데이터 — `Product` 등 예제 |

각 스키마마다 `~JpaConfiguration` · `~JdbcConfiguration` 이 함께 있다. 스키마 이름은 `application.yml` 의 URL 과 `docker/mysql/init/01-schemas-users.sql` 을 함께 바꾼다.

### 4. 비밀값 출처를 고른다

애플리케이션은 비밀값을 **논리 키**(`${db-writer-password}` 같은 kebab-case 이름)로만 참조한다. 어디서 채울지는 프로필이 고른다 — 「설정과 비밀값」 참고. 쓰지 않는 클라우드의 의존성 줄과 프로필 yml 은 지운다.

### 5. 지워도 되는 샘플

| 지울 것 | 함께 지워야 하는 테스트 |
|---|---|
| `job/sample/` · `quartz/job/SampleQuartzJob.kt` · `config/properties/SampleJobScheduleProperties.kt` | 해당 유닛·통합 테스트 |
| `dto/SampleDto.kt` · `model/SampleModel.kt` · `mapper/SampleMapper.kt` · `service/biz/SampleBizService.kt` | 〃 |
| `persistence/{jpa,jdbc}/**/Sample*` | 〃 |
| `persistence/{jpa,jdbc}/**/sub/*`, `Product*` (sub 스키마 예제) | 〃 |
| `persistence/jpa/entity/main/SampleJpaEntity.kt` | **`AuditingIntegrationTest` · `AuditorChainIntegrationTest` · `AuditorHeaderDistrustIntegrationTest` · `support/AuditProbeConfiguration.kt`** |

**샘플만 지우면 빌드가 깨진다.** `SampleJpaEntity` 는 감사 계약을 검증하는 통합 테스트 셋이 저장 대상으로 쓴다. 지울 거라면 그 테스트들의 저장 대상을 **프로젝트의 실제 감사 엔티티로 바꿔라** — 통째로 지우면 감사 계약을 아무도 검증하지 않게 된다. 예제를 지우고 나면 `db/manual/create-*.sql` 의 테이블도 정리한다.

### 6. 뜨는지 확인한다

```bash
docker compose up -d                 # MySQL — 스키마·계정·업무 테이블까지 만든다 (메타 스키마는 기동 시 Flyway)
./gradlew ktlintFormat && ./gradlew build
./gradlew bootRun                    # SPRING_PROFILES_ACTIVE 가 없으면 local 로 뜬다
curl localhost:8080/actuator/health
open http://localhost:8080/admin     # 관리자 대시보드 — local 웹훅 키: local-webhook-api-key
```

`docker/mysql/init` 과 `db/manual/*.sql` 은 **볼륨을 처음 만들 때만** 실행된다. 바꾼 뒤에는 `docker compose down -v` 로 다시 만든다.

## 아키텍처

**계층을 건너뛰지 않는다.** API 스켈레톤과 달리 진입점이 `ItemProcessor` 다.

```
ItemProcessor       배치 아이템 처리. Reader 가 준 것을 받아 Writer 로 넘긴다
    │               위치: job/{jobName}/
    ▼
BizService          비즈니스 로직. 트랜잭션 경계. DTO 를 쓴다
    │               위치: service/biz/
    ▼
EntityService       단일 Entity CRUD. Entity ↔ DTO 변환
    │               위치: persistence/jpa/service/, persistence/jdbc/service/
    ▼
Repository          순수 데이터 접근만
                    위치: persistence/jpa/repository/, persistence/jdbc/repository/
```

관리자 대시보드와 웹훅에는 `Controller → MappingService → BizService → EntityService` 경로가 따로 있다(`AdminMappingService → JobAdminBizService`, `JobMappingService → JobLauncherBizService`). MappingService 는 BizService·Mapper 만 주입받고 응답은 `~Model` 이다.

### 감사 주체 — 이 레포에는 인증이 없다

`BaseAuditedEntity` 를 상속한 엔티티는 `created_by` · `updated_by` 가 자동으로 채워진다. **그 값이 어디서 오는지가 보안 계약이다.**

| 순위 | 출처 | 조건 |
|---|---|---|
| 1 | `X-User-Id` 헤더 | **`app.audit.trust-user-header` 가 켜진 경우에만** (기본 `false`) |
| 2 | `Constant.SYSTEM_USER_ID` | 그 외 전부 — **스케줄 기동은 항상 여기다** |

**`HeaderAuditorContextFilter` 라는 이름이 계약을 말한다** — 인증 결과가 아니라 헤더에서 온다. 웹훅 API Key 는 **키 이름**을 남길 뿐 사람을 식별하지 않는다. **MDC 는 로깅 전용이고 감사에 관여하지 않는다.**

> **플래그를 켜는 것은 이 코드가 강제하지 않는 배포 계약을 단언하는 행위다** — 게이트웨이를 통해서만 도달 가능하고, 게이트웨이가 자기 인증 결과로 `X-User-Id` 를 덮어써 호출자가 위조할 수 없어야 한다. **그 경계 없이 켜지 않는다.**

비동기 전파는 `AsyncConfiguration` 이 executor **둘 다**(`batchTaskExecutor` · `taskExecutor`) 배선한다 — 하나만 걸면 그 경로만 조용히 다르게 동작한다.

<!-- 아래 「코딩 규약」 절은 3레포 공통 정본이다. 한 레포만 고치지 않는다. -->

## 코딩 규약

**이 절이 세 스켈레톤 공통 규약의 정본이다.** `CLAUDE.md` 를 포함한 다른 문서는 여기를 가리킨다. 세 레포에서 이 절은 **글자 단위로 동일**해야 한다.

| 항목 | 규칙 | 올바른 예 | 잘못된 예 |
|---|---|---|---|
| Enum 클래스 | 접미사 없이 명명 | `ProductStatus`, `OrderType` | `ProductStatusEnum` |
| Configuration 클래스 | `Configuration` 접미사 | `SecurityConfiguration` | `SecurityConfig` |
| DataSource 파라미터 | 명시적 이름 | `mainDataSource`, `subDataSource` | `dataSource` |
| enum 패키지 | `enums/` | `...enums.SampleStatus` | `enum/` — Java 예약어라 MapStruct 빌드가 깨진다 |
| 역할 코드·Enum 값·상수 | 풀네임 | `ADMINISTRATOR`, `TEAM_LEADER` | `ADMIN`, `TL` |

클래스 접미사는 그 클래스가 **어느 계층에 속하는지**를 이름만으로 알 수 있게 한다.

| 접미사 | 계층 | 예 |
|---|---|---|
| `~Model` | Controller 입출력 | `SampleModel` |
| `~Dto` | Service 간 전송 | `SampleDto` |
| `~JpaEntity` · `~JdbcEntity` | DB 매핑 | `SampleJpaEntity` |
| `~JpaRepository` · `~JdbcRepository` | 데이터 접근 | `SampleJpaRepository` |
| `~JpaEntityService` · `~JdbcEntityService` | 단일 Entity CRUD | `SampleJpaEntityService` |
| `~BizService` | 비즈니스 로직 | `SampleBizService` |
| `~MappingService` | Controller 입출력 변환 · BizService 호출 (응답은 `~Model`) | `SampleMappingService` |
| `~Client` | 외부 API 어댑터 (`client/` 패키지, `@Component`) | `SampleApiClient` |
| `~Mapper` | MapStruct 변환 | `SampleMapper` |
| `~Configuration` | 설정 | `RedisConfiguration` |

**응답은 `~Model`, 요청 본문은 `~Dto` 를 허용한다.** Controller 는 MappingService 만 주입받고 Model 을 응답한다(허용 목록: MappingService 와 `util/`·`config/` 의 요청 경계 보조 컴포넌트 — `~Resolver`·`~Authorizer`·`~Properties` — 만; 그 외 BizService·EntityService·Repository·Client·임의 서비스는 전부 위반). MappingService 는 BizService · Mapper · Properties 만 주입받는다. `~EntityService` 는 Entity 를 위로 올리지 않는다 — DTO 를 돌려주거나 의도 메서드(`revoke…` · `markUsed…`)로 쓴다. 검출: `ServiceNamingConventionTest` · `LayeringConventionTest`.

**1 Class per 1 File.** 최상위 `class`·`interface`·`object`(enum·data·annotation 포함)는 파일에 하나만 둔다. 예외는 Kotlin extension(최상위 함수·프로퍼티만 있는 파일)과 `sealed` 계층(sealed 타입과 그 직접 하위 타입) 뿐이며 `typealias`·`fun main` 은 세지 않는다. 관련 파일은 하위 패키지로 묶는다. 검출: `OneClassPerFileConventionTest`(파일 이름 = 클래스 이름은 ktlint `standard:filename`).

**Model / DTO / Entity 를 섞지 않는다.** Entity 는 상위 계층에 노출하지 않는다. 변환은 MapStruct 가 한다 — 손으로 매핑 코드를 쓰지 않는다. 선언 형태(Model·DTO·JdbcEntity 는 data class + val, JPA Entity 는 class + var)·nullable·MapStruct 정책의 정본은 `.claude/rules/conventions.md` 「타입 선언」이다.

**트랜잭션은 클래스 레벨에 `@Transactional(readOnly = true)` 를 기본으로 두고, 변경 메서드에만 `@Transactional` 을 붙인다.** 메서드 레벨이 클래스 레벨을 덮는다.

**모든 `@Transactional` 은 `transactionManager` 를 명시한다(primary 포함)** — 데이터소스가 여럿이라 묵시는 primary 로 간다. 정본·근거·검출은 `.claude/rules/conventions.md` 「데이터베이스」 3.

**`@Async` 도 같은 프록시 규칙이다** — 같은 클래스 안 호출은 동기로 돈다. 비동기는 별도 빈으로 나눈다(`.claude/rules/conventions.md` 「데이터베이스」 3).

**문자열 값을 갖는 Enum 은 `StringEnum` 인터페이스를 구현한다.** JPA 영속화는 `AbstractStringEnumConverter`, 역변환은 `StringEnumUtil.fromValue()` 를 쓴다. `name()` 을 DB 값으로 쓰지 않는다 — 이름을 바꾸면 데이터가 깨진다.

**커스텀 예외는 `ExceptionCode` 를 구현하고 `GlobalExceptionHandler` 가 중앙에서 처리한다.** 예외를 삼키지 않는다.

**비밀값은 논리 키로만 참조한다.** yml 에는 `${db-writer-password}` 같은 kebab-case 논리 키만 적고 평문을 어디에도 적지 않는다. 값은 환경변수 또는 비밀값 출처 프로필(`aws`·`azure`·`gcp`)이 채운다 — 정본은 `.claude/rules/conventions.md` 「설정값·비밀값 규약」.

**ktlint 는 강제다.** 커밋 전에 `./gradlew ktlintFormat` 을 돌린다.

## 테스트

**이 레포는 소스셋이 둘이다** — API 스켈레톤과 달리 `e2e` 가 없다.

| 소스셋 | Gradle task | 의존 | 도구 |
|---|---|---|---|
| `src/test/unit` | `test` | 없음 — Spring Context 를 띄우지 않는다 | Kotest · MockK · FixtureMonkey |
| `src/test/integration` | `integrationTest` | **Docker** — Testcontainers MySQL(batch·main·sub)·LocalStack | `@SpringBootTest` |

```bash
./gradlew test                                    # 유닛만 — 가장 빠르다
./gradlew integrationTest                         # Docker 필요
./gradlew check                                   # 둘 다 (+ ktlint)

./gradlew test --tests "*SampleItemProcessorTest" # 특정 클래스만
```

> **「전체 테스트」는 `check` 다. `build` 가 아니다.**
>
> `integrationTest` 에 `onlyIf { !gradle.startParameter.taskNames.any { it.contains("build") } }` 가 걸려 있어 **`./gradlew build` 는 통합 테스트를 건너뛴다.** 빌드가 초록이어도 통합 테스트는 안 돈 것이다.

## 설정과 비밀값

### 프로필 — 비밀값을 어디서 읽을지 고른다

환경(dev·staging·prod)별 프로필은 없다. 환경 간 차이(호스트, 풀 크기, 로그 레벨, cron)는 배포할 때 환경변수로 준다. 프로필은 **비밀값 출처**만 고른다.

| 프로필 | 비밀값 출처 | 필요한 것 |
|---|---|---|
| (없음) | 환경변수 (컨테이너, k8s Secret) | 논리 키를 환경변수로 (`.env.example`) |
| `local` | `application-local.yml` 의 docker compose 값 | `docker compose up -d` |
| `aws` | AWS Secrets Manager — JSON 비밀 하나의 키 | `AWS_SECRET_ID`, AWS 기본 자격증명 체인 |
| `azure` | Azure Key Vault — 논리 키와 같은 이름의 비밀 | `AZURE_KEYVAULT_ENDPOINT`, DefaultAzureCredential |
| `gcp` | GCP Secret Manager — 논리 키와 같은 이름의 비밀 | `GCP_PROJECT_ID`, Application Default Credentials |
| `loki` (조합) | — | Loki 로그 전송. `LOKI_URL` (예: `SPRING_PROFILES_ACTIVE=aws,loki`) |

빠진 논리 키가 있으면 `UnresolvedPlaceholderGuard` 가 **기동을 막고 빠진 키를 나열한다** — 해석되지 않은 `${…}` 가 문자열 그대로 설정값이 되는 일을 막는다.

### 논리 키

| 논리 키 | 환경변수 | 비밀 | 설명 |
|---|---|---|---|
| `db-host` | `DB_HOST` | | MySQL 쓰기 호스트 |
| `db-port` | `DB_PORT` | | 기본 3306 |
| `db-read-host` | `DB_READ_HOST` | | 읽기 복제본 호스트 (기본 `db-host`) |
| `db-reader-username` · `db-reader-password` | `DB_READER_USERNAME` · `DB_READER_PASSWORD` | ✔ | 읽기 계정 |
| `db-writer-username` · `db-writer-password` | `DB_WRITER_USERNAME` · `DB_WRITER_PASSWORD` | ✔ | 쓰기 계정 (배치 메타·Quartz 포함) |
| `db-admin-username` · `db-admin-password` | `DB_ADMIN_USERNAME` · `DB_ADMIN_PASSWORD` | ✔ | Flyway 전용 DDL 계정 (`skeleton_batch`) |
| `webhook-api-key` | `WEBHOOK_API_KEY` | ✔ | Job 트리거 웹훅·관리자 API 의 `X-API-Key` |

Spring 은 환경변수 `DB_WRITER_PASSWORD` 를 논리 키 `db-writer-password` 로 자동 대응시킨다. 비밀 저장소에는 **논리 키 이름(kebab-case) 그대로** 비밀을 만든다 — Key Vault 이름 제약(영숫자·하이픈)을 만족하는 형식이다. 키를 추가하면 `.env.example` 과 `application-gcp.yml` 에도 추가한다(`SecretKeyCoverageTest` 가 누락을 잡는다).

### 클라우드별 비밀 등록 예

```bash
# AWS — JSON 비밀 하나에 키를 모은다
aws secretsmanager create-secret --name batch-skeleton/prod \
  --secret-string '{"db-writer-username":"app","db-writer-password":"…","db-admin-username":"admin","db-admin-password":"…","webhook-api-key":"…"}'
SPRING_PROFILES_ACTIVE=aws AWS_SECRET_ID=batch-skeleton/prod DB_HOST=… java -jar app.jar

# Azure — 비밀 이름 = 논리 키
az keyvault secret set --vault-name <vault> --name webhook-api-key --value '…'
SPRING_PROFILES_ACTIVE=azure AZURE_KEYVAULT_ENDPOINT=https://<vault>.vault.azure.net/ DB_HOST=… java -jar app.jar

# GCP — 비밀 이름 = 논리 키 (application-gcp.yml 이 키마다 ${sm@…} 로 대응시킨다)
printf '…' | gcloud secrets create webhook-api-key --data-file=-
SPRING_PROFILES_ACTIVE=gcp GCP_PROJECT_ID=<project> DB_HOST=… java -jar app.jar
```

### 쓰지 않는 클라우드 지우기

세 클라우드 starter 는 모두 `runtimeOnly` 로 들어 있고 기본 `application.yml` 에서 꺼져 있다. 하나만 쓴다면 나머지를 지운다.

| 클라우드 | `build.gradle.kts` | 리소스 |
|---|---|---|
| AWS | `spring-cloud-aws-dependencies`, `spring-cloud-aws-starter-secrets-manager`, `testcontainers-localstack`, `awssdk:secretsmanager` | `application-aws.yml`, `AwsSecretsManagerProfileIntegrationTest` |
| Azure | `spring-cloud-azure-dependencies`, `spring-cloud-azure-starter-keyvault`, `lowkey-vault-testcontainers` | `application-azure.yml`, `AzureKeyVaultProfileIntegrationTest` |
| GCP | `spring-cloud-gcp-dependencies`, `spring-cloud-gcp-starter-secretmanager` | `application-gcp.yml`, `SecretKeyCoverageTest` 의 GCP 검사 |

기본 `application.yml` 의 `spring.cloud.*` 비활성 블록에서도 해당 줄을 지운다.

| 항목 | 값 |
|---|---|
| Flyway | **`skeleton_batch` 만** 관리한다(`MysqlFlywayConfiguration`, 이력 `flyway_schema_history_batch`). admin 계정(`db-admin-*`)으로 붙는다 |
| 로깅 | `logback-spring.xml` 하나 — `local` 은 컬러 콘솔, 그 외는 콘솔 + 파일(`./logs`), `loki` 를 조합하면 Loki. 로그에 `traceId`·`jobName`·`stepName` 이 붙는다 |
| 쿼리 로깅 | `datasource-proxy` — `local` 에서 DEBUG 로 쿼리를 찍는다 |
| 의존성 | `build.gradle.kts` 에 직접 정의 — version catalog 를 쓰지 않는다 |

## 빌드·실행

```bash
./gradlew ktlintFormat        # 커밋 전 필수
./gradlew ktlintCheck         # 검사만

./gradlew build               # 빌드 + 유닛 테스트 (통합은 건너뛴다 — 위 참조)
./gradlew build -x test       # 테스트 없이
./gradlew clean build

docker compose up -d
./gradlew bootRun             # local 프로필
```

---

# 이 레포에만 있는 것

## 새 배치 Job 추가하기

1. `job/{jobName}/` 디렉터리를 만든다
2. `{JobName}JobConfiguration.kt` — Job · Step 정의
3. `{JobName}ItemReaderConfiguration.kt`
4. `{JobName}ItemProcessor.kt`
5. `{JobName}ItemWriterConfiguration.kt`
6. (선택) `quartz/job/{JobName}QuartzJob.kt`
7. (선택) `QuartzConfiguration.kt` 에 트리거 추가 — cron·사용 여부는 `config/properties/~JobScheduleProperties`(`app.batch.jobs.{jobName}.*`)로 받는다. 코드에 cron 을 박지 않는다

**클래스 이름이 곧 계약이다** — `~JobConfiguration` · `~ItemReaderConfiguration` · `~ItemProcessor` · `~ItemWriterConfiguration` · `~QuartzJob`.

## Quartz 스케줄러

JDBC JobStore 기반이라 **여러 인스턴스가 떠도 Job 이 한 번만 돈다**(클러스터링). JobStore 는 `skeleton_batch` 스키마를 쓴다.

샘플 Job 스케줄은 `app.batch.jobs.sample.cron`(기본 매일 02:00, `local` 은 5분마다)·`app.batch.jobs.sample.enabled` 로 정한다. `enabled=false` 면 트리거만 등록하지 않고 웹훅 수동 실행은 그대로다 — `QuartzScheduleBindingIntegrationTest`.

**Quartz 전용 트랜잭션 매니저(`quartzTransactionManager`)를 지우지 않는다.** Quartz 는 원본 write 풀(`@QuartzDataSource`)에서 커넥션을 받는데, 등록을 primary(`batchTransactionManager`, 프록시 데이터소스) 트랜잭션에서 돌리면 저장이 커밋되지 않아 **트리거가 조용히 등록되지 않는다.** 같은 풀에 묶인 `@QuartzTransactionManager` 가 그것을 막는다(`BatchDataSourceConfiguration`).

## 웹훅 Job 트리거와 관리자 대시보드

### 인증 — API Key

`ApiKeyAuthInterceptor` 가 **`/api` 와 `/admin/api` 하위 전체**를 검사한다(`config/webhook/WebMvcConfiguration.kt`). 키가 없거나 틀리면 401.

**`/admin` HTML 셸은 열려 있다** — 막으면 키를 입력할 화면 자체에 도달할 수 없다. 데이터는 `/admin/api` 뒤에 있다.

`JobTriggerAuthorizer` 가 **키별로 실행 가능한 Job 을 제한한다**(`allowedJobs`). 키 하나가 곧 전체 실행 권한이 아니다.

키 값은 논리 키(`${webhook-api-key}`)로만 yml 에 적는다 — 평문 금지. 호출 시스템이 여럿이면 `webhook.security.api-keys` 항목과 논리 키를 늘린다.

### 대시보드는 샘플이다

`templates/admin/` 의 6개 뷰(`dashboard` · `executions` · `execution-detail` · `schedules` · `webhooks` · `errors`)는 **예시**다.

대시보드는 API Key 를 브라우저 `sessionStorage` 에 두고 모든 요청에 싣는다. **스켈레톤은 여기에 운영급 인증을 넣지 않는다** — 관리자 콘솔의 인증 요건은 프로젝트마다 다르고, 스켈레톤이 고른 방식은 모든 파생으로 복제된다.

**운영에 쓰는 프로젝트는 자기 인증 뒤에 둔다** — 사내망 한정 노출, 리버스 프록시 인증, 또는 그 프로젝트의 세션. 그러면 키를 브라우저에 둘 필요도 없어진다.

## 분산 추적

`JobTraceListener` 가 MDC 에 `traceId` 를 넣고, `StepTraceListener` 가 `stepName` · `jobName` · `jobExecutionId` 를 얹는다. 모든 로그에 자동으로 붙는다.

`traceId` 는 `JobParameters` 로도 넘어가 `BATCH_JOB_EXECUTION_PARAMS` 에 남는다 — **로그와 배치 실행 이력을 이어 보기 위한 것**이며, 「MDC 는 로깅 전용」의 명시된 예외다.

## 알려진 제한

- `JobLauncherTestUtils` 가 Spring Batch 6 에서 deprecated
- 관리자 대시보드의 「스케줄」 화면은 표시용 커스텀 테이블 `JOB_SCHEDULE_CONFIG` 를 읽는다 — Quartz 트리거(`QRTZ_*`)와 자동으로 맞춰지지 않는다

## MySQL 스키마 및 계정

- 업무 스키마는 `skeleton_main` · `skeleton_sub` 다. 업무 테이블과 데이터는 기동 시 자동 생성/변경하지 않는다. DDL 정본은 `src/main/resources/db/manual/create-{main,sub}.sql` 이고 docker compose 초기화와 Testcontainers 가 같은 파일을 쓴다.
- `skeleton_batch` 의 배치 메타(`BATCH_*`)·Quartz(`QRTZ_*`)·커스텀 이력(`WEBHOOK_TRIGGER_HISTORY`·`JOB_ERROR_LOG` 등) 테이블은 Flyway `db/migration/batch/V1__batch_meta_schema.sql` 이 유일한 정본이다. 이력은 `flyway_schema_history_batch` 에 기록한다.
- Flyway 는 admin 계정(`db-admin-*`)을 쓴다. 지정한 catalog(`skeleton_batch`) 이외 URL 은 연결 전에 거부한다. 자동 스키마 생성, baseline, repair 는 사용하지 않는다.
- 런타임 read/write 는 `skeleton_reader`/`skeleton_writer` 를 쓴다.
- 스키마·테이블의 문자셋·콜레이션은 `utf8mb4` / `utf8mb4_0900_ai_ci` 다 — DDL 의 `COLLATE` 명시값과 스키마 기본이 같다. Flyway 마이그레이션 파일을 고치면 체크섬이 바뀐다 — 이미 적용된 DB 는 새 버전 파일(`V2__…`)로 고친다.

| 계정 | 권한 | 쓰는 곳 |
|---|---|---|
| `skeleton_reader` | 세 스키마 `SELECT` | read 풀 |
| `skeleton_writer` | 세 스키마 `SELECT, INSERT, UPDATE, DELETE` | write 풀 · Quartz JobStore |
| `skeleton_admin` | `skeleton_batch` DDL + DML | Flyway |

## 관련 문서

- [`CLAUDE.md`](CLAUDE.md) — Claude Code 작업 지침
- [`AGENTS.md`](AGENTS.md) — 에이전트 작업 진입점
- [Spring Boot 4 Release Notes](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Release-Notes)
- [Spring Batch 6.0 Migration Guide](https://github.com/spring-projects/spring-batch/wiki/Spring-Batch-6.0-Migration-Guide)

## 에이전트 지침

에이전트 작업의 진입점은 [AGENTS.md](AGENTS.md)다. 프로젝트 규칙은 `CLAUDE.md`와 README 「코딩 규약」 원본을 공유한다. `.claude/rules/**/*.md`가 있으면 `AGENTS.md`의 탐색 절차에 따라 직접 읽으며, `paths`가 없는 규칙은 항상 적용하고 있는 규칙은 작업 경로에 맞춰 적용한다. 규칙 본문을 별도 사본으로 복제하지 않는다.
