# PokerHole Server - Project Roadmap

## Overview
PokerHole is a Texas Hold'em poker server implementation using Hexagonal Architecture, Event Sourcing, and CQRS patterns. This roadmap outlines the project phases, milestones, and implementation status.

## Project Status
**Current Phase**: Phase 1 Complete (87.5% - Steps 1-7 of 8)
**Last Updated**: 2025-10-04
**Test Coverage**: 49 tests, 100% pass rate ✅

---

## Phase 1: Core Game Logic & WebSocket Communication ✅

### Step 1: Domain Model Foundation ✅
**Status**: Complete
**Completed**: 2025-09-25

- ✅ Card domain (Card, Deck, Rank, Suit)
- ✅ Hand evaluation (HandEvaluator with 21 golden test vectors)
- ✅ Player aggregate (Player, PlayerId, Chips, Status)
- ✅ Domain events structure (DomainEvent interface)
- ✅ Value objects (immutable with records)

**Key Files**:
- `core/domain/card/` - Card, Deck, Rank, Suit, Tier
- `core/domain/player/` - Player, PlayerId, Nickname, PlayerStatus
- `core/domain/game/HandEvaluator.java` - Hand evaluation logic

### Step 2: Texas Hold'em Game Rules ✅
**Status**: Complete
**Completed**: 2025-09-28

- ✅ Betting rounds (PRE_FLOP, FLOP, TURN, RIVER, SHOWDOWN)
- ✅ Player actions (FOLD, CHECK, CALL, RAISE, ALL_IN)
- ✅ Community cards dealing (0 → 3 → 4 → 5 cards)
- ✅ Turn management and rotation
- ✅ Pot management (basic pot accumulation)

**Key Files**:
- `dealer/Dealer.java` - Main game orchestration (936 lines)
- `core/domain/game/vo/BettingRound.java` - Round states
- `core/domain/game/vo/PlayerAction.java` - Action types

### Step 3: Hexagonal Architecture Setup ✅
**Status**: Complete
**Completed**: 2025-09-20

- ✅ Port & Adapter pattern implementation
- ✅ Application layer (Use Cases, Ports)
- ✅ Domain isolation (zero framework dependencies)
- ✅ ArchUnit tests for architecture validation

**Structure**:
```
core/
├── domain/         # Pure Java domain (no Spring)
└── application/    # Use Cases, Ports (in/out)

adapter/
├── in/            # Input adapters (WebSocket, REST)
└── out/           # Output adapters (JPA, Event Store)
```

### Step 4: WebSocket Protocol Design ✅
**Status**: Complete
**Completed**: 2025-09-30

- ✅ Client → Server message types (12 types)
- ✅ Server → Client message types (16 types)
- ✅ JSON message encoding/decoding
- ✅ Session management and registry
- ✅ Error handling and validation

**Key Components**:
- `adapter/in/websocket/message/ClientMessageType.java`
- `adapter/in/websocket/message/ServerMessageType.java`
- `adapter/in/websocket/GameWebSocketHandler.java`

### Step 5: Matching System ✅
**Status**: Complete
**Completed**: 2025-10-01

- ✅ Random matching queue
- ✅ Code-based matching (6-digit codes)
- ✅ Matching notifications (progress, completion)
- ✅ Automatic game start when queue full
- ✅ AI player integration (RuleBasedAI)

**Key Files**:
- `core/application/service/MatchingService.java`
- `adapter/out/matching/InMemoryMatchingAdapter.java`
- `adapter/out/ai/RuleBasedAIStrategy.java`

### Step 6: Texas Hold'em Integration Tests ✅
**Status**: Complete
**Completed**: 2025-10-02

- ✅ 18 comprehensive integration tests
- ✅ Game start scenarios (3 tests)
- ✅ Player action validation (6 tests)
- ✅ Round progression tests (2 tests)
- ✅ Two-player game flows (3 tests)
- ✅ Error case handling (4 tests)

**Critical Bugs Fixed**:
1. Betting round completion logic (all ACTIVE players check)
2. CHECK action recording in currentRoundBets

**Test File**: `test/java/dev/xiyo/pokerhole/dealer/TexasHoldemIntegrationTest.java`

### Step 7: WebSocket Protocol Updates ✅
**Status**: Complete
**Completed**: 2025-10-03

- ✅ Protocol field naming improvements
- ✅ Community card format optimization (70% smaller payload)
- ✅ Turn timeout system (configurable seconds)
- ✅ Real-time broadcasting (action, state, timeout)
- ✅ Client-server protocol synchronization

**Changes**:
- `roomId` → `gameId`
- `currentTurnPlayer` → `currentPlayer`
- Player `currentBet` → `bet`
- Cards: `[{"suit":"HEARTS","rank":"ACE"}]` → `["AH"]`

### Step 8: Documentation Finalization 🔄
**Status**: In Progress (This document)
**Target**: 2025-10-05

- ✅ ROADMAP.md creation
- 🔄 ARCHITECTURE.md (Server + Client design)
- 🔄 Component interaction diagrams
- 🔄 Public API documentation
- 🔄 WebSocket message flow diagrams

---

## Phase 2: Production Readiness Features 📋

### Step 9: Side Pot Implementation
**Status**: Not Started
**Priority**: High

- [ ] Side pot calculation for all-in scenarios
- [ ] Multiple side pots support (2+ players all-in)
- [ ] Side pot winner determination
- [ ] Side pot distribution tests

**Reference**: See `SIDE_POT_IMPLEMENTATION.md` for detailed spec

### Step 10: Blind Management
**Status**: Not Started
**Priority**: High

- [ ] Small blind / Big blind enforcement
- [ ] Dealer button rotation per hand
- [ ] Blind posting at round start
- [ ] Blind amount configuration

### Step 11: Timeout & Disconnect Handling
**Status**: Partial (timeout started)
**Priority**: Medium

- ✅ Turn timeout detection (Step 7)
- [ ] Automatic fold on timeout
- [ ] Disconnect handling (reconnection grace period)
- [ ] Session recovery mechanism

### Step 12: Event Sourcing Completion
**Status**: Structure Ready, Not Used
**Priority**: Medium

- [ ] Event store persistence (PostgreSQL)
- [ ] Event replay mechanism
- [ ] Snapshot generation for optimization
- [ ] Event versioning strategy

---

## Phase 3: Enhanced Features 📋

### Step 13: Multi-Table Support
**Status**: Not Started
**Priority**: Medium

- [ ] Multiple concurrent game rooms
- [ ] Room lobby and listing
- [ ] Room creation/join/leave
- [ ] Observer mode (spectators)

### Step 14: Tournament Mode
**Status**: Not Started
**Priority**: Low

- [ ] Tournament structure (knockout, rebuy)
- [ ] Blind level escalation
- [ ] Prize pool calculation
- [ ] Leaderboard tracking

### Step 15: Advanced AI Players
**Status**: Basic RuleBasedAI Complete
**Priority**: Low

- ✅ RuleBasedAI (conservative strategy)
- [ ] Aggressive AI strategy
- [ ] Bluffing behavior
- [ ] Hand strength calculation
- [ ] Pot odds analysis

### Step 16: Analytics & Monitoring
**Status**: Not Started
**Priority**: Medium

- [ ] Game statistics (hands played, win rate)
- [ ] Player performance metrics
- [ ] Prometheus metrics endpoint
- [ ] Grafana dashboards
- [ ] Error tracking (Sentry integration)

---

## Phase 4: Client Integration 📋

### Step 17: REST API (Optional)
**Status**: Not Started
**Priority**: Low

- [ ] Player profile management
- [ ] Game history queries
- [ ] Leaderboard API
- [ ] OpenAPI/Swagger documentation

### Step 18: Client SDK
**Status**: Not Started
**Priority**: High (for CLI client)

- [ ] Go client library (for pokerhole-cli)
- [ ] JavaScript/TypeScript SDK (future web client)
- [ ] WebSocket connection helper
- [ ] Message serialization/deserialization
- [ ] Reconnection logic

### Step 19: TUI Client (Go)
**Status**: Separate Repository
**Priority**: High

See: `pokerhole-cli` repository

- [ ] Terminal UI with Bubble Tea
- [ ] Game state rendering
- [ ] Player action input
- [ ] Chat functionality
- [ ] Hand history display

---

## Technical Debt & Improvements

### Code Quality
- [ ] Increase test coverage to 85%+ (currently ~60%)
- [ ] Add more edge case tests
- [ ] Refactor GameRoom class (currently 384 lines)
- [ ] Extract WebSocket broadcasting to separate service

### Performance
- [ ] Load testing (100+ concurrent users)
- [ ] Database connection pooling optimization
- [ ] WebSocket message batching
- [ ] Hand evaluation caching

### Security
- [ ] Authentication (JWT tokens)
- [ ] Authorization (player actions validation)
- [ ] Rate limiting (prevent spam)
- [ ] Input sanitization (SQL injection, XSS)

### DevOps
- [ ] CI/CD pipeline (GitHub Actions)
- [ ] Docker Compose for local development
- [ ] Kubernetes deployment manifests
- [ ] Production environment setup

---

## Milestones

### Milestone 1: Core Game Working ✅
**Target**: 2025-10-05
**Status**: 87.5% Complete (7/8 steps)

- ✅ Full Texas Hold'em game flow
- ✅ WebSocket real-time communication
- ✅ Player matching system
- 🔄 Documentation (in progress)

### Milestone 2: Production Ready 📋
**Target**: 2025-10-31
**Status**: Not Started

- [ ] Side pots
- [ ] Blind management
- [ ] Timeout handling
- [ ] Event sourcing active
- [ ] 90%+ test coverage

### Milestone 3: Public Beta 📋
**Target**: 2025-11-30
**Status**: Not Started

- [ ] Multi-table support
- [ ] Client SDK released
- [ ] TUI client working
- [ ] Analytics & monitoring
- [ ] Security hardening

---

## Dependencies & Blockers

### Current Blockers
1. **Side Pot Logic**: Complex calculation needs careful testing
2. **Event Sourcing**: Not fully utilized yet (structure exists)
3. **Client SDK**: Needed for CLI client development

### External Dependencies
- PostgreSQL 16+ (database)
- Java 23+ (virtual threads)
- Spring Boot 3.4.0 (framework)
- Go 1.21+ (for CLI client)

---

## Success Metrics

### Technical Metrics
- ✅ Build success rate: 100%
- ✅ Test pass rate: 100% (49/49 tests)
- ⚠️ Code coverage: ~60% (target: 85%)
- ⏳ Response time: <100ms (not yet measured)

### Feature Completeness
- Phase 1: 87.5% (7/8 steps)
- Phase 2: 0% (0/4 steps)
- Phase 3: 12.5% (1/8 steps - RuleBasedAI only)
- Phase 4: 0% (0/3 steps)

### Game Functionality
- ✅ Deal cards
- ✅ Betting rounds (all 5 rounds)
- ✅ Player actions (all 5 actions)
- ✅ Winner determination
- ❌ Side pots (not implemented)
- ❌ Blinds (not enforced)
- ⚠️ Timeout (detection only, no auto-fold)

---

## Contributing

### How to Add a New Feature
1. Create an issue with feature description
2. Design domain model changes (if needed)
3. Write tests first (TDD approach)
4. Implement in domain layer
5. Add application use case
6. Create adapters (WebSocket, persistence)
7. Update documentation
8. Submit pull request

### Code Review Checklist
- [ ] Tests added and passing
- [ ] Architecture rules enforced (ArchUnit)
- [ ] No Spring dependencies in domain
- [ ] Javadoc for public APIs
- [ ] CHANGELOG.md updated
- [ ] README.md updated (if needed)

---

## Version History

| Version | Date | Changes | Status |
|---------|------|---------|--------|
| 0.1.0 | 2025-09-20 | Initial project setup | ✅ |
| 0.2.0 | 2025-09-25 | Domain model complete | ✅ |
| 0.3.0 | 2025-09-30 | WebSocket protocol | ✅ |
| 0.4.0 | 2025-10-02 | Texas Hold'em gameplay | ✅ |
| 0.5.0 | 2025-10-04 | Protocol optimization | ✅ |
| 0.6.0 | 2025-10-05 | Documentation (target) | 🔄 |
| 1.0.0 | 2025-10-31 | Production ready (target) | 📋 |

---

## Resources

### Documentation
- [README.md](README.md) - Project overview and quick start
- [ARCHITECTURE.md](ARCHITECTURE.md) - Detailed architecture design
- [SIDE_POT_IMPLEMENTATION.md](SIDE_POT_IMPLEMENTATION.md) - Side pot specification

### External Links
- [Texas Hold'em Rules](https://www.pokernews.com/poker-rules/texas-holdem.htm)
- [Hexagonal Architecture](https://alistair.cockburn.us/hexagonal-architecture/)
- [Event Sourcing](https://martinfowler.com/eaaDev/EventSourcing.html)
- [WebSocket Protocol](https://datatracker.ietf.org/doc/html/rfc6455)

---

## Contact & Support

**Repository**: https://github.com/bunnyholes/pokerhole-server
**Issues**: https://github.com/bunnyholes/pokerhole-server/issues
**Discord**: (TBD)

---

## License
MIT License - See LICENSE file for details
