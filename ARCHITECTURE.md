# PokerHole Server Architecture

## Overview

PokerHole Server follows **Hexagonal Architecture** (Ports & Adapters pattern) combined with **Domain-Driven Design** principles. The architecture separates business logic from infrastructure concerns, making the system testable, maintainable, and extensible.

## System Architecture

### High-Level Design

```
┌─────────────────────────────────────────────────────────────┐
│                    SERVER (Java/Spring)                      │
│                                                              │
│  - Game Logic (Domain Model)                                │
│  - Matching System                                          │
│  - AI Players                                               │
│  - State Management (Room, Player)                          │
│  - Game Flow Control                                        │
│  - WebSocket Message Broadcasting                           │
│                                                              │
└──────────────────────┬───────────────────────────────────────┘
                       │
                  WebSocket
                  (JSON Protocol)
                       │
┌──────────────────────┴───────────────────────────────────────┐
│                   CLIENT (Go/Bubble Tea)                     │
│                                                              │
│  - TUI Rendering (Lipgloss)                                 │
│  - User Input Handling                                      │
│  - WebSocket Communication                                  │
│  - Local State Caching                                      │
│  - Animations and Effects                                   │
│                                                              │
└──────────────────────────────────────────────────────────────┘
```

### Layered Architecture

```
┌─────────────────────────────────────────────────┐
│           Adapter (In) - Driving               │
│        WebSocket, REST API (Future)             │
└──────────────────┬──────────────────────────────┘
                   │ uses
                   ↓
┌─────────────────────────────────────────────────┐
│              Application Layer                  │
│         Use Cases, Ports (in/out)               │
└──────────────────┬──────────────────────────────┘
                   │ uses
                   ↓
┌─────────────────────────────────────────────────┐
│                Domain Layer                     │
│   Card, Player, Game, Room (Pure Java)         │
│        Aggregates, Entities, VOs, Events        │
└──────────────────┬──────────────────────────────┘
                   │ defines needs
                   ↓
┌─────────────────────────────────────────────────┐
│          Adapter (Out) - Driven                │
│   Network, Persistence, Event, Notification     │
└─────────────────────────────────────────────────┘
```

## Package Structure

```
dev.xiyo.pokerhole/
├── core/
│   ├── domain/                    # Pure Java domain models
│   │   ├── card/                  # Card, Rank, Suit, Tier, Deck
│   │   ├── player/                # Player aggregate
│   │   │   └── vo/                # PlayerId, Nickname
│   │   ├── game/                  # Game domain
│   │   │   ├── event/             # GameEvent, RoundStarted, RoundEnded
│   │   │   └── vo/                # BettingRound, Pot, HandResult
│   │   ├── matching/              # Matching domain
│   │   │   ├── MatchingRequest, MatchingQueue
│   │   │   └── event/             # MatchingCompleted, MatchingTimeout
│   │   ├── ai/                    # AI domain
│   │   │   ├── AIPlayer, AIStrategy
│   │   │   └── AIDecision
│   │   └── shared/                # DomainEvent, AggregateRoot
│   │
│   ├── application/               # Use cases and ports
│   │   ├── UseCase.java           # Custom annotation
│   │   ├── mapper/                # MapStruct mappers (Domain <-> DTO)
│   │   │   ├── PlayerMapper.java
│   │   │   ├── GameMapper.java
│   │   │   └── MatchingMapper.java
│   │   └── port/
│   │       ├── in/                # Input ports (use cases)
│   │       │   ├── game/
│   │       │   ├── matching/
│   │       │   └── ai/
│   │       └── out/               # Output ports
│   │           ├── game/
│   │           ├── matching/
│   │           └── ai/
│   │
│   └── common/
│       └── exception/             # DomainException, GameException
│
├── adapter/
│   ├── in/                        # Input adapters
│   │   ├── websocket/             # WebSocket adapter
│   │   │   ├── GameWebSocketHandler.java
│   │   │   ├── message/
│   │   │   │   ├── ClientMessage.java
│   │   │   │   ├── ServerMessage.java
│   │   │   │   └── MessageCodec.java
│   │   │   └── session/
│   │   │       ├── WebSocketSessionRegistry.java
│   │   │       └── PlayerSession.java
│   │   └── web/                   # REST API (Future)
│   │       └── mapper/            # Request/Response mappers
│   │
│   └── out/                       # Output adapters
│       ├── network/               # WebSocket notification
│       ├── persistence/           # JPA entities, repositories
│       │   ├── jpa/
│       │   │   ├── entity/
│       │   │   ├── repository/
│       │   │   └── adapter/
│       │   └── mapper/            # JPA Entity <-> Domain (MapStruct)
│       │       ├── PlayerEntityMapper.java
│       │       ├── GameEntityMapper.java
│       │       └── RoomEntityMapper.java
│       ├── event/                 # Spring event publishing
│       ├── matching/              # In-memory matching queue
│       └── ai/                    # Rule-based AI strategy
│
├── configuration/
│   ├── PropertiesConfig.java
│   ├── WebSocketConfig.java
│   ├── MapperConfig.java          # MapStruct configuration
│   └── properties/
│       ├── GameProperties.java
│       ├── MatchingProperties.java
│       └── TerminalGatewayProperties.java
│
└── server/                        # Legacy (refactoring in progress)
    └── room/
```

## Dependency Rules

### 1. Domain Layer (core/domain)
- **No external dependencies** (Pure Java)
- Does not depend on other layers
- Exceptions: MapStruct, Lombok only

### 2. Application Layer (core/application)
- Depends on Domain Layer only
- Does not know about Adapters
- Defines Port interfaces

### 3. Adapter Layer (adapter)
- Depends on Application and Domain
- Implements Port interfaces
- Handles external systems

### 4. UI Layer (ui)
- Calls business logic through Application Ports only
- Does not access Domain internals

## Communication Protocol

### WebSocket Endpoints

- **Server**: `ws://localhost:8080/ws/game`
- **Protocol**: JSON over WebSocket

### Message Types

#### Client to Server

```java
enum ClientMessageType {
    // Connection
    REGISTER,           // Initial connection (UUID, Nickname)
    HEARTBEAT,          // Keep-alive

    // Matching
    JOIN_RANDOM_MATCH,  // Join random matching
    JOIN_CODE_MATCH,    // Join code-based matching
    CANCEL_MATCHING,    // Cancel matching

    // Game Actions
    CALL,               // Call
    RAISE,              // Raise
    FOLD,               // Fold
    CHECK,              // Check
    ALL_IN,             // All-in

    // Other
    LEAVE_GAME,         // Leave game
    CHAT_MESSAGE        // Chat
}
```

#### Server to Client

```java
enum ServerMessageType {
    // Connection
    REGISTER_SUCCESS,   // Registration success
    REGISTER_FAILURE,   // Registration failure

    // Matching
    MATCHING_STARTED,   // Matching started
    MATCHING_PROGRESS,  // Matching progress (waiting players)
    MATCHING_COMPLETED, // Matching completed
    MATCHING_CANCELLED, // Matching cancelled

    // Game State
    GAME_STARTED,       // Game started
    GAME_STATE_UPDATE,  // Game state update
    PLAYER_ACTION,      // Player action notification
    ROUND_COMPLETED,    // Round completed
    GAME_ENDED,         // Game ended

    // Errors
    ERROR,              // General error
    INVALID_ACTION      // Invalid action
}
```

### Message Structure

```json
{
  "type": "GAME_STATE_UPDATE",
  "timestamp": 1234567890,
  "payload": {
    "gameId": "game-123",
    "round": "FLOP",
    "pot": 1000,
    "currentBet": 200,
    "communityCards": ["AS", "KH", "QD"],
    "players": [
      {
        "id": "player-1",
        "nickname": "LuckyShark123",
        "chips": 8500,
        "bet": 200,
        "status": "ACTIVE",
        "position": 0
      }
    ],
    "currentPlayer": "player-2",
    "validActions": ["CALL", "RAISE", "FOLD"]
  }
}
```

## Client Architecture

### Go Client Package Structure

```
client/
├── cmd/
│   └── poker-client/
│       └── main.go                 # Entry point
│
├── internal/
│   ├── app/
│   │   └── app.go                  # Application initialization
│   │
│   ├── ui/                         # Bubble Tea UI
│   │   ├── model.go                # Overall UI model
│   │   ├── splash.go               # Splash screen
│   │   ├── menu.go                 # Menu screen
│   │   ├── matching.go             # Matching screen
│   │   ├── game.go                 # Game screen
│   │   └── styles.go               # Lipgloss styles
│   │
│   ├── network/                    # Network layer
│   │   ├── client.go               # WebSocket client
│   │   ├── messages.go             # Message types
│   │   ├── codec.go                # JSON encoding/decoding
│   │   └── heartbeat.go            # Heartbeat handling
│   │
│   ├── state/                      # State management
│   │   ├── game_state.go           # Game state
│   │   ├── player_state.go         # Player state
│   │   └── sync.go                 # Server synchronization
│   │
│   └── identity/                   # User identification
│       ├── uuid.go                 # UUID management
│       └── nickname.go             # Nickname generation
│
├── pkg/                            # Shared utilities
│   └── logger/
│       └── logger.go
│
├── go.mod
└── go.sum
```

## Design Patterns

### Domain-Driven Design (DDD)

1. **Aggregates**: Player, Game, Room, MatchingQueue
2. **Entities**: Player, Game
3. **Value Objects**: PlayerId, Nickname, Pot, HandResult, Card
4. **Domain Events**: RoundStarted, RoundEnded, MatchingCompleted
5. **Domain Services**: HandEvaluator, PotManager, BettingValidator

### Hexagonal Architecture

1. **Ports**: Input ports (use cases), Output ports (repositories, notifiers)
2. **Adapters**: WebSocket adapter, JPA persistence adapter, Event adapter
3. **Domain Core**: Pure business logic, framework-agnostic

### Object Mapping (MapStruct)

All object conversions use MapStruct:

1. **Persistence Mappers**: `adapter/out/persistence/mapper/` (JPA Entity <-> Domain)
2. **Application Mappers**: `core/application/mapper/` (Domain <-> DTO)
3. **Input Adapter Mappers**: `adapter/in/web/mapper/` (Request/Response <-> Command)

## Technology Stack

### Server
- **Framework**: Spring Boot 4.0.0-M3
- **Language**: Java 21 (Virtual Threads)
- **Build**: Gradle 9.x (Kotlin DSL)
- **Mapping**: MapStruct 1.6.3
- **Database**: PostgreSQL 16 (production), H2 (test)
- **Testing**: JUnit 5, TestContainers, ArchUnit

### Client
- **Language**: Go
- **TUI Framework**: Bubble Tea
- **Styling**: Lipgloss
- **WebSocket**: gorilla/websocket

## Quality Standards

### Architecture Rules
- Domain has no external dependencies (Pure Java)
- Application does not depend on Adapters
- No circular dependencies
- MapStruct mappers are located in clear layers

### Test Coverage Goals
- Domain Layer: 95%+
- Application Layer: 90%+
- Adapter Layer: 75%+
- Overall: 85%+

### Performance Targets
- Concurrent Users: 100+
- Matching Processing Time: < 1 second average
- Hand Evaluation Time: < 10ms
- Response Time: p95 < 500ms

## Migration History

### Phase 1: Server Cleanup (Completed)
- Removed SSH gateway (2,400+ lines)
- Removed server-side UI rendering
- Unified to WebSocket communication

### Phase 2: WebSocket Implementation (Completed)
- Implemented `/ws/game` endpoint
- Message codec (JSON serialization/deserialization)
- Session management (WebSocketSessionRegistry, PlayerSession)
- Integration tests passed

### Phase 3: Client Refactoring (Completed)
- Modularized Go package structure
- WebSocket client (auto-reconnect, heartbeat)
- Bubble Tea integration
- Thread-safe state synchronization

### Phase 4: Game Communication (In Progress)
- Game start flow
- Action handling (Call, Raise, Fold, etc.)
- State update broadcasting
- Game end processing

## References

For detailed implementation plans and roadmap, see:
- `ROADMAP.md` - Complete development roadmap
- `CONTRIBUTING.md` - Development guidelines and rules
- `CHANGELOG.md` - Version history and phase releases
