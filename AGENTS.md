````md id="nz2rfx"
# TaskCore

## Build & Run

```bash
# Run tests (uses H2 in-memory database)
./mvnw clean test

# Build jar (skip tests)
./mvnw clean package -DskipTests

# Run full stack with Docker
docker-compose up -d
````

---

## Tech Stack

* Java 21
* Spring Boot 4.0.6
* PostgreSQL
* Flyway
* Spring Data JPA
* Hibernate EntityGraph
* JPA Specifications
* MapStruct
* Lombok
* Springdoc OpenAPI
* JUnit 5
* Mockito
* Docker

---

## Architecture

```text id="h1pb8c"
src/main/java/github/oliveira/gb/taskcore/
├── api/             # Controllers, DTOs, mappers, exception handling
├── domain/          # Entities, repositories, services, specifications, validations
└── infrastructure/  # Configurations, audit, OpenAPI, CORS, shared infrastructure
```

### Layer Responsibilities

#### API Layer

Responsible for:

* Receiving HTTP requests
* Input validation
* Returning HTTP responses
* Mapping DTOs
* Delegating business logic to services

Controllers may only:

* Validate requests
* Map DTOs
* Delegate execution to services
* Return HTTP responses

Rules:

* Controllers must remain thin
* Controllers must not contain business logic
* Controllers must return `ResponseEntity`
* Never expose entities directly in API responses
* Use DTOs for all request and response payloads
* Use Bean Validation annotations for request validation
* Use proper HTTP status codes
* Use centralized exception handling

---

#### Domain Layer

Responsible for:

* Business rules
* Application orchestration
* Specifications
* Validations
* Persistence contracts

Business logic includes:

* Domain validations
* Workflow orchestration
* Business rule enforcement
* State transitions
* Application decision-making

Rules:

* Services contain business logic
* Repositories are responsible only for data access
* Avoid duplicated business logic
* Prefer descriptive and intention-revealing method names
* Prefer small and readable methods
* Avoid deeply nested conditionals
* Prefer single-responsibility methods

---

#### Infrastructure Layer

Responsible for:

* Framework configuration
* Security configuration
* OpenAPI configuration
* Persistence infrastructure
* Shared technical concerns

---

## Database Rules

* `ddl-auto` must always remain `none`
* Never auto-create or auto-update tables
* All schema changes must be implemented using Flyway migrations
* Migrations are located at:
  `src/main/resources/db/migration`
* Prefer explicit migrations over implicit schema generation
* Never modify database structure outside Flyway
* Never bypass migrations manually

---

## Entity Rules

* Soft delete is mandatory where applicable
* Entities use:

    * `@SQLDelete`
    * `@SQLRestriction`
* Avoid eager relationships unless strictly necessary
* Prefer lazy loading by default
* Prefer explicit fetch strategies
* Avoid exposing entities directly through the API

---

## JPA Performance Rules

* Avoid N+1 query problems
* Use EntityGraph intentionally
* Avoid unnecessary database calls inside loops
* Prefer optimized queries over excessive in-memory processing
* Be careful with collection fetching strategies

---

## API Conventions

* Controllers must return `ResponseEntity`
* Use DTOs for all request and response payloads
* Never expose entities directly in responses
* Use Bean Validation annotations for request validation
* Use proper HTTP status codes
* Use global exception handling for API errors
* Paginated endpoints should use `Pageable`
* Error responses should remain consistent across the API

---

## Service Conventions

* Services contain business rules only
* Services should orchestrate repositories and validations
* Avoid placing persistence logic inside controllers
* Avoid placing HTTP concerns inside services
* Prefer descriptive method names
* Prefer small and readable methods
* Avoid duplicated business logic
* Keep orchestration logic cohesive

---

## Complexity Rules

* Avoid premature abstraction
* Avoid unnecessary design patterns
* Avoid speculative generalization
* Prefer straightforward implementations
* Prefer simple and maintainable solutions
* Avoid excessive indirection
* Introduce abstractions only when they provide clear value

---

## Repository Conventions

* Repositories must contain only persistence concerns
* Avoid business logic inside repositories
* Use Specifications for dynamic filtering when applicable
* Use EntityGraph when necessary to optimize queries
* Prefer explicit query intent

---

## Transaction Rules

* Transaction boundaries belong to the service layer
* Avoid transactions in controllers
* Use read-only transactions for query operations when appropriate
* Keep transactional methods focused and small

---

## Mapping Rules

* Use MapStruct for entity/DTO mapping
* Lombok annotation processor must come before MapStruct in `pom.xml`
* Avoid manual mapping unless necessary
* Keep mappings explicit and predictable

---

## Dependency Injection

* Prefer constructor injection
* Do not use field injection

---

## Null Handling

* Prefer `Optional` instead of returning `null`
* Avoid nullable returns when possible
* Validate inputs explicitly
* Avoid defensive null checks when validation already guarantees correctness

---

## Exception Handling Rules

* Use custom business exceptions when appropriate
* Avoid generic `RuntimeException`
* Exceptions must provide meaningful messages
* API error handling must be centralized
* Do not expose internal stack traces in API responses
* Keep exception hierarchy simple and predictable

---

## Logging Rules

* Use structured and meaningful logs
* Avoid excessive logging
* Do not log sensitive information
* Use error logs only for actual failures
* Prefer concise and contextual log messages

---

## Documentation Rules

* Prefer self-explanatory code over excessive comments
* Do not add redundant comments
* Do not comment obvious implementations
* Add comments only when business intent or technical reasoning is not obvious
* Keep JavaDoc concise and meaningful
* Avoid generating noisy documentation

---

## Security Rules

* Never trust client input
* Validate all external input
* Do not expose sensitive internal data
* Avoid leaking technical details in API responses
* Never hardcode credentials or secrets

---

## Naming Conventions

* Use descriptive and intention-revealing names
* Avoid abbreviations unless widely known
* Class names should represent responsibilities clearly
* Method names should express actions explicitly
* Prefer consistency with existing naming patterns

---

## Testing Rules

* Use JUnit 5 and Mockito
* Follow Arrange / Act / Assert pattern
* Create tests for:

    * Success scenarios
    * Validation failures
    * Business rule failures
    * Exception scenarios
* Prefer unit tests for service logic
* Integration tests should use H2 in-memory database
* Mock external dependencies only
* Keep tests isolated and deterministic
* Prefer readable assertions

---

## OpenAPI

Swagger UI available at:

```text id="qut7s6"
/swagger-ui/index.html
```

---

## Environment

* `.env` file exists at project root
* Database credentials may differ from `docker-compose`
* Do not hardcode credentials
* Keep environment-specific configuration externalized

---

## CI

GitHub Actions pipeline (`pipeline.yml`) runs:

```bash id="uqdyot"
./mvnw clean test
```

Triggers:

* push to:

    * `main`
    * `develop`
    * `feature/*`
    * `test/*`
* pull requests to:

    * `main`
    * `develop`

---

## Refactoring Guidelines

* Preserve existing architecture during refactors
* Avoid unnecessary rewrites
* Prefer incremental improvements
* Maintain backward compatibility when possible
* Prefer consistency over introducing new patterns

---

## Scope Awareness

* Avoid modifying unrelated files
* Minimize the impact radius of changes
* Prefer localized modifications
* Respect existing module boundaries
* Avoid unnecessary large-scale refactors
* Only modify files directly related to the requested task

---

## Consistency Rules

* Follow existing project patterns before introducing new approaches
* Prefer consistency with the current codebase over personal preference
* Reuse existing abstractions when appropriate
* Avoid introducing unnecessary frameworks or layers

---

## Agent Behavior

* Do not invent frameworks, APIs, or project structures that do not exist
* Prefer asking for clarification over making risky assumptions
* Follow existing project conventions before introducing new patterns
* Generate production-safe code
* Prefer maintainable solutions over clever solutions
* Avoid speculative abstractions
* Respect existing architecture and conventions

---

## Forbidden Practices

* Do not use `ddl-auto=create`
* Do not use `ddl-auto=update`
* Do not expose entities directly in controllers
* Do not place business logic inside controllers
* Do not use field injection
* Do not duplicate validation logic
* Do not bypass Flyway migrations
* Do not hardcode credentials
* Do not create overly large service methods
* Do not ignore exception handling
* Do not introduce unnecessary abstractions
* Do not create inconsistent architectural patterns

---

## Priorities

Priority order:

1. Correctness
2. Maintainability
3. Consistency with existing architecture
4. Readability
5. Simplicity
6. Performance optimization

---

## Development Philosophy

* Prefer readability over clever code
* Prefer maintainability over premature optimization
* Follow clean architecture principles
* Keep code cohesive and predictable
* Maintain consistency with existing project structure
* Generate production-ready code
* Prefer explicitness over implicit behavior

```
```
