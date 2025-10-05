# Getting Started with PokerHole Server

This guide helps new team members understand the project structure and start contributing.

## Quick Navigation

| Document | Purpose | When to Read |
|----------|---------|--------------|
| [README.md](README.md) | Project overview, quick start, WebSocket protocol | First - Start here |
| [ARCHITECTURE.md](ARCHITECTURE.md) | Detailed architecture, component design, data flows | Second - Deep dive |
| [ROADMAP.md](ROADMAP.md) | Project phases, milestones, implementation status | Third - Planning |
| [SIDE_POT_IMPLEMENTATION.md](SIDE_POT_IMPLEMENTATION.md) | Side pot feature specification | When implementing side pots |

---

## Understanding the System in 5 Minutes

### What is PokerHole?
A **Texas Hold'em poker server** with real-time WebSocket communication.

### Key Components

```
┌─────────────┐    WebSocket     ┌─────────────┐    Delegates    ┌─────────────┐
│   Client    │ ←──────────────→ │  WebSocket  │ ──────────────→ │   Game      │
│   (TUI)     │   JSON Messages  │   Handler   │   Action/State  │  Command    │
└─────────────┘                  └─────────────┘                  └─────────────┘
                                                                          │
                                                                          ↓
                                                                  ┌─────────────┐
                                                                  │  GameRoom   │
                                                                  │  + Dealer   │
                                                                  └─────────────┘
```

### Game Flow

```
1. Client connects → REGISTER
2. Client joins match → JOIN_RANDOM_MATCH
3. Match complete → GAME_STARTED
4. Players take turns → CALL/RAISE/FOLD/CHECK/ALL_IN
5. Rounds progress → PRE_FLOP → FLOP → TURN → RIVER → SHOWDOWN
6. Winners determined → ROUND_COMPLETED
```

---

## Project Structure

### Most Important Files

| File | Lines | Purpose |
|------|-------|---------|
| `dealer/Dealer.java` | 936 | **Core game logic** - Texas Hold'em rules |
| `server/room/GameRoom.java` | 384 | **Room management** - Player coordination |
| `adapter/in/websocket/GameWebSocketHandler.java` | 350 | **WebSocket entry point** - Message routing |
| `adapter/in/websocket/service/GameCommandService.java` | 443 | **Action handler** - Game commands |
| `core/domain/game/HandEvaluatorImpl.java` | 300+ | **Hand evaluation** - Poker hand ranking |

### Directory Map

```
src/main/java/dev/xiyo/pokerhole/
│
├── core/domain/              ← Domain model (pure Java, no Spring)
│   ├── card/                 ← Card, Deck, Rank, Suit
│   ├── player/               ← Player aggregate
│   ├── game/                 ← Game rules, HandEvaluator
│   └── matching/             ← Matching domain
│
├── adapter/in/websocket/     ← WebSocket adapter (messages, sessions)
│   ├── message/              ← ClientMessage, ServerMessage types
│   ├── session/              ← Session registry
│   └── service/              ← GameCommandService, TurnTimeoutService
│
├── dealer/                   ← Game orchestration (Dealer.java)
├── server/room/              ← Room management (GameRoom.java)
└── configuration/            ← Spring configuration
```

---

## Current Implementation Status

### ✅ Working Features
- Texas Hold'em full game flow (PRE_FLOP → SHOWDOWN)
- All player actions (FOLD, CHECK, CALL, RAISE, ALL_IN)
- WebSocket real-time communication
- Player matching (random + code-based)
- Hand evaluation with 21 golden tests
- Turn timeout detection
- Chat functionality

### 🔄 Partial Implementation
- Event sourcing (structure exists, not used)
- Timeout auto-fold (detection only)

### ❌ Not Implemented Yet
- **Side pots** (all-in scenarios) ← High priority
- **Blind management** (small/big blind) ← High priority
- Reconnection handling
- Multi-table support
- Analytics & monitoring

---

## How to Run

### Prerequisites
- Java 23+
- Docker (for PostgreSQL)

### Steps

```bash
# 1. Start PostgreSQL
docker compose up -d

# 2. Run server
./gradlew bootRun

# 3. Server starts at http://localhost:8080
# WebSocket endpoint: ws://localhost:8080/ws/game
```

### Test WebSocket Connection

Using `wscat` (install: `npm install -g wscat`):

```bash
# Connect
wscat -c ws://localhost:8080/ws/game

# Register
{"type":"REGISTER","timestamp":1696454400000,"payload":{"uuid":"player-1","nickname":"Alice"}}

# Join random match
{"type":"JOIN_RANDOM_MATCH","timestamp":1696454401000,"payload":{}}
```

---

## How to Add a Feature

### Example: Implement Side Pots

1. **Understand the Domain**
   - Read `SIDE_POT_IMPLEMENTATION.md`
   - Study `core/domain/game/vo/SidePot.java`

2. **Write Tests First** (TDD)
   ```java
   @Test
   void should_create_side_pot_when_player_all_in() {
       // Given: 3 players, one goes all-in
       // When: Other players continue betting
       // Then: Side pot created
   }
   ```

3. **Implement in Domain Layer**
   - Modify `dealer/Dealer.java`
   - Add side pot calculation
   - Ensure pure logic (no framework deps)

4. **Update Adapters**
   - Update `GameCommandService` to broadcast side pot info
   - Update `GameRoom.getGameStateMap()` to include side pots

5. **Test Integration**
   - Run `./gradlew test`
   - Test manually via WebSocket

6. **Update Documentation**
   - Update implementation status in ROADMAP.md
   - Add to ARCHITECTURE.md if needed

---

## Common Tasks

### Run Tests
```bash
# All tests
./gradlew test

# Specific test
./gradlew test --tests TexasHoldemIntegrationTest

# With coverage
./gradlew test jacocoTestReport
```

### Build JAR
```bash
./gradlew build
# Output: build/libs/pokerhole-server-*.jar
```

### Check Architecture Rules
```bash
./gradlew test --tests "*ArchitectureTest*"
```

### Debug WebSocket Messages
Add logging in `GameWebSocketHandler`:
```java
log.debug("Received: {}", message.getPayload());
```

Then check logs:
```bash
tail -f logs/pokerhole-server.log
```

---

## Key Design Patterns

### 1. Hexagonal Architecture
- **Domain** (core) is isolated from infrastructure
- **Ports** define interfaces (in/out)
- **Adapters** implement ports (WebSocket, JPA, etc.)

### 2. Event Sourcing (Partial)
- Game events emitted: `GameStarted`, `PlayerActed`, etc.
- Events stored (structure ready, not persisted yet)
- Enables audit trail and replay

### 3. CQRS (Light)
- Commands: Player actions (CALL, RAISE, etc.)
- Queries: Game state (via `getGameStateMap()`)

---

## Debugging Tips

### Issue: Build Fails
```bash
./gradlew clean build --refresh-dependencies
```

### Issue: Port 8080 Already in Use
```bash
lsof -i :8080
kill -9 <PID>
```

### Issue: Tests Failing
1. Check logs: `cat test-results.log`
2. Run specific test with `--info`:
   ```bash
   ./gradlew test --tests "TexasHoldemIntegrationTest" --info
   ```

### Issue: WebSocket Connection Refused
1. Check server is running: `curl http://localhost:8080/actuator/health`
2. Check WebSocket endpoint: Use browser DevTools or `wscat`

---

## Code Style Guidelines

### Naming Conventions
- Use Cases: `*UseCase` interface (e.g., `PlaceBetUseCase`)
- Ports: `*Port` interface (e.g., `GameRepositoryPort`)
- Adapters: `*Adapter` class (e.g., `GameJpaAdapter`)
- Domain Events: Past tense (e.g., `GameStarted`, `PlayerActed`)

### Immutability
- Value Objects: Use Java `record` or `@Value` (Lombok)
- Domain Events: Always immutable
- Aggregates: Mutable but encapsulated

### Null Safety
- Avoid returning `null`
- Use `Optional<T>` for optional results
- Validate with `Objects.requireNonNull()`

---

## Getting Help

### Documentation
- **Architecture questions**: See [ARCHITECTURE.md](ARCHITECTURE.md)
- **Feature planning**: See [ROADMAP.md](ROADMAP.md)
- **Quick reference**: See [README.md](README.md)

### Code Navigation
- **Domain model**: `src/main/java/dev/xiyo/pokerhole/core/domain/`
- **WebSocket**: `src/main/java/dev/xiyo/pokerhole/adapter/in/websocket/`
- **Game logic**: `src/main/java/dev/xiyo/pokerhole/dealer/Dealer.java`

### Testing
- **Integration tests**: `src/test/java/dev/xiyo/pokerhole/dealer/TexasHoldemIntegrationTest.java`
- **Hand evaluator tests**: `src/test/java/dev/xiyo/pokerhole/core/domain/game/HandEvaluatorTest.java`

---

## Next Steps

1. ✅ Read this guide
2. ✅ Read [README.md](README.md)
3. ✅ Run the server locally
4. ✅ Test WebSocket connection
5. ✅ Read [ARCHITECTURE.md](ARCHITECTURE.md)
6. ✅ Review [ROADMAP.md](ROADMAP.md)
7. ✅ Pick a task from roadmap (suggest: implement side pots)
8. ✅ Write tests first
9. ✅ Implement feature
10. ✅ Submit pull request

---

## Contributing

### Pull Request Checklist
- [ ] Tests added and passing
- [ ] Architecture tests pass
- [ ] No Spring dependencies in domain layer
- [ ] Javadoc for public APIs
- [ ] ROADMAP.md updated (implementation status)
- [ ] README.md updated (if protocol/API changed)

### Code Review Focus
1. Does it follow hexagonal architecture?
2. Is domain logic pure (no framework deps)?
3. Are tests comprehensive?
4. Is the code readable and maintainable?

---

**Welcome to the team! Let's build a great poker game together! 🎮🃏**
