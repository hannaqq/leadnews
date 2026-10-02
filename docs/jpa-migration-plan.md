# Spring Data JPA / Hibernate migration plan

## Progress

- Phase 1: complete.
- Phase 2: complete; Snowflake generation and all four mapped entities are covered by Hibernate integration tests.
- Phase 3: complete; Admin, Behavior, User, Article, Wemedia, Schedule, and ES Init now use Spring Data JPA/Hibernate.

## Goal and constraints

Migrate every MySQL persistence path from MyBatis/MyBatis-Plus to Spring Data JPA and Hibernate, then remove all Mapper interfaces, Mapper XML, wrappers, interceptors, and MyBatis dependencies. Existing HTTP/Feign APIs, database schema, Kafka topics, Redis keys, and task-state behavior remain unchanged. Queries use repository methods or JPQL; native queries, JdbcTemplate, and QueryDSL are out of scope.

## Phase 1 — Dependencies and coexistence

- Add `spring-boot-starter-data-jpa` and the runtime MySQL driver directly to Admin, Behavior, User, Article, Wemedia, Schedule, and ES Init. Do not add them to the shared service parent or Search.
- Add provided-scope `jakarta.persistence-api` and `hibernate-core` to Model. Keep only the lightweight MyBatis annotation API as a temporary compile dependency while legacy entity annotations remain. Move the MySQL driver and MyBatis starter immediately into the applications that use them so Model does not leak persistence runtimes into Search or the gateways.
- Configure `ddl-auto: none` and disable Open Session in View in each MySQL application.
- Keep MyBatis-Plus temporarily so migrated and non-migrated services can coexist.
- Let Spring Boot manage Hibernate and Testcontainers versions; do not import a second BOM.

## Phase 2 — Entity and identifier foundation

- Convert entities to explicit JPA mappings as their owning service migrates. Replace Lombok `@Data` with getters/setters and a no-argument constructor.
- Map database-generated IDs with `IDENTITY`.
- Preserve Long Snowflake IDs for Article, ArticleConfig, ArticleContent, and Collection with a Hibernate 6 `BeforeExecutionGenerator` and `@SnowflakeId`.
- Read worker/datacenter IDs through Hibernate `ConfigurationService` from `spring.jpa.properties.leadnews.snowflake.*`, backed by `SNOWFLAKE_WORKER_ID` and `SNOWFLAKE_DATACENTER_ID` (defaults 0, range 0–31).
- Reject clock rollback and invalid configuration; wait for the next millisecond when the per-millisecond sequence is exhausted.
- Keep `Taskinfo.taskId` as `IDENTITY`; keep `TaskinfoLogs.taskId` application-assigned and its nullable `@Version` unset on creation so Spring Data treats it as new.

## Phase 3 — Service-by-service migration

Migrate in this order, completing verification after every service:

1. **Admin:** pilot entity/repository conversion and login lookup.
2. **Behavior:** replace CRUD, existence checks, and conditional deletes.
3. **User:** replace login/follow queries and convert API 1-based pages to Spring Data 0-based pages while preserving response page numbers.
4. **Article:** replace CRUD and feed XML with JPQL joining Article and ArticleConfig by ID. Use nullable time/channel parameters and Pageable for the result limit. Preserve Snowflake IDs.
5. **Wemedia:** replace CRUD and pagination; save ordered material relations with `saveAll`; keep relation replacement transactional.
6. **Schedule:** replace task repositories and use a JPQL conditional update (`status = SCHEDULED`) that increments version. Interpret affected rows as the race winner.
7. **ES Init:** replace its article Mapper/XML with an Article-only JPA repository and JPQL.

For every service, explicitly restrict `@EntityScan` and repository scanning to that service’s domain. Before removing `IService`/`ServiceImpl`, find every inherited CRUD-method call and replace it with a named business-service operation. Controllers and Feign adapters must not access repositories directly.

## Transactions and external systems

- Put derived deletes, JPQL updates, and multi-table writes in public methods on separate transactional beans; do not rely on private methods or self-invocation.
- Article database writes commit before MinIO work. Wemedia database transactions do not encompass Feign, Kafka, or Schedule calls.
- Schedule uses a public transactional persistence component so the conditional log update and Taskinfo deletion commit or roll back together. Redis remains outside the database transaction; a rollback leaves Taskinfo available for reload recovery.
- Do not introduce distributed transactions or claim that JPA alone provides Redis/MySQL atomicity.

## Cleanup

- Remove Mapper interfaces/XML, `@MapperScan`, MyBatis interceptors, wrappers, `BaseMapper`, `IService`, and `ServiceImpl`.
- Remove MyBatis/MyBatis-Plus/PageHelper properties and dependencies, including commented obsolete declarations, and remove the MySQL driver from Model.
- Require zero repository matches for `com.baomidou`, `org.mybatis`, `BaseMapper`, `IService`, MyBatis `ServiceImpl` inheritance, and `*Mapper.xml`.
- Update README persistence, Snowflake configuration, transaction boundaries, and Schedule conditional-update documentation.

## Verification

- After each phase: compile the affected module, run its tests, start its Spring context, and run the full reactor build.
- Use Boot-managed MySQL Testcontainers dependencies in test scope; use `create-drop` only in isolated repository tests.
- Add a real-schema validation profile with `ddl-auto: validate`; normal runtime remains `none`.
- Cover Snowflake uniqueness/configuration/clock rollback, assigned TaskinfoLogs IDs, pagination conversion, Article feed boundaries, Wemedia relation ordering and rollback, and Schedule race/rollback behavior.
- Final acceptance: `mvn clean verify`, ten-application startup and Consul registration, minimal login/article/publish/feed/search smoke tests, Schedule JUnit concurrency test, and Python race test.

## Defaults

- Java 17, Spring Boot 3.5.15, and Spring Cloud 2025.0.3 remain unchanged.
- No general schema migration tool is introduced. Article and Wemedia add documented unique constraints for `ap_author.user_id` and `wm_user.ap_user_id` so cross-service account creation remains idempotent under concurrent approval requests.
- Only one service migration is active at a time; failures are resolved before continuing.
