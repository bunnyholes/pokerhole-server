# Changelog

All notable changes to the PokerHole Server project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [Unreleased]

### Phase 6: Poker Core Logic Implementation (In Progress)

#### Task 6.1 - Hand Evaluation System (2025-10-02)

**Added**
- `HandEvaluator` interface for poker hand evaluation
- `HandEvaluatorImpl` implementation with 7-card combination algorithm
- `HandResult` Value Object for hand evaluation results
- Comprehensive hand ranking comparison logic
- Support for all poker hand types (Royal Flush to High Card)

**Technical Details**
- Production code: 267 lines
- Test code: 506 lines (25 test cases, 100% pass)
- Files created: 3 production, 1 test
- Algorithm: C(7,5) = 21 combinations for best 5-card hand selection

**Tests**
- All hand types validation (Royal Flush, Straight Flush, Four of a Kind, etc.)
- 7-card combination selection tests
- Hand comparison logic tests
- Edge cases (Ace-low straight, duplicate cards)
- Input validation tests

---

## [Phase 5] - Game Logic Integration (Partial Complete)

### 2025-09 to 2025-10

**Added**
- Round start logic
- Card distribution system
- Round end logic
- Basic game flow control

**Pending**
- Room management (Legacy code integration)
- Game state persistence
- Complete game lifecycle

---

## [Phase 4] - Terminal UI and Game Flow (Completed)

### 2025-07 to 2025-08

**Added**
- Banner system with auto-display
- Interactive menu system with arrow key navigation
- Random/Code matching system
- AI player system with 3 strategies (Conservative, Aggressive, Random)
- Game session lifecycle management
- Matching timeout handling (10 seconds)
- AI player auto-deployment on timeout

**Technical Details**
- Menu options: Random Match, Code Match, Game Manual, About
- Matching flow: Wait → Match (4 players) or Timeout → AI Fill → Start
- Game exit: q key (reserve), c key (cancel reservation)

---

## [Phase 3] - Adapter Layer Implementation (Completed)

### 2025-05 to 2025-06

**Added**

#### WebSocket Adapter
- `GameWebSocketHandler` - Protocol-based WebSocket handler (245 lines)
- `PlayerSession` - Session wrapper with player metadata
- `WebSocketSessionRegistry` - Thread-safe session registry
- JSON message protocol (ClientMessage, ServerMessage)
- `MessageCodec` - Jackson-based serialization

#### Message Types
- Client to Server: REGISTER, HEARTBEAT, JOIN_RANDOM_MATCH, CALL, RAISE, FOLD, etc. (13 types)
- Server to Client: REGISTER_SUCCESS, GAME_STATE_UPDATE, PLAYER_ACTION, etc. (13 types)

#### JPA Persistence Adapter
- Entity classes for Player, Game, Room
- Spring Data JPA repositories
- Persistence adapters implementing output ports

#### Event Adapter
- Spring event publishing
- Domain event handlers

#### Matching Adapter
- In-memory matching queue
- Timeout detection

#### AI Adapter
- Rule-based AI strategy implementation
- Decision-making algorithms for 3 personalities

**Technical Details**
- Server production code: ~850 lines
- Integration tests: WebSocket protocol verified end-to-end
- Endpoints: `/ws/game` (new), `/ws/terminal` (legacy compatibility)

---

## [Phase 2] - Core Domain Model Implementation (Completed)

### 2025-03 to 2025-04

**Added**

#### Card Domain
- `Card`, `Rank`, `Suit`, `Tier` entities
- `Deck` and `Hand` aggregates
- Card shuffling and dealing logic

#### Player Domain
- `Player` aggregate root
- `PlayerId`, `Nickname` Value Objects
- `PlayerRecord` entity for win/loss tracking
- Chip management methods

#### Game Domain
- `GameId`, `Pot`, `BettingRound` Value Objects
- `GameState` enum (WAITING, PLAYING, FINISHED)
- `GameEvent` sealed interface
- `RoundStarted`, `RoundEnded` events

#### Matching Domain
- `MatchingType` enum (RANDOM, CODE)
- `MatchingCode` Value Object (6-digit code)
- `MatchingRequest`, `MatchingQueue` aggregates
- `MatchingCompleted`, `MatchingTimeout` events

#### AI Domain
- `AIStrategyType` enum (CONSERVATIVE, AGGRESSIVE, RANDOM)
- `AIDecision` Value Object
- `AIPlayer` entity
- `AIStrategy` interface

#### Shared Domain
- `DomainEvent` interface
- `AggregateRoot` abstract class
- `DomainException`, `GameException` classes

**Technical Details**
- Pure Java implementation (no external dependencies except Lombok, MapStruct)
- All domain logic testable in isolation
- Immutable Value Objects
- Rich domain models with business rules

---

## [Phase 1] - Infrastructure & Architecture Foundation (Completed)

### 2025-01 to 2025-02

**Added**
- Gradle Kotlin DSL build configuration
- Spring Boot 4.0.0-M3 setup
- Java 21 Toolchain with Virtual Threads
- Hexagonal Architecture package structure
- ArchUnit architecture tests
- MapStruct configuration for object mapping
- Docker Compose setup for PostgreSQL
- Test infrastructure (JUnit 5, TestContainers, ArchUnit)

**Changed**
- Migrated from Groovy to Kotlin DSL for build scripts
- Upgraded to Spring Boot 4.x (Milestone)
- Updated to Java 21 LTS

**Technical Details**
- Build system: Gradle 9.x
- Architecture: Hexagonal (Ports & Adapters)
- Design: Domain-Driven Design (DDD)
- Testing: ArchUnit for architecture validation

---

## [Refactoring Phase 1] - Protocol and UI Cleanup (Completed)

### 2025-10-01

**Removed**
- SSH infrastructure (port 2222)
  - Apache SSHD server implementation
  - SSH configuration properties
  - `sshd-core` and `sshd-common` dependencies
- TCP Terminal server (port 7777)
  - TCP server implementation
  - Terminal gateway configuration
- Server-side UI rendering (~1,500 lines)
  - `BannerRenderer`, `MenuRenderer`, `PokerCliRenderer`
  - `PokerCliViewModel`, `PokerCliTableState`, `PokerCliSeatState`
  - Terminal UI controllers and handlers
  - Terminal session state management
- JLine dependencies (terminal UI library)

**Changed**
- Server now focused solely on game logic and WebSocket communication
- All UI rendering moved to client side

**Statistics**
- Files deleted: 34
- Lines removed: ~2,465
- Lines added: ~22
- Net reduction: ~2,443 lines

**Impact**
- Freed ports: 2222 (SSH), 7777 (TCP)
- Eliminated SSH performance overhead
- Simplified network layer
- Reduced server CPU usage (no rendering)

---

## [Refactoring Phase 2-3] - WebSocket Protocol and Client (Completed)

### 2025-10-01

**Added**

#### Server Side
- Complete WebSocket protocol stack
- JSON message encoding/decoding
- Session management with UUID tracking
- Heartbeat mechanism
- Error handling and validation
- Backward-compatible legacy endpoint

#### Client Side (Go)
- Modular package structure
  - `cmd/poker-client` - Entry point
  - `internal/identity` - UUID persistence, nickname generation
  - `internal/network` - WebSocket client with auto-reconnect
  - `internal/state` - Thread-safe game state management
  - `internal/ui` - Bubble Tea TUI integration
- Persistent UUID storage (~/.pokerhole/uuid)
- Random nickname generation
- Automatic reconnection logic
- Protocol matching server implementation

**Changed**
- Client architecture: Monolithic 532 lines → 5 modular files (650+ lines)
- Protocol: Text-based → JSON-based
- Communication: Request-response → Bidirectional real-time

**Technical Details**
- Server: ~850 new lines (10 files)
- Client: ~650 new lines (6 files)
- Integration tests: All passing
- Build status: Clean compilation, no errors

**Verified**
- WebSocket connection establishment
- REGISTER flow (client → server → response)
- Session registration and cleanup
- Heartbeat mechanism
- JSON serialization/deserialization

---

## Future Releases

See [ROADMAP.md](ROADMAP.md) for detailed future plans.

### v1.0 (2026-06) - MVP
- Complete Phase 6: Poker core logic
- Complete Phase 7: Testing and quality assurance
- Complete Texas Hold'em rules
- 85%+ test coverage
- Support 100+ concurrent users

### v1.1 (2026-09)
- Spectator mode
- Chat system
- Leaderboard and statistics

### v1.2 (2026-12)
- Tournament mode
- Performance optimization
- Security enhancement
- Monitoring and logging

### v2.0 (2027-06)
- REST API
- React web UI
- Mobile-responsive design

### v2.1 (2027-12)
- Mobile app (React Native)
- Social features (friends, invites)
- Social login (Google, Facebook, Apple)

### v3.0 (2028-06)
- Machine learning AI players
- Multi-region support
- CDN integration
- Multi-language support

---

## References

- [ROADMAP.md](ROADMAP.md) - Complete development roadmap
- [ARCHITECTURE.md](ARCHITECTURE.md) - Architecture documentation
- [CONTRIBUTING.md](CONTRIBUTING.md) - Development guidelines
