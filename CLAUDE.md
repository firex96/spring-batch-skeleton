# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build and Development Commands

### Build
```bash
# Build the project
./gradlew build

# Clean and build
./gradlew clean build

# Build without tests
./gradlew build -x test

# build 는 통합테스트를 돌리지 않는다 (onlyIf). Docker 는 integrationTest 를 직접 실행할 때만 필요
./gradlew build -x integrationTest
```

### Run
```bash
# 로컬 인프라(MySQL) — 스키마·계정·업무 테이블까지 만든다 (메타 스키마는 기동 시 Flyway)
docker compose up -d

# Run the Spring Boot application (SPRING_PROFILES_ACTIVE 가 없으면 local)
./gradlew bootRun

# 비밀값 출처 프로필로 실행 (aws · azure · gcp, 필요하면 ,loki)
SPRING_PROFILES_ACTIVE=aws AWS_SECRET_ID=… ./gradlew bootRun
```

### Code Quality
```bash
# Run ktlint checks
./gradlew ktlintCheck

# Auto-format code with ktlint
./gradlew ktlintFormat

# Run all checks (including tests and ktlint)
./gradlew check
```

### Testing

테스트는 2가지 유형으로 분리되어 있습니다:

| 유형 | 소스 경로 | 설명 | 속도 |
|------|----------|------|------|
| Unit Test | `src/test/unit/` | 순수 유닛 테스트 (MockK, Spring Context 없음) | 빠름 |
| Integration Test | `src/test/integration/` | 통합 테스트 (@SpringBootTest, Testcontainers) | 느림 |

```bash
# 유닛 테스트만 실행 (가장 빠름)
./gradlew test

# 통합 테스트만 실행 (Docker 필요)
./gradlew integrationTest

# 전체 테스트 실행
./gradlew check

# 특정 테스트 클래스 실행
./gradlew test --tests "com.rex.batch.skeleton.*TestClassName"
./gradlew integrationTest --tests "*IntegrationTest"

# 테스트 상세 출력
./gradlew test --info
```

**테스트 유형별 역할:**

1. **Unit Test** (`src/test/unit/`)
   - MockK으로 의존성 Mock, Spring Context 로드 없음
   - Processor, Service, Mapper 테스트
   - FixtureMonkey로 테스트 객체 생성
   - 예: `SampleItemProcessorTest`, `SampleBizServiceUnitTest`

2. **Integration Test** (`src/test/integration/`)
   - `@SpringBootTest`, Testcontainers 사용
   - 실제 DB 연동 테스트, Job 실행 테스트
   - MySQL 컨테이너 3개 자동 구동 (`skeleton_batch` · `skeleton_main` · `skeleton_sub` — 스키마마다 하나), AWS 프로필은 LocalStack
   - 예: `SampleJobIntegrationTest`, `SampleJpaEntityServiceIntegrationTest`

**테스트 설정 파일:**
- `config/KotestUnitProjectConfig.kt` · `config/KotestIntegrationProjectConfig.kt`: Kotest + Spring Extension 설정 (소스셋별)
- `config/IntegrationTestConfiguration.kt`: 통합 테스트용 빈 설정
- `config/TestcontainersConfiguration.kt`: Testcontainers MySQL(batch·main·sub) 설정
- 통합 테스트 프로필은 `test`(`application-test.yml`) — `integrationTest` 태스크의 기본 프로필이기도 하다
- `context/FixtureMonkeyFactory.kt`: 테스트 객체 생성 팩토리

## Architecture Overview

This is a Spring Batch skeleton project with a layered architecture designed for multi-datasource batch processing.

### Layer Structure

1. **Batch Job Layer** (`job/`)
   - Job, Step 정의
   - ItemReader, ItemProcessor, ItemWriter 구성
   - Chunk 기반 처리

2. **Service Layer** (`service/`)
   - **Business Services** (`service/biz/`): 비즈니스 로직, 트랜잭션 경계
   - Services return DTOs, not entities

3. **Persistence Layer** (`persistence/`)
   - **JPA** (`persistence/jpa/`): 엔티티, 리포지토리, EntityService
   - **JDBC** (`persistence/jdbc/`): 복잡한 쿼리, 대량 데이터 처리
   - Multi-datasource: batch (메타데이터·Quartz, @Primary) + main · sub (업무, 각각 Read/Write 분리)

4. **Scheduler Layer** (`quartz/`)
   - Quartz Job 정의
   - Spring Batch Job 실행 트리거

5. **Cross-Cutting Concerns**
   - **Listeners** (`listener/`): Job/Step/Chunk 실행 추적
   - **Configuration** (`config/`): 배치, 스케줄러, 데이터소스 설정
   - **Exception Handling** (`exception/`): 커스텀 예외

### Key Technical Decisions

1. **Multi-Datasource Architecture**
   - `batchDataSource` (Primary, `skeleton_batch`): Spring Batch 메타데이터 + Quartz JobStore + 커스텀 이력 테이블. Quartz 는 원본 풀 `batchWriteDataSource`(`@QuartzDataSource`)와 같은 풀에 묶인 `quartzTransactionManager`(`@QuartzTransactionManager`)를 쓴다 — 지우면 트리거 저장이 커밋되지 않는다
   - `mainDataSource` (`skeleton_main`): 업무 데이터 — 샘플 Job 의 `sample` 테이블
   - `subDataSource` (`skeleton_sub`): 업무 데이터 — `Product` 등 예제
   - 각 `{group}DataSource` 는 `{group}WriteDataSource`·`{group}ReadDataSource` 를 LazyConnectionDataSourceProxy 로 묶어 Read/Write 를 라우팅한다. 비 primary(main·sub) JDBC 는 `jdbcOperationsRef`·`dataAccessStrategyRef` 와 한정자를 명시한다

2. **Spring Batch 6.0 Package Changes**
   ```kotlin
   // Item 관련 클래스
   import org.springframework.batch.infrastructure.item.ItemProcessor
   import org.springframework.batch.infrastructure.item.ItemReader
   import org.springframework.batch.infrastructure.item.ItemWriter

   // Job/Step 관련 클래스
   import org.springframework.batch.core.job.Job
   import org.springframework.batch.core.job.JobExecution
   import org.springframework.batch.core.step.Step
   import org.springframework.batch.core.step.StepExecution

   // Listener 관련 클래스
   import org.springframework.batch.core.listener.JobExecutionListener
   import org.springframework.batch.core.listener.StepExecutionListener
   import org.springframework.batch.core.listener.ChunkListener

   // 설정 클래스
   import org.springframework.batch.core.configuration.support.JdbcDefaultBatchConfiguration
   ```

3. **Dual Persistence Strategy**: JPA for standard CRUD, JDBC Template for complex queries and bulk operations.

4. **MapStruct for Object Mapping**: Entity ↔ DTO 변환에 MapStruct 사용.

5. **Kotest for Testing**: Kotest BehaviorSpec + MockK + FixtureMonkey.

6. **Quartz JDBC JobStore**: 클러스터링 지원, 배치 스케줄 영구 저장.

7. **Jackson 3**: Spring Boot 4.0에서 `tools.jackson` 패키지 사용.

8. **API Key 인증 표면**
   - `ApiKeyAuthInterceptor` 가 **`/api` 와 `/admin/api` 하위 전체**를 검사한다(`WebMvcConfiguration`). 키가 없거나 틀리면 401
   - **`/admin` HTML 셸은 열려 있다** — 막으면 키를 입력할 화면 자체에 도달할 수 없다
   - Job 트리거는 인증에 더해 **인가**를 받는다 — `JobTriggerAuthorizer` 가 키별 `allowed-jobs` 와 파라미터 화이트리스트를 검사하고, 벗어나면 403
   - 관리자 대시보드는 키를 `sessionStorage` 에 두고 모든 요청에 싣는다. 브라우저 보관은 샘플의 수용 항목이다 — 운영에 쓰는 프로젝트는 대시보드를 자기 인증 뒤에 둔다(`AdminController` KDoc)
   - **이 대시보드는 샘플이다 — 운영에 쓰는 파생 프로젝트는 자기 인증 뒤에 둔다**(사내망 한정 노출·리버스 프록시 인증·프로젝트 세션). 스켈레톤이 고른 인증 방식은 모든 파생으로 복제되므로 여기에 운영급 인증을 넣지 않는다
   - 회귀 방지: `AdminApiReadAuthorizationIntegrationTest`(조회 인증) · `JobTriggerAuthorizationIntegrationTest`(트리거 인가)

### Naming Conventions

### 공통 명명 규칙 (모든 스켈레톤 프로젝트 공통)

> **정본은 [`README.md`](README.md) 「코딩 규약」 절이다.** 명명 표·클래스 접미사·1클래스1파일·트랜잭션·String Enum 이 전부 거기 있다.
>
> 이 절에 표를 다시 두지 않는다 — 사본이 둘이 되면 조용히 어긋난다. 실제로 그런 상태였고(README 쪽이 CLAUDE.md 의 부분집합이었다) 2026-08-31 에 정리했다.
>
> 세 스켈레톤의 그 절은 **글자 단위로 동일**해야 한다. 규약을 고치려면 **세 레포 README 를 함께** 고친다.

### Coding Guidelines

1. **1 Class per 1 File Principle**
   - One top-level `class`/`interface`/`object` (including enum, data, annotation) per file
   - Exceptions: Kotlin extension files (top-level functions/properties only) and `sealed` hierarchies (a sealed type and its direct subtypes); `typealias` and `fun main` are not counted
   - Group related files in sub-packages (e.g., `job/sample/`)
   - Detected by `OneClassPerFileConventionTest` (all three skeletons); file name = class name is enforced by ktlint `standard:filename`

2. **Model vs DTO vs Entity Usage**
   - **Model (`model/`)**: 외부 API 응답 모델
   - **DTO (`dto/`)**: 서비스 계층 데이터 전송
   - **Entity (`persistence/`)**: DB 매핑, 상위 계층 노출 금지
   - Conversion flow: Entity ↔ DTO ↔ Model (via MapStruct)
   - 선언 형태(Model·DTO·JdbcEntity = data class + val, JPA Entity = class + var)·Model 전 프로퍼티 `@field:JsonProperty`·nullable 은 DB 와 일치·`!!` 금지·전부-기본값 금지·MapStruct `unmappedTargetPolicy = ERROR`(non-null 타깃 매핑 명시, `is`-prefix `@Mapping`) — [.claude/rules/conventions.md](.claude/rules/conventions.md) 「타입 선언」. 메서드명 단수·복수 규칙은 같은 문서 「명명」

3. **Layered Service Pattern**
   - **Batch Job** → ItemProcessor에서 BizService 호출
   - **BizService** (`service/biz/`) → 비즈니스 로직, DTO 반환 — Repository 직접 주입 금지
   - **EntityService** (`persistence/.../service/`) → CRUD, DTO 반환 — Entity 는 여기까지
   - **관리자·웹훅 API** → `Controller → MappingService → BizService`(`AdminMappingService → JobAdminBizService`, `JobMappingService → JobLauncherBizService`). Controller 는 MappingService(와 `config/`·`util/` 의 요청 경계 보조 — `JobTriggerAuthorizer`)만 주입하고 응답은 `~Model`(허용 목록형 검출); MappingService 는 BizService·Mapper·Properties 만 주입한다. 검출 `ServiceNamingConventionTest`·`LayeringConventionTest`

4. **Transaction Management**
   - `@Transactional(readOnly = true)` 클래스 레벨 기본, 변경 메서드만 `@Transactional`(메서드가 클래스를 덮는다)
   - **모든 `@Transactional` 에 `transactionManager` 명시(primary `batchTransactionManager` 포함)** — 데이터소스가 여럿이라 묵시는 primary 로 간다
   - 경계는 BizService·EntityService — MappingService 에는 붙이지 않는다
   - Batch Job 의 스텝 트랜잭션은 `BatchConfiguration.getTransactionManager()`(`batchTransactionManager`)가 관리하고, ItemProcessor 안에서 부르는 서비스는 자기 데이터소스의 트랜잭션 매니저를 명시한다
   - 정본 `.claude/rules/conventions.md` 「데이터베이스」 3, 검출 `TransactionManagerConventionTest`
   - `@Async` 는 같은 프록시 규칙 — 같은 클래스 안 호출은 동기. 이 레포에는 `@Async` 가 없다(쓰게 되면 별도 빈으로, 검출 테스트는 public·internal 의 `AsyncSelfInvocationConventionTest` 를 복제)

5. **Exception Handling Pattern**
   - **업무 예외는 `CodedException` 을 상속하고, HTTP 상태와 오류 코드는 `ErrorCode` 하나가 정한다.** 새 오류는 `ErrorCode` 에 항목을 추가하고 `throw CodedException(ErrorCode.X)` 로 던진다. 응답 `details` 에는 요청에서 온 값(위조 가능)을 싣지 않는다
   - `GlobalExceptionHandler` 는 `CodedException` 하나로 받고 상태는 `errorCode.status` 로만 정한다. 전용 핸들러는 응답에 Job 이름이 든 예외 메시지 대신 코드 문구를 싣는 `InvalidJobParameterException`(거부 키 이름만 노출) 하나뿐이다. `ForbiddenException` 은 메시지 인자를 받지 않아 타입이 노출을 막는다(어떤 Job 이 있는지 숨김 — 거부 사유는 `JobTriggerAuthorizer` 가 로그로 남긴다)
   - **응답 문구는 사용자 문장이다** — `message` 는 업무 예외 4xx 면 예외 메시지, 5xx 업무 예외·캐치올이면 `errorCode.message`, Spring Batch 예외면 고정 문구다. `details` 에는 예외가 실은 값(경로 변수·거부 키 이름)만 싣고 Spring Batch·캐치올 예외의 메시지(Job 파라미터 값이 들어간다)는 싣지 않는다. 로그는 `respond` 한 곳 — 4xx `warn`, 5xx `error`(스택)
   - 실행 조회는 `getJobExecution` 을 직접 부르지 않고 `util/JobExplorerExtensions.kt` 의 `requireJobExecution` 을 쓴다 — Spring Batch 6 의 `JobExplorer.getJobExecution` 은 없는 id 에 `EmptyResultDataAccessException` 을 던져 500 이 되므로, 그 예외와 `null` 을 `JobExecutionNotFoundException`(404 `JOB_002`)으로 바꾼다. 웹훅·관리자 API 모두 같다
   - 예외: `InvalidApiKeyException`(401 AUTH_001) · `ForbiddenException`(403 AUTH_002) · `InvalidJobParameterException`(400 COMMON_001) · `JobNotFoundException`(404 JOB_001) · `JobExecutionNotFoundException`(404 JOB_002) · `ItemNotFoundException`(404 COMMON_002) · `BatchProcessException`(500 BATCH_001, Step 안에서 던진다)
   - 검출: `ErrorCodeContractTest`(모든 코드의 status 400~599·코드 유일) · `GlobalExceptionHandlerTest`(unit, 단일 핸들러·전용 핸들러 선택과 응답 문구 비노출) · `JobExplorerExtensionsTest` · `JobExecutionNotFoundIntegrationTest`(웹훅·관리자 404 `JOB_002`)
   - Listener에서 에러 로깅 자동 처리

6. **String Enum Pattern**
   - Enums implement `StringEnum` interface with `val value: String`
   - Use `AbstractStringEnumConverter` for JPA persistence
   - Use `StringEnumUtil.fromValue()` for conversion

7. **External API Client Pattern**
   - Define interface with `@HttpExchange` annotations
   - Configure via `ClientProperties` (timeouts, connection pool)
   - Use `WebClientUtil` for retry logic and error handling

8. **Distributed Tracing**
   - MDC에 traceId 저장 (JobTraceListener에서 설정)
   - stepName, jobName, jobExecutionId도 MDC에 저장
   - 모든 로그에 traceId 자동 포함

9. **JPA Auditing**
   - Entities extend `BaseAuditedEntity` for auto audit fields
   - `SimpleAuditorAware` reads the auditor from **`AuditorContext`**. **MDC is for logging only and no longer feeds auditing** — an empty audit subject writes a wrong value to the DB, so the channels are separate
   - **This repo has no authentication framework**, so the auditor cannot be derived from an authenticated principal. `HeaderAuditorContextFilter` copies `X-User-Id` into the context — **only when `app.audit.trust-user-header` is on (default `false`)**
   - Falls back to `SYSTEM_USER_ID` when it is off, when the header is absent/unparsable/out of `Int` range, or for non-request contexts
   - **Turning the flag on asserts a deployment contract this code does not enforce**: the service is reachable only through the gateway, and the gateway overwrites `X-User-Id` from its own authentication result so a caller cannot forge it. **Do not turn it on without that boundary.** Signing the header at the gateway would let the service verify it — that needs an infrastructure change, not an authentication framework
   - Async propagation is wired in `AsyncConfiguration` by composing `MdcTaskDecorator` with `AuditorContextTaskDecorator` (scheduled runs) — the batch itself is the actor
   - `AuditConfiguration` names the provider explicitly: `@EnableJpaAuditing(auditorAwareRef = "auditorProvider")`
   - `createdAt`, `updatedAt`, `createdBy`, `updatedBy` 자동 설정

10. **Chunk Listener Type Parameters**
    - Spring Batch 6.0에서 `ChunkListener<I, O>` 타입 파라미터 필요
    - Example: `class ChunkTraceListener : ChunkListener<Any, Any>`

### Environment Configuration

- **Profiles**: 비밀값 출처만 고른다 — `local`(docker compose 값) · `aws` · `azure` · `gcp`, 조합용 `loki`. 환경(dev·prod)별 프로필은 없다 — 환경 차이는 환경변수로 준다
- **Configuration Files**: `application.yml`(논리 키 참조) + `application-{local,aws,azure,gcp}.yml`
- **설정값·비밀값**: yml 은 kebab-case 논리 키만 참조, 평문 금지. 정본 [.claude/rules/conventions.md](.claude/rules/conventions.md) 「설정값·비밀값 규약」. 논리 키 목록은 README 「설정과 비밀값」·`.env.example`. 웹훅 API Key 는 `${webhook-api-key}`
- **로컬 개발 환경**: `docker compose up -d` 로 MySQL 을 띄우고 `local` 프로필로 붙는다. 업무 DDL 은 `db/manual/create-*.sql`(compose·Testcontainers 공용), 메타 스키마는 Flyway
- **Schedule**: Job 스케줄(cron·사용 여부)은 `app.batch.jobs.{job}.*` → `~JobScheduleProperties` 로 받는다. `QuartzConfiguration` 에 cron 을 박지 않는다
- **Logging**: `logback-spring.xml` 하나 — `<springProfile>` 분기. 콘솔(+파일), `loki` 프로필을 조합하면 Loki
- **Dependencies**: All library versions are defined directly in `build.gradle.kts`

### Batch Job Checklist

새 배치 Job 추가 시:

1. [ ] `job/{name}/` 디렉토리 생성
2. [ ] `{Name}JobConfiguration.kt` - Job, Step Bean 정의
3. [ ] `{Name}ItemReaderConfiguration.kt` - Reader Bean 정의 (JdbcPagingItemReader 등)
4. [ ] `{Name}ItemProcessor.kt` - 처리 로직 (`ItemProcessor<I, O>` 구현)
5. [ ] `{Name}ItemWriterConfiguration.kt` - Writer Bean 정의 (JdbcBatchItemWriter 등)
6. [ ] 리스너 등록 (기존 JobTraceListener, StepTraceListener, ChunkTraceListener 사용)
7. [ ] (선택) `quartz/job/{Name}QuartzJob.kt` 추가
8. [ ] (선택) `QuartzConfiguration.kt`에 JobDetail, Trigger Bean 추가 — cron·사용 여부는 `config/properties/{Name}JobScheduleProperties`(`app.batch.jobs.{name}.*`)로 받는다
9. [ ] 단위 테스트 작성 (Processor 중심)
10. [ ] 통합 테스트 작성 (Job 실행 검증)
11. [ ] Job 트리거는 Quartz·웹훅만 — `@Scheduled` 를 쓰지 않는다. Job·Step 재실행 멱등 ([.claude/rules/conventions.md](.claude/rules/conventions.md) 「레이어드 아키텍처」)

### Important Notes

- ktlint is strictly enforced - run `./gradlew ktlintFormat` before committing
- 통합 테스트는 Docker 필요 (Testcontainers MySQL + LocalStack)
- `datasource-proxy-spring-boot-starter` 2.0.0 활성화 — `local` 에서 쿼리 로그가 찍힌다 (1.12.1 이 Boot 4.0 미지원이었고 2.0.0 에서 해소됐다)
- `JobLauncherTestUtils`는 Spring Batch 6.0에서 deprecated
- Jackson 3 사용: `tools.jackson.module:jackson-module-kotlin`
- Kotlin 2.3.0의 `@Qualifier` 어노테이션 타겟 경고는 무시해도 됨

---

## 미결 항목 처리

- 지적을 "별도 티켓" 으로 미루려면 **티켓 식별자·담당·시점**을 함께 적는다. 셋 중 하나라도 없으면 그것은 티켓이 아니라 **미해결로 방치한 것**이므로, 지금 처리하거나 사용자에게 판단을 요청한다.
- "나중에 한다" 를 선택할 때는 **미루는 이득**을 한 줄로 적는다. 적을 수 없으면 미루지 않는다.
- 문서에 남긴 미결 항목은 다음 게이트에서 다시 확인한다.

### 테스트 소스셋에서 MapStruct 를 쓰려면

테스트 소스셋(`src/test/{unit,integration,e2e}`)에서 `@Mapper` 를 **직접 선언**하려면 그 소스셋에 kapt 설정을 추가한다.

```kotlin
val kaptIntegrationTest: Configuration by configurations.getting {
    extendsFrom(configurations.getByName("kapt"))
}
```

현재는 세 스켈레톤 어디에도 테스트 소스셋 `@Mapper` 가 없어 설정을 두지 않는다. 테스트가 쓰는 `*MapperImpl` 은 `src/main` 에서 생성돼 test 컴파일 클래스패스에 이미 올라온다.


## MySQL 스키마 및 계정

- 업무 스키마는 `skeleton_main` · `skeleton_sub` 다. 업무 테이블과 데이터는 기동 시 자동 생성/변경하지 않는다. DDL 정본은 `src/main/resources/db/manual/create-{main,sub}.sql` 이다(docker compose 초기화·Testcontainers 공용).
- `skeleton_batch` 는 Flyway(`db/migration/batch/V1__batch_meta_schema.sql`)가 유일한 정본이다 — 별도 참고 스키마 SQL 을 두지 않는다. 이력은 `flyway_schema_history_batch` 에 기록한다. `spring.flyway.enabled` 는 기본 `true` 다.
- Flyway 는 admin 계정(논리 키 `db-admin-{username,password}` → `app.flyway.mysql.*`)을 쓴다. 지정한 catalog 이외 URL 은 연결 전에 거부한다. 자동 스키마 생성, baseline, repair 는 사용하지 않는다.
- 런타임 read/write 는 `skeleton_reader`/`skeleton_writer` 를 쓴다. 논리 키는 `db-{reader,writer}-{username,password}` 다.
- 스키마·테이블의 문자셋·콜레이션은 `utf8mb4` / `utf8mb4_0900_ai_ci` 다. 이미 적용된 Flyway 파일은 고치지 않는다(체크섬) — 새 버전 파일로 바꾼다.

## 코드 정리·재검증 범위

- `/simplify` 는 구현자가 테스트 통과 후, 코드 리뷰(PR) 전에 1회 실행한다. 단일 파일 수준의 소규모 변경은 생략 가능. 실행 후 테스트를 재확인한다. 리뷰어는 읽기·판정 전용이므로 실행하지 않는다 — 가독성 문제는 Request Changes 로 돌려보낸다.
- 리뷰·검증에서 발견한 문제를 수정한 뒤의 재검증은 변경 범위에 맞춘다. 검증 리포트 또는 PR 본문에 재검증 범위와 근거(변경 파일 → 영향 경로)를 적는다. "처음부터 전체" 는 횡단 변경일 때만 한다.

| 수정의 변경 범위 | 재검증 범위 |
|---|---|
| 국소 — 단일 모듈 내부, 공용 코드 미변경 | 해당 모듈 테스트 재실행 + 변경 지점과 그것을 쓰는 경로 |
| 공통 — 공용 설정·베이스 클래스·공용 라이브러리 | 사용처를 Grep 으로 열거해 적고, 그 경로들을 재검증 |
| 횡단 — 규약 변경·인증/보안 계층 | `./gradlew check` 전체 재실행 |

## 에이전트 지침

에이전트 작업의 진입점은 [AGENTS.md](AGENTS.md)다. 프로젝트 규칙은 `CLAUDE.md`와 README 「코딩 규약」 원본을 공유한다. `.claude/rules/conventions.md`(front matter `paths`: `**/*.kt` · `**/build.gradle*` · `**/*.sql` · `**/*.yml`)가 있으며, `AGENTS.md`의 탐색 절차에 따라 그 경로를 만지는 작업이면 본문을 읽고 적용한다. 규칙 본문을 별도 사본으로 복제하지 않는다.
