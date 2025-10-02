# Contributing to PokerHole Server

Thank you for your interest in contributing to PokerHole Server! This document provides guidelines and standards for development.

---

## Table of Contents

1. [Development Setup](#development-setup)
2. [Architecture Guidelines](#architecture-guidelines)
3. [Coding Standards](#coding-standards)
4. [Object Mapping Rules](#object-mapping-rules)
5. [Testing Requirements](#testing-requirements)
6. [Git Workflow](#git-workflow)
7. [Pull Request Process](#pull-request-process)

---

## Development Setup

### Prerequisites

- **Java**: 21 or higher (Virtual Threads support required)
- **Docker**: For PostgreSQL container
- **Gradle**: 9.x (included via wrapper)
- **Go**: 1.21+ (for client development)

### Database Setup

```bash
# Start PostgreSQL container
docker compose up -d

# Verify database is running
docker ps | grep postgres
```

### Running the Application

```bash
# Build and run server
./gradlew bootRun

# Run tests
./gradlew test

# Run architecture tests
./gradlew test --tests "*HexagonalArchitectureTest"

# Generate test coverage report
./gradlew test jacocoTestReport
open build/reports/jacoco/test/html/index.html
```

### Client Development

```bash
# Navigate to client directory
cd client

# Install dependencies
go mod download

# Build client
go build -o poker-client cmd/poker-client/main.go

# Run client
./poker-client
```

---

## Architecture Guidelines

### Hexagonal Architecture (Ports & Adapters)

PokerHole Server follows strict Hexagonal Architecture principles:

```
Domain Layer (core/domain)
    ↑
Application Layer (core/application)
    ↑
Adapter Layer (adapter/in, adapter/out)
```

### Dependency Rules

1. **Domain Layer** (`core/domain`)
   - **NO external dependencies** (Pure Java)
   - Exception: Lombok, MapStruct (annotation processors only)
   - No Spring, JPA, or framework dependencies
   - No references to other layers

2. **Application Layer** (`core/application`)
   - Depends on Domain only
   - Defines Port interfaces (in/out)
   - Implements Use Cases
   - No knowledge of Adapters

3. **Adapter Layer** (`adapter`)
   - Depends on Application and Domain
   - Implements Port interfaces
   - Handles external systems (DB, WebSocket, etc.)

4. **UI Layer** (`ui`)
   - Calls business logic through Application Ports only
   - No direct access to Domain internals

### Package Structure

```
dev.xiyo.pokerhole/
├── core/
│   ├── domain/           # Pure domain models
│   │   ├── game/
│   │   ├── player/
│   │   ├── matching/
│   │   └── ai/
│   ├── application/      # Use cases and ports
│   │   ├── port/in/
│   │   ├── port/out/
│   │   └── mapper/       # Application-level mappers
│   └── common/
│       └── exception/
├── adapter/
│   ├── in/               # Input adapters
│   │   ├── websocket/
│   │   └── web/
│   └── out/              # Output adapters
│       ├── persistence/
│       │   └── mapper/   # Persistence mappers
│       ├── event/
│       ├── matching/
│       └── ai/
└── configuration/
```

---

## Coding Standards

### Naming Conventions

#### Use Case Interfaces
- **Must** end with `UseCase` suffix
- Example: `PlaceBetUseCase`, `StartRoundUseCase`

#### Port Interfaces
- **Must** end with `Port` suffix
- Example: `GameRepositoryPort`, `EventPublisherPort`

#### Domain Events
- **Must** end with `Event` suffix or use past tense verb
- Example: `RoundStartedEvent`, `MatchingCompleted`

#### Value Objects
- Use noun or noun phrase
- Example: `PlayerId`, `Nickname`, `HandResult`

#### Aggregates
- Use domain entity name
- Example: `Player`, `Game`, `Room`

### Code Style

1. **Lombok Usage**
   - Use `@Getter`, `@Builder` for Value Objects
   - Use `@RequiredArgsConstructor` for dependency injection
   - Avoid `@Data` (too broad)

2. **Immutability**
   - Value Objects must be immutable
   - Use `final` fields
   - Use `@EqualsAndHashCode` for Value Objects

3. **Null Safety**
   - Avoid returning `null`
   - Use `Optional<T>` for optional values
   - Validate inputs with `Objects.requireNonNull()`

---

## Object Mapping Rules

All object conversions **must** use MapStruct. Manual mapping is prohibited.

### Mapper Locations

#### 1. Persistence Mappers
**Location**: `adapter/out/persistence/mapper/`
**Purpose**: JPA Entity ↔ Domain conversion

```java
@Mapper(componentModel = "spring")
public interface PlayerEntityMapper {
    Player toDomain(PlayerJpaEntity entity);
    PlayerJpaEntity toEntity(Player domain);
    List<Player> toDomainList(List<PlayerJpaEntity> entities);
}
```

#### 2. Application Mappers
**Location**: `core/application/mapper/`
**Purpose**: Domain ↔ DTO conversion

```java
@Mapper(componentModel = "spring")
public interface PlayerMapper {
    PlayerDTO toDTO(Player domain);
    Player toDomain(PlayerDTO dto);
}
```

#### 3. Input Adapter Mappers
**Location**: `adapter/in/web/mapper/`
**Purpose**: Request/Response ↔ Command conversion

```java
@Mapper(componentModel = "spring")
public interface GameRequestMapper {
    StartRoundCommand toCommand(StartRoundRequest request);
    GameResponse toResponse(Game game);
}
```

### Mapper Configuration

```java
// configuration/MapperConfig.java
@Configuration
@ComponentScan(basePackages = {
    "dev.xiyo.pokerhole.core.application.mapper",
    "dev.xiyo.pokerhole.adapter.out.persistence.mapper",
    "dev.xiyo.pokerhole.adapter.in.web.mapper"
})
public class MapperConfig {
    // MapStruct mappers auto-registered as Spring beans
}
```

### Build Configuration

```kotlin
// build.gradle.kts
dependencies {
    implementation("org.mapstruct:mapstruct:1.6.3")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.6.3")

    compileOnly("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok-mapstruct-binding:0.2.0")
}
```

### Mapper Examples

**Good**:
```java
@Mapper(componentModel = "spring")
public interface PlayerMapper {
    Player toDomain(PlayerJpaEntity entity);
    PlayerJpaEntity toEntity(Player domain);
    List<Player> toDomainList(List<PlayerJpaEntity> entities);
}
```

**Bad** (missing componentModel):
```java
@Mapper
public interface PlayerMapper {
    Player map(PlayerJpaEntity entity);
}
```

---

## Testing Requirements

### Test Coverage Goals

- **Domain Layer**: 95% or higher
- **Application Layer**: 90% or higher
- **Adapter Layer**: 75% or higher
- **Overall Average**: 85% or higher

### Test Types

#### 1. Unit Tests
- **Required for**: All domain logic and use cases
- **Location**: `src/test/java` (same package as code)
- **Naming**: `ClassNameTest.java`
- **Framework**: JUnit 5, AssertJ, Mockito

Example:
```java
@DisplayName("HandEvaluator Tests")
class HandEvaluatorTest {

    @Test
    @DisplayName("Should identify royal flush")
    void shouldIdentifyRoyalFlush() {
        // given
        List<Card> cards = List.of(
            new Card(Suit.HEARTS, Rank.ACE),
            new Card(Suit.HEARTS, Rank.KING),
            // ...
        );

        // when
        HandResult result = evaluator.evaluateFiveCards(cards);

        // then
        assertThat(result.getRanking()).isEqualTo(Tier.ROYAL_FLUSH);
    }
}
```

#### 2. Integration Tests
- **Required for**: Adapter implementations
- **Location**: `src/test/java` with `@SpringBootTest`
- **Tools**: TestContainers for database
- **Naming**: `ClassNameIntegrationTest.java`

Example:
```java
@SpringBootTest
@Testcontainers
class GamePersistenceAdapterIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Test
    void shouldPersistAndRetrieveGame() {
        // test implementation
    }
}
```

#### 3. Architecture Tests
- **Required for**: All packages
- **Tool**: ArchUnit
- **Location**: `src/test/java/.../architecture/`

Example:
```java
@AnalyzeClasses(packages = "dev.xiyo.pokerhole")
class HexagonalArchitectureTest {

    @ArchTest
    static final ArchRule domainLayerShouldNotDependOnOutside =
        classes()
            .that().resideInAPackage("..core.domain..")
            .should().onlyDependOnClassesInPackages(
                "..core.domain..",
                "java..",
                "org.projectlombok..",
                "org.mapstruct.."
            );
}
```

#### 4. Mapper Tests
- **Required for**: All MapStruct mappers
- **Verify**: Field mappings, null handling
- **Count**: 10 tests per mapper minimum

Example:
```java
class PlayerMapperTest {

    @Test
    void shouldMapEntityToDomain() {
        PlayerJpaEntity entity = new PlayerJpaEntity(/* ... */);
        Player domain = mapper.toDomain(entity);

        assertThat(domain.getId()).isEqualTo(entity.getId());
        assertThat(domain.getNickname()).isEqualTo(entity.getNickname());
        // verify all fields
    }
}
```

### Running Tests

```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests HandEvaluatorTest

# Run tests with coverage
./gradlew test jacocoTestReport

# Run only architecture tests
./gradlew test --tests "*ArchitectureTest"
```

---

## Git Workflow

### Branch Naming

- `feature/task-description` - New features
- `bugfix/issue-description` - Bug fixes
- `refactor/component-name` - Refactoring
- `test/test-description` - Test additions

Examples:
- `feature/betting-action-validation`
- `bugfix/pot-calculation-error`
- `refactor/game-aggregate-extraction`

### Commit Messages

Follow conventional commits format:

```
<type>(<scope>): <subject>

<body>

<footer>
```

**Types**:
- `feat`: New feature
- `fix`: Bug fix
- `refactor`: Code refactoring
- `test`: Test additions/changes
- `docs`: Documentation changes
- `chore`: Build/config changes

**Examples**:
```
feat(game): implement betting action validation

- Add BettingValidator domain service
- Implement minimum/maximum bet checks
- Add RAISE amount validation (2x previous bet)

Closes #42
```

```
test(hand-evaluator): add edge case tests for ace-low straight

- Test A-2-3-4-5 straight detection
- Verify ace can be used as low card
- Add 15 new test cases
```

### Pre-commit Checklist

Before committing:

- [ ] Code compiles without errors
- [ ] All tests pass (`./gradlew test`)
- [ ] Architecture tests pass
- [ ] No SonarQube Critical issues
- [ ] Code follows naming conventions
- [ ] MapStruct used for all object mapping
- [ ] Javadoc added for public APIs
- [ ] Test coverage meets requirements

---

## Pull Request Process

### Before Creating PR

1. **Update from main**
   ```bash
   git checkout main
   git pull origin main
   git checkout your-feature-branch
   git rebase main
   ```

2. **Run full test suite**
   ```bash
   ./gradlew clean build
   ```

3. **Check code quality**
   ```bash
   ./gradlew test jacocoTestReport
   # Verify coverage meets requirements
   ```

### PR Requirements

1. **Description**
   - Clear title describing the change
   - Reference related issues (`Closes #123`)
   - List of changes made
   - Test plan description

2. **Code Quality**
   - All tests passing
   - Coverage requirements met
   - No architecture violations
   - No merge conflicts

3. **Documentation**
   - Update relevant documentation
   - Add Javadoc for public APIs
   - Update CHANGELOG.md if needed

### PR Template

```markdown
## Description
Brief description of the changes

## Related Issues
Closes #123

## Changes Made
- Added BettingValidator domain service
- Implemented PlaceBetUseCase
- Added 50 unit tests

## Test Plan
- Unit tests for all betting scenarios
- Integration tests for betting flow
- Manual testing: verified in local environment

## Checklist
- [ ] Tests passing
- [ ] Architecture tests passing
- [ ] Coverage ≥ 85%
- [ ] Documentation updated
- [ ] No breaking changes
```

### Review Process

1. **Automated Checks**
   - Build must pass
   - Tests must pass
   - Coverage must meet requirements

2. **Code Review**
   - At least 1 approving review required
   - Address all review comments
   - Re-request review after changes

3. **Merge**
   - Use "Squash and merge" for feature branches
   - Use "Rebase and merge" for hotfixes
   - Delete branch after merge

---

## Additional Resources

- [ARCHITECTURE.md](ARCHITECTURE.md) - Architecture documentation
- [ROADMAP.md](ROADMAP.md) - Development roadmap
- [CHANGELOG.md](CHANGELOG.md) - Version history

---

## Questions?

If you have questions about contributing:
1. Check existing documentation
2. Search closed issues
3. Open a new issue with the `question` label

## Related Projects

- [PokerHole CLI](https://github.com/bunnyholes/pokerhole-cli) - Go-based terminal client

Thank you for contributing to PokerHole Server!
