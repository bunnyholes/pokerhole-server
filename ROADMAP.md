# PokerHole Server - Development Roadmap

**Version**: 3.0
**Last Updated**: 2025-10-02
**Current Status**: Phase 5 partial complete, Phase 6 in progress
**Target v1.0 Release**: 2026 Q4

---

## Table of Contents

1. [Project Overview](#project-overview)
2. [Current Status](#current-status)
3. [Technology Stack](#technology-stack)
4. [Phase Implementation Plan](#phase-implementation-plan)
5. [Domain Model Specification](#domain-model-specification)
6. [Use Case Definitions](#use-case-definitions)
7. [Quality Standards](#quality-standards)
8. [Future Roadmap](#future-roadmap)
9. [Risk Management](#risk-management)

---

## Project Overview

### Vision

PokerHole Server is a backend server for multiplayer Texas Hold'em poker games. Built with Hexagonal Architecture and Domain-Driven Design principles, it leverages modern technologies (Spring Boot 4.x, Java 21) to provide a scalable and maintainable gaming platform via WebSocket communication.

### Core Values

- **Clean Architecture**: Clear separation between business logic and infrastructure
- **Domain-Centric**: Focus on the essence of poker game through domain models
- **Extensibility**: Easy to add new game modes, UIs, and features
- **Testability**: Each layer independently testable
- **Modern Technology**: Utilizing Java 21 features (Virtual Threads, Pattern Matching, Sealed Classes)

### Key Features

#### Completed Features
- Terminal (TCP) and WebSocket connection support
- Banner screen and menu system
- Random/Code matching system
- AI players with automatic deployment (3 strategies: Conservative, Aggressive, Random)
- Game session lifecycle management
- Basic round start/end logic
- Hexagonal Architecture-based package structure
- ArchUnit architecture tests

#### In Progress
- Poker hand evaluation system
- Betting round management
- Actual poker game logic

#### Planned Features
- Complete Texas Hold'em rules implementation
- Comprehensive test coverage (target 85%+)
- Spectator mode
- Chat system
- Leaderboard and tournaments

---

## Current Status

### Progress Summary

| Phase | Status | Completion | Key Items |
|-------|--------|------------|-----------|
| Phase 1-4 | Completed | 100% | Infrastructure, Domain, Adapters, UI |
| Phase 5 | In Progress | 70% | Game Logic Integration (Room incomplete) |
| Phase 6 | In Progress | 11% | Poker Core Logic (1/9 tasks done) |
| Phase 7 | Pending | 0% | Testing & Quality Assurance |
| Phase 8 | Pending | 0% | Advanced Features |

**Overall Progress**: Approximately 35-40%

### Completed Work

**Phase 1-4 (100%)**
- Hexagonal Architecture structure
- Core domain models (Card, Player, Game, Matching, AI)
- Adapter layers (WebSocket, JPA, Event)
- Terminal UI and matching system

**Phase 5 (Partial, ~70%)**
- Round start/end logic
- Card distribution
- Room management (Legacy code integration pending)

**Phase 6 (In Progress, ~11%)**
- Task 6.1 Complete: Poker hand evaluation system (773 lines, 25 tests)
- Tasks 6.2-6.8 Pending: Betting, pot management, round orchestration, etc.

### Current Priorities

1. Complete remaining Phase 6 tasks (Tasks 6.2-6.8)
2. Phase 7 testing and quality assurance
3. Phase 8 advanced features

---

## Technology Stack

### Core Framework
- **Spring Boot**: 4.0.0-M3
- **Java**: 21 (LTS, Virtual Threads support)
- **Gradle**: 9.x (Kotlin DSL)

### Major Libraries
- **MapStruct**: 1.6.3 (Object mapping)
- **JLine**: 3.27.1 (Terminal UI)
- **Quartz Scheduler**: Background tasks
- **Problem Details (RFC 7807)**: Standardized error responses
- **OpenAPI 3.1 (springdoc)**: API documentation
- **Micrometer & Prometheus**: Metrics collection
- **Lombok**: 1.18.34 (Boilerplate reduction)

### Testing Infrastructure
- **JUnit 5**: Unit testing framework
- **TestContainers**: Actual DB integration tests
- **ArchUnit**: Architecture rule validation
- **Awaitility**: Async testing
- **PITest**: Mutation testing
- **AssertJ**: Assertion API
- **Mockito**: Mocking framework

### Database
- **PostgreSQL**: 16.x (Production, Docker Compose)
- **H2**: 2.3.232 (Test environment, in-memory)

---

## Phase Implementation Plan

### Phase 1: Infrastructure & Architecture Foundation (Completed)

**Period**: 2025-01 ~ 2025-02
**Goal**: Build technical foundation and apply architecture patterns

- [x] Gradle Kotlin DSL migration
- [x] Spring Boot 4.0.0-M3 upgrade
- [x] Java 21 Toolchain configuration
- [x] Hexagonal Architecture package structure
- [x] ArchUnit architecture tests

---

### Phase 2: Core Domain Model Implementation (Completed)

**Period**: 2025-03 ~ 2025-04
**Goal**: Build core domain models for poker game in Pure Java

- [x] Card domain (Rank, Suit, Card, Tier, Deck, Hand)
- [x] Player domain (PlayerId, Nickname, Player, PlayerRecord)
- [x] Game domain basics (GameId, Pot, BettingRound, GameState, GameEvent)
- [x] Matching domain (MatchingType, MatchingCode, MatchingRequest, MatchingQueue)
- [x] AI domain (AIStrategyType, AIDecision, AIPlayer, AIStrategy)
- [x] Shared domain abstractions (DomainEvent, AggregateRoot, exception classes)

---

### Phase 3: Adapter Layer Implementation (Completed)

**Period**: 2025-05 ~ 2025-06
**Goal**: Implement input/output adapters and infrastructure integration

- [x] Terminal (TCP), WebSocket adapters
- [x] JPA Persistence adapter
- [x] Event, Matching, AI, Network adapters

---

### Phase 4: Terminal UI and Game Flow (Completed)

**Period**: 2025-07 ~ 2025-08
**Goal**: Implement terminal-based UI and game flow

- [x] Banner system
- [x] Menu system
- [x] Matching system
- [x] AI player system
- [x] Game session lifecycle

---

### Phase 5: Game Logic Integration (Partial)

**Period**: 2025-09 ~ 2025-10
**Goal**: Basic game logic integration and round management

- [x] Round start logic
- [x] Card distribution
- [x] Round end logic
- [ ] Room management (Legacy integration)

---

### Phase 6: Poker Core Logic Implementation (In Progress)

**Period**: 2025-11 ~ 2026-03 (6 months)
**Goal**: Complete Texas Hold'em poker rules implementation
**Expected Team**: 2 backend developers

#### Task 6.1: Poker Hand Evaluation System (2 weeks) - COMPLETED

- [x] HandRanking enum (HIGH_CARD ~ ROYAL_FLUSH)
- [x] HandEvaluator service interface
- [x] HandResult Value Object
- [x] Each hand type verification methods (isRoyalFlush, isStraightFlush, etc.)
- [x] Hand comparison logic and kicker handling
- [x] 100+ unit tests

**Completion Date**: 2025-10-02
**Code**: 773 lines (267 production + 506 test)
**Tests**: 25 test cases (100% pass)

#### Task 6.2: Betting Actions and Validation (2 weeks) - PENDING

- [ ] BettingAction enum (FOLD, CHECK, CALL, RAISE, ALL_IN)
- [ ] BettingActionCommand DTO
- [ ] PlaceBetUseCase
- [ ] BettingValidator domain service
- [ ] BettingException
- [ ] 50+ unit tests

**Estimated Duration**: 2-3 days

#### Task 6.3: Pot Management (Main & Side Pots) (3 weeks) - PENDING

- [ ] Pot Value Object extension
- [ ] PotManager domain service
- [ ] SidePot Value Object
- [ ] All-in side pot creation logic
- [ ] Pot distribution logic
- [ ] 70+ unit tests

**Estimated Duration**: 3-4 days
**Complexity**: High (4/5 stars) - All-in scenarios are complex

#### Task 6.4: Betting Round Management (2 weeks) - PENDING

- [ ] BettingRoundManager domain service
- [ ] PlayerPosition enum
- [ ] Betting round progression logic
- [ ] Blind handling
- [ ] Betting round completion conditions
- [ ] 40+ unit tests

**Estimated Duration**: 2-3 days

#### Task 6.5: Community Card Management (1 week) - PENDING

- [ ] CommunityCards Value Object
- [ ] revealFlop, revealTurn, revealRiver methods
- [ ] Community card broadcasting

**Estimated Duration**: 1-2 days
**Complexity**: Low (2/5 stars) - Relatively simple logic

#### Task 6.6: Game Round Orchestration (4 weeks) - PENDING

- [ ] RoundOrchestrator domain service
- [ ] startRound method
- [ ] proceedToNextPhase method
- [ ] handleBettingAction method
- [ ] determineWinner method
- [ ] handleFoldedPlayers method
- [ ] 30+ integration test scenarios

**Estimated Duration**: 3-5 days
**Complexity**: Very High (5/5 stars) - Core logic integrating all components

#### Task 6.7: Player Action Handling and Timeout (2 weeks) - PENDING

- [ ] PlayerActionHandler
- [ ] Action input parsing
- [ ] Timeout handling
- [ ] Action broadcasting
- [ ] AI player automatic actions

**Estimated Duration**: 2-3 days

#### Task 6.8: Game Aggregate Extraction (3 weeks) - PENDING

- [ ] Game Aggregate Root definition
- [ ] Dealer class refactoring
- [ ] GameRepository Port
- [ ] GamePersistenceAdapter (using MapStruct)
- [ ] 80+ Game aggregate unit tests

**Estimated Duration**: 2-3 days

#### Task 6.9: MapStruct Mapper Integration (2 weeks) - PENDING

- [ ] Persistence mapper implementation (PlayerEntityMapper, GameEntityMapper, RoomEntityMapper)
- [ ] Application mapper implementation (PlayerMapper, GameMapper, MatchingMapper)
- [ ] Input adapter mapper implementation (GameRequestMapper, PlayerRequestMapper)
- [ ] MapperConfig configuration
- [ ] Mapper unit tests (10 per mapper)

---

### Phase 7: Testing and Quality Assurance (Planned)

**Period**: 2026-04 ~ 2026-06 (3 months)
**Goal**: Comprehensive test coverage and quality validation
**Expected Team**: 1 QA, 2 backend developers

#### Task 7.1: Unit Tests (4 weeks)

**Coverage Goals**:
- Domain Layer: 95%+
- Application Layer: 90%+

**Expected Tests**: 500+

#### Task 7.2: Integration Tests (4 weeks)

- [ ] MatchingIntegrationTest
- [ ] GameSessionLifecycleTest
- [ ] PersistenceIntegrationTest (TestContainers)
- [ ] WebSocketIntegrationTest
- [ ] EventPublishingIntegrationTest

**Expected Tests**: 100+

#### Task 7.3: E2E Tests (2 weeks)

- [ ] FullGameE2ETest
- [ ] MultiPlayerE2ETest
- [ ] AIPlayerE2ETest

**Expected Tests**: 30+

#### Task 7.4: Architecture Test Extension (1 week)

- [ ] Matching/AI domain dependency tests
- [ ] Use Case/Port naming tests
- [ ] Adapter dependency tests

#### Task 7.5: Performance Tests (2 weeks)

**Performance Goals**:
- Concurrent Users: 100+
- Matching Processing Time: < 1 second average
- Hand Evaluation Time: < 10ms
- Response Time: p95 < 500ms

**Tools**: JMeter, Gatling

#### Task 7.6: Mutation Tests (1 week)

**Mutation Score Goals**:
- Domain Layer: 80%+
- Application Layer: 70%+

**Tool**: PITest

#### Task 7.7: Test Coverage Measurement (1 week)

- JaCoCo report generation
- SonarQube integration

---

### Phase 8: Advanced Features and Optimization (Planned)

**Period**: 2026-07 ~ 2026-12 (6 months)
**Goal**: User experience improvement and advanced features
**Expected Team**: 2 backend, 1 frontend, 1 designer

#### Task 8.1: Spectator Mode (2 weeks)
#### Task 8.2: Chat System (3 weeks)
#### Task 8.3: Leaderboard and Statistics (3 weeks)
#### Task 8.4: Tournament Mode (6 weeks)
#### Task 8.5: Performance Optimization (3 weeks)
#### Task 8.6: Security Enhancement (2 weeks)
#### Task 8.7: Monitoring and Logging (2 weeks)

---

## Domain Model Specification

### Aggregates

#### 1. Player Aggregate

**Root Entity**: Player

**Components**:
- PlayerId (Value Object): UUID-based identifier
- Nickname (Value Object): 3-20 characters, alphanumeric and underscore
- chips (long): Current chips (0 or more)
- PlayerRecord (Entity): Win/loss record

**Business Rules**:
- Nickname: 3-20 characters, alphanumeric and underscore only
- chips must be 0 or more
- Cannot participate in game if chips are 0

**Responsibilities**:
- Chip management (increase/decrease)
- Record updates

#### 2. Game Aggregate (Phase 6)

**Root Entity**: Game

**Components**:
- GameId (Value Object)
- List<Player> players (2-10 players)
- Deck deck
- CommunityCards (Value Object)
- Pot (Value Object)
- BettingRound (Value Object)
- GameState (Enum)

**Business Rules**:
- Minimum 2, maximum 10 players
- All players must have sufficient chips at game start
- Betting rounds proceed sequentially (PRE_FLOP -> FLOP -> TURN -> RIVER)

**Responsibilities**:
- Round start/end
- Betting action processing
- Winner determination and pot distribution

#### 3. Room Aggregate (Phase 5.4)

**Root Entity**: Room

**Components**:
- RoomId (Value Object)
- name (String)
- List<Player> players
- hostPlayerId (PlayerId)
- RoomState (Enum)

**Business Rules**:
- Only host can start game
- Host transfer to next player when host leaves
- Cannot enter when max capacity exceeded

**Responsibilities**:
- Player join/leave management
- Host authority management

#### 4. MatchingQueue Aggregate

**Root Entity**: MatchingQueue

**Components**:
- List<MatchingRequest> requests
- MatchingType (Enum)
- createdAt (Instant)

**Business Rules**:
- AI deployment when 10-second timeout exceeded
- Game creation when 4 players achieved

**Responsibilities**:
- Matching request management
- Timeout detection

### Value Objects

1. PlayerId: UUID wrapper
2. Nickname: String wrapper with validation
3. Pot: long amount, immutable
4. BettingRound: Enum (PRE_FLOP, FLOP, TURN, RIVER)
5. GameId: UUID wrapper
6. MatchingCode: 6-digit code
7. AIDecision: BettingAction, amount, reason
8. HandRanking (Phase 6): HIGH_CARD ~ ROYAL_FLUSH
9. HandResult (Phase 6): HandRanking, cards, kickers
10. CommunityCards (Phase 6): List<Card>
11. SidePot (Phase 6): amount, eligiblePlayers

### Domain Events

1. **GameEvent** (Sealed Interface)
   - RoundStarted
   - RoundEnded
   - BettingPhaseStarted

2. **MatchingEvent**
   - MatchingCompleted
   - MatchingTimeout

---

## Use Case Definitions

### Input Ports (Use Cases)

#### Game Use Cases

1. **StartRoundUseCase**
   - Input: StartRoundCommand (gameId, playerId)
   - Output: RoundId

2. **PlaceBetUseCase** (Phase 6)
   - Input: BettingActionCommand (gameId, playerId, action, amount)
   - Output: void

3. **FoldUseCase** (Phase 6)
   - Input: FoldCommand (gameId, playerId)
   - Output: void

4. **JoinAsSpectatorUseCase** (Phase 8)
   - Input: JoinSpectatorCommand (gameId, spectatorId)
   - Output: void

#### Room Use Cases (Phase 5.4)

1. **CreateRoomUseCase**
   - Input: CreateRoomCommand (name, hostPlayerId)
   - Output: RoomId

2. **JoinRoomUseCase**
   - Input: JoinRoomCommand (roomId, playerId)
   - Output: void

3. **LeaveRoomUseCase**
   - Input: LeaveRoomCommand (roomId, playerId)
   - Output: void

#### Matching Use Cases

1. **JoinRandomMatchingUseCase**
   - Input: JoinRandomMatchingCommand (playerId)
   - Output: MatchingRequestId

2. **JoinCodeMatchingUseCase**
   - Input: JoinCodeMatchingCommand (playerId, code)
   - Output: MatchingRequestId

3. **CreateMatchingCodeUseCase**
   - Input: CreateMatchingCodeCommand (playerId)
   - Output: MatchingCode

4. **CancelMatchingUseCase**
   - Input: CancelMatchingCommand (playerId)
   - Output: void

#### AI Use Cases

1. **RequestAIPlayerUseCase**
   - Input: RequestAIPlayerCommand (strategyType)
   - Output: AIPlayer

### Output Ports

#### Game Ports

1. **EventPublisher**: publish(DomainEvent)
2. **GameRepositoryPort** (Phase 6): save, findById, findAll

#### Room Ports (Phase 5.4)

1. **RoomRepositoryPort**: save, findById, findAll

#### Matching Ports

1. **MatchingQueuePort**: add, remove, findAll
2. **MatchingNotificationPort**: notifyMatchingComplete

#### AI Ports

1. **AIStrategyPort**: decide(AIPlayer, GameContext)

#### Player Ports

1. **PlayerRepositoryPort**: save, findById, findAll

---

## Quality Standards

### Test Coverage Goals

- Domain Layer: 95%+
- Application Layer: 90%+
- Adapter Layer: 75%+
- Overall Average: 85%+

### Mutation Score Goals

- Domain Layer: 80%+
- Application Layer: 70%+

### Architecture Rules

- Domain has no external dependencies (Pure Java)
- Application does not depend on Adapters
- No circular dependencies
- MapStruct mappers located in clear layers

### Performance Standards

- Concurrent Users: 100+
- Matching Processing Time: < 1 second average
- Hand Evaluation Time: < 10ms
- Response Time: p95 < 500ms

### Security Standards

- All user input validation
- Rate Limiting: 10 requests per second per player
- Session Timeout: Auto-logout after 30 minutes inactivity

### Code Quality

- Boilerplate minimization using Lombok
- Object mapping automation using MapStruct
- Clear and consistent naming
- SonarQube Critical issues: 0

---

## Future Roadmap

### v1.0 (2026-06) - MVP

**Goal**: Complete Texas Hold'em poker server

- Complete Texas Hold'em poker rules
- Terminal and WebSocket support
- Random/Code matching system
- AI players (3 strategies)
- Test coverage 85%+

**Release Date**: June 2026

---

### v1.1 (2026-09)

**Goal**: User experience improvement

- Spectator mode
- Chat system
- Leaderboard and statistics

**Release Date**: September 2026

---

### v1.2 (2026-12)

**Goal**: Advanced features

- Tournament mode
- Performance optimization (Caffeine Cache, Virtual Threads)
- Security enhancement (Rate Limiting, input validation)
- Monitoring (Prometheus, Grafana, ELK)

**Release Date**: December 2026

---

### v2.0 (2027-06) - Web Platform

**Period**: 2027-01 ~ 2027-06 (6 months)
**Expected Team**: 2 frontend, 2 backend, 1 designer

#### Phase 9: REST API Development (2 months)

- RESTful API design (OpenAPI 3.1)
- Spring Web MVC Controller implementation
- API authentication/authorization (Spring Security, JWT)
- API documentation auto-generation (springdoc-openapi)

#### Phase 10: React Web UI Development (4 months)

- React project setup (Vite, TypeScript, TailwindCSS)
- Game UI components (poker table, cards, player avatars)
- WebSocket integration (SockJS, STOMP)
- Responsive design (mobile/tablet/desktop)
- Leaderboard and statistics UI (Chart.js)

**Release Date**: June 2027

---

### v2.1 (2027-12) - Mobile App

**Period**: 2027-07 ~ 2027-12 (6 months)
**Expected Team**: 2 mobile, 2 backend, 1 frontend, 1 designer

#### Phase 11: Mobile App Development (4 months)

- React Native project setup (Expo, TypeScript)
- Mobile game UI (touch optimization, portrait/landscape)
- Push notifications (Firebase Cloud Messaging)

#### Phase 12: Social Features (2 months)

- Friend system (add/remove friends, friend invites)
- Social login (Google, Facebook, Apple)

**Release Date**: December 2027

---

### v3.0 (2028-06) - Global Service

**Period**: 2028-01 ~ 2028-06 (6 months)
**Expected Team**: 2 ML engineers, 3 backend, 2 DevOps, 1 frontend

#### Phase 13: Machine Learning AI Players (3 months)

- Reinforcement learning model development (TensorFlow, PyTorch, PPO)
- AI training data collection (100,000+ actual game logs)
- AI model serving (TensorFlow Serving, gRPC)

#### Phase 14: Multi-Region Support (3 months)

- CDN integration (Cloudflare, AWS CloudFront)
- Multi-language support (English, Korean, Japanese, Chinese)
- Regional server deployment (AWS, GCP, Azure)

**Release Date**: June 2028

---

## Risk Management

### Technical Risks

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| Spring Boot 4.x Milestone instability | High | Medium | Wait for GA release, prepare alternative version |
| Virtual Threads performance issues | Medium | Low | Early performance testing, Thread Pool alternative |
| MapStruct learning curve | Low | High | Team training, example code sharing |
| PostgreSQL scaling limits | Medium | Low | Redis caching, Read Replica |

### Schedule Risks

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| Phase 6 hand evaluation implementation delay | High | Medium | Experienced developer assignment, external library review |
| Phase 7 test coverage goal not met | Medium | Medium | Priority adjustment, QA assignment |
| Phase 10 React UI design delay | Medium | High | Early designer assignment, UI library utilization |

### Personnel Risks

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| Key developer departure | High | Low | Documentation enhancement, pair programming |
| New team member adaptation delay | Medium | Medium | Onboarding process establishment, mentoring |

### Quality Risks

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| Multiple bug occurrences | High | Medium | Test coverage enhancement, code review |
| Performance degradation | Medium | Low | Early performance testing, profiling |

---

## Timeline Estimates

### v1.0 Release Timeline (Phase 6-7 completion)

```
Phase 6 completion:       4-5 weeks
Phase 7 testing:          2-3 weeks
Bug fixes & stabilization: 1-2 weeks
----------------------------------
Total estimated:          7-10 weeks
```

### v1.1 Release Timeline (Partial Phase 8)

```
Tasks 8.1-8.3:            2-3 weeks
----------------------------------
Total estimated:          2-3 weeks
```

---

## Appendix

### Terminology

- **Hexagonal Architecture**: Ports and Adapters pattern, abstracting connections with external systems
- **DDD (Domain-Driven Design)**: Domain-centric design, expressing business logic through domain models
- **Aggregate**: Cluster of entities and value objects with consistency boundary
- **Value Object**: Immutable object without identifier
- **Domain Event**: Significant occurrence in the domain
- **Use Case**: System behavior to achieve user goals
- **Port**: Interface between application and external world
- **Adapter**: Implementation connecting to external systems through Ports
- **MapStruct**: Compile-time object mapping tool

### References

- [Spring Boot 4.x Official Documentation](https://spring.io/projects/spring-boot)
- [Java 21 Release Notes](https://openjdk.org/projects/jdk/21/)
- [MapStruct Official Documentation](https://mapstruct.org/)
- [ArchUnit User Guide](https://www.archunit.org/userguide/html/000_Index.html)
- [TestContainers Official Documentation](https://www.testcontainers.org/)
- [Texas Hold'em Rules](https://www.pokernews.com/poker-rules/texas-holdem.htm)

---

This roadmap provides the complete implementation plan for the PokerHole Server project. Track progress through phase-by-phase checkboxes and follow detailed guides for systematic development.

## Related Projects

- [PokerHole CLI](https://github.com/bunnyholes/pokerhole-cli) - Go-based terminal client
