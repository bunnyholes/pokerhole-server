# PokerHole - Architecture Documentation

## Table of Contents
1. [Overview](#overview)
2. [Architecture Patterns](#architecture-patterns)
3. [Server Architecture](#server-architecture)
4. [Domain Model](#domain-model)
5. [WebSocket Protocol](#websocket-protocol)
6. [Client Architecture](#client-architecture)
7. [Data Flow](#data-flow)
8. [Component Interactions](#component-interactions)
9. [Public APIs](#public-apis)
10. [Implementation Status](#implementation-status)

---

## Overview

PokerHole is a **Texas Hold'em poker game** implementation with:
- **Server**: Java/Spring Boot backend using Hexagonal Architecture + Event Sourcing
- **Client**: Go TUI (Terminal UI) application using Bubble Tea framework
- **Communication**: WebSocket-based real-time bidirectional messaging

### Key Design Goals
1. **Separation of Concerns**: Domain logic isolated from framework dependencies
2. **Event Sourcing**: All game state changes captured as immutable events
3. **Real-time Communication**: WebSocket for low-latency game updates
4. **Testability**: Pure domain logic with comprehensive test coverage
5. **Extensibility**: Easy to add new game modes and features

---

## Architecture Patterns

### Hexagonal Architecture (Ports & Adapters)

```
┌─────────────────────────────────────────────────────────────┐
│                    Input Adapters (Driving Side)            │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │  WebSocket   │  │  REST API    │  │   Terminal   │     │
│  │   Handler    │  │ (future)     │  │   Commands   │     │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘     │
└─────────┼──────────────────┼──────────────────┼─────────────┘
          │                  │                  │
          ↓                  ↓                  ↓
┌─────────────────────────────────────────────────────────────┐
│                    Application Layer                        │
│  ┌──────────────────────────────────────────────────────┐  │
│  │           Use Cases (Port IN interfaces)             │  │
│  │  • JoinRandomMatchingUseCase                         │  │
│  │  • ExecuteGameActionUseCase                          │  │
│  │  • StartGameUseCase                                  │  │
│  └──────────────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────────┐  │
│  │              Port OUT interfaces                     │  │
│  │  • MatchingQueuePort                                 │  │
│  │  • EventStorePort                                    │  │
│  │  • GameRepositoryPort                                │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
          │                                        │
          ↓                                        ↓
┌─────────────────────────────────────────────────────────────┐
│                      Domain Layer                           │
│            (Pure Java - Zero Framework Dependencies)        │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │  Aggregates  │  │Value Objects │  │   Services   │     │
│  │  • Game      │  │  • Card      │  │• HandEval.   │     │
│  │  • Player    │  │  • Deck      │  │• PotDistrib. │     │
│  │  • Matching  │  │  • Pot       │  │• WinnerRes.  │     │
│  └──────────────┘  └──────────────┘  └──────────────┘     │
│  ┌──────────────────────────────────────────────────────┐  │
│  │              Domain Events                           │  │
│  │  • GameStarted, PlayerActed, RoundProgressed         │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
          │                                        │
          ↓                                        ↓
┌─────────────────────────────────────────────────────────────┐
│                   Output Adapters (Driven Side)             │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │  PostgreSQL  │  │ Event Store  │  │  AI Player   │     │
│  │   (JPA)      │  │  Adapter     │  │   Strategy   │     │
│  └──────────────┘  └──────────────┘  └──────────────┘     │
└─────────────────────────────────────────────────────────────┘
```

### Event Sourcing Pattern

All game state changes are captured as **immutable domain events**:

```
┌──────────────┐         ┌──────────────┐         ┌──────────────┐
│   Command    │────────>│   Aggregate  │────────>│   Events     │
│  (Action)    │         │   (Game)     │         │  (Stored)    │
└──────────────┘         └──────────────┘         └──────────────┘
                                │                          │
                                │                          │
                                ↓                          ↓
                         ┌──────────────┐         ┌──────────────┐
                         │  State Update│         │  Event Store │
                         │  (In Memory) │         │ (PostgreSQL) │
                         └──────────────┘         └──────────────┘
```

**Benefits**:
- Complete audit trail of all actions
- Ability to replay game from any point
- Time travel debugging
- Analytics and statistics generation

---

## Server Architecture

### Package Structure

```
src/main/java/dev/xiyo/pokerhole/
│
├── core/                           # Core domain and application logic
│   ├── domain/                     # Pure Java domain (no Spring)
│   │   ├── card/                   # Card, Deck, Rank, Suit, Tier
│   │   │   ├── Card.java           # Immutable card (Rank + Suit)
│   │   │   ├── Deck.java           # 52-card deck with shuffle
│   │   │   ├── Rank.java           # A, K, Q, J, 10...2
│   │   │   ├── Suit.java           # SPADES, HEARTS, DIAMONDS, CLUBS
│   │   │   ├── Tier.java           # Hand rankings (Royal Flush → High Card)
│   │   │   └── Hand.java           # Player's 5-card hand
│   │   │
│   │   ├── player/                 # Player aggregate
│   │   │   ├── Player.java         # Player entity (chips, status, hand)
│   │   │   ├── PlayerRecord.java   # Player statistics
│   │   │   └── vo/
│   │   │       ├── PlayerId.java   # UUID-based player ID
│   │   │       ├── Nickname.java   # Display name
│   │   │       └── PlayerStatus.java # ACTIVE, FOLDED, ALL_IN, etc.
│   │   │
│   │   ├── game/                   # Game aggregate and rules
│   │   │   ├── GameId.java         # UUID-based game ID
│   │   │   ├── GameState.java      # Game state machine
│   │   │   ├── HandEvaluator.java  # Interface for hand evaluation
│   │   │   ├── HandEvaluatorImpl.java # 21 golden test vectors
│   │   │   ├── PotDistributor.java # Pot distribution logic
│   │   │   ├── WinnerResolver.java # Winner determination
│   │   │   ├── event/              # Domain events
│   │   │   │   ├── GameEvent.java  # Base event interface
│   │   │   │   ├── RoundStarted.java
│   │   │   │   ├── PlayerActed.java
│   │   │   │   ├── RoundProgressed.java
│   │   │   │   └── RoundEnded.java
│   │   │   └── vo/                 # Value objects
│   │   │       ├── BettingRound.java # PRE_FLOP, FLOP, TURN, RIVER, SHOWDOWN
│   │   │       ├── PlayerAction.java # FOLD, CHECK, CALL, RAISE, ALL_IN
│   │   │       ├── HandResult.java   # Tier + kickers
│   │   │       ├── Pot.java          # Main pot amount
│   │   │       └── SidePot.java      # Side pot for all-in
│   │   │
│   │   ├── matching/               # Matching domain
│   │   │   ├── MatchingQueue.java  # Queue for player matching
│   │   │   ├── MatchingRequest.java # Player matching request
│   │   │   ├── MatchingCode.java   # 6-digit room code
│   │   │   └── MatchingType.java   # RANDOM, CODE
│   │   │
│   │   ├── ai/                     # AI player domain
│   │   │   ├── AIPlayer.java       # AI player entity
│   │   │   ├── AIStrategy.java     # AI strategy interface
│   │   │   └── AIDecision.java     # AI action decision
│   │   │
│   │   └── shared/                 # Shared domain concepts
│   │       ├── DomainEvent.java    # Base domain event
│   │       └── AggregateRoot.java  # Base aggregate root
│   │
│   └── application/                # Application services (use cases)
│       ├── port/                   # Port interfaces
│       │   ├── in/                 # Input ports (use cases)
│       │   │   ├── game/
│       │   │   │   ├── StartGameUseCase.java
│       │   │   │   └── StartRoundUseCase.java
│       │   │   ├── matching/
│       │   │   │   ├── JoinRandomMatchingUseCase.java
│       │   │   │   ├── JoinCodeMatchingUseCase.java
│       │   │   │   └── CancelMatchingUseCase.java
│       │   │   └── ai/
│       │   │       └── RequestAIPlayerUseCase.java
│       │   │
│       │   └── out/                # Output ports
│       │       ├── EventPublisher.java
│       │       ├── matching/
│       │       │   ├── MatchingQueuePort.java
│       │       │   └── MatchingNotificationPort.java
│       │       └── ai/
│       │           └── AIStrategyPort.java
│       │
│       └── service/                # Use case implementations
│           ├── MatchingService.java      # Matching logic
│           └── AIPlayerService.java      # AI player management
│
├── adapter/                        # Adapters (infrastructure)
│   ├── in/                         # Input adapters (driving)
│   │   ├── websocket/              # WebSocket adapter
│   │   │   ├── GameWebSocketHandler.java # Main WebSocket handler
│   │   │   ├── message/            # Message types
│   │   │   │   ├── ClientMessage.java    # Client → Server
│   │   │   │   ├── ClientMessageType.java # CALL, RAISE, FOLD, etc.
│   │   │   │   ├── ServerMessage.java    # Server → Client
│   │   │   │   ├── ServerMessageType.java # GAME_STATE_UPDATE, etc.
│   │   │   │   └── MessageCodec.java     # JSON encoder/decoder
│   │   │   ├── session/            # Session management
│   │   │   │   ├── WebSocketSessionRegistry.java
│   │   │   │   └── PlayerSession.java
│   │   │   ├── service/            # WebSocket services
│   │   │   │   ├── GameCommandService.java # Game action handler
│   │   │   │   └── TurnTimeoutService.java # Timeout management
│   │   │   └── metrics/            # Metrics collection
│   │   │       └── GameMetrics.java
│   │   │
│   │   ├── web/                    # REST API (future)
│   │   └── terminal/               # Terminal commands (for testing)
│   │
│   └── out/                        # Output adapters (driven)
│       ├── persistence/            # Database persistence
│       │   └── jpa/
│       │       ├── entity/         # JPA entities
│       │       ├── repository/     # Spring Data repositories
│       │       └── adapter/        # Port implementations
│       │
│       ├── event/                  # Event store adapter
│       ├── matching/               # Matching queue adapter
│       │   └── InMemoryMatchingAdapter.java
│       │
│       ├── ai/                     # AI strategy adapter
│       │   └── RuleBasedAIStrategy.java
│       │
│       └── network/                # Network session management
│           └── session/
│               └── model/
│                   └── SessionState.java
│
├── dealer/                         # Game orchestration (transitional)
│   └── Dealer.java                 # Texas Hold'em game logic (936 lines)
│
├── server/                         # Server infrastructure
│   └── room/                       # Game room management
│       ├── GameRoom.java           # Room entity (384 lines)
│       ├── RoomRegistry.java       # Room registry
│       └── GameRoomSummary.java    # Room summary DTO
│
├── configuration/                  # Spring configuration
│   ├── WebSocketConfig.java        # WebSocket endpoint config
│   ├── JacksonConfig.java          # JSON serialization config
│   ├── MatchingSchedulingConfig.java # Matching scheduler
│   └── properties/                 # Configuration properties
│       ├── GameProperties.java
│       └── MatchingProperties.java
│
└── announcer/                      # Message broadcasting
    └── Announcer.java              # Broadcast utility
```

### Key Components

#### 1. GameWebSocketHandler
**Purpose**: Main WebSocket message handler
**Responsibilities**:
- Accept WebSocket connections
- Parse client messages (JSON → ClientMessage)
- Route messages to appropriate handlers
- Send server messages back to clients

**Public Methods**:
```java
@Override
public void afterConnectionEstablished(WebSocketSession session)
@Override
protected void handleTextMessage(WebSocketSession session, TextMessage message)
@Override
public void afterConnectionClosed(WebSocketSession session, CloseStatus status)
```

#### 2. GameCommandService
**Purpose**: Game action orchestration
**Responsibilities**:
- Execute player actions (CALL, RAISE, FOLD, CHECK, ALL_IN)
- Validate turn order and action legality
- Broadcast game state updates to all players
- Manage turn timeouts

**Public Methods**:
```java
public void executeAction(String sessionId, String action, Integer amount)
public void broadcastChatMessage(String roomId, String senderNickname, String message)
public String createAndStartGame(UUID gameSessionId, List<MatchingRequest> matchedPlayers)
```

#### 3. Dealer
**Purpose**: Texas Hold'em game rules engine
**Responsibilities**:
- Manage game flow (PRE_FLOP → FLOP → TURN → RIVER → SHOWDOWN)
- Deal community cards (0 → 3 → 4 → 5)
- Process player actions and update pot
- Determine winners and distribute chips

**Public Methods**:
```java
public void startTexasHoldem()                    // Start new game
public void processPlayerAction(Player player, PlayerAction action, int amount)
public Optional<Player> getCurrentTurnPlayer()    // Get current turn player
public void progressToNextRound()                 // Advance betting round
public List<Player> determineWinners()            // Determine showdown winners
public BettingRound getCurrentRound()             // Get current betting round
public List<Card> getCommunityCards()             // Get community cards
public int getPot()                               // Get current pot
public int getCurrentBet()                        // Get current bet amount
```

#### 4. GameRoom
**Purpose**: Game room management and player coordination
**Responsibilities**:
- Player join/leave management
- Host designation
- Game state synchronization
- Message broadcasting to room members

**Public Methods**:
```java
public void join(SessionState session, String nickname, boolean asHost)
public boolean remove(SessionState session)
public void startRound(SessionState requester)
public void processPlayerActionByNickname(String nickname, PlayerAction action, int amount)
public Map<String, Object> getGameStateMap()
public Optional<Player> getCurrentTurnPlayer()
```

#### 5. HandEvaluator
**Purpose**: Poker hand evaluation
**Responsibilities**:
- Evaluate 5-card poker hands
- Determine hand tier (Royal Flush → High Card)
- Calculate kickers for tie-breaking
- Tested with 21 golden test vectors

**Public Methods**:
```java
public HandResult evaluate(List<Card> fiveCards)
```

**Hand Rankings** (from best to worst):
1. ROYAL_FLUSH (A-K-Q-J-10 same suit)
2. STRAIGHT_FLUSH (5 consecutive cards same suit)
3. FOUR_OF_A_KIND (4 cards same rank)
4. FULL_HOUSE (3 of a kind + pair)
5. FLUSH (5 cards same suit)
6. STRAIGHT (5 consecutive cards)
7. THREE_OF_A_KIND (3 cards same rank)
8. TWO_PAIR (2 pairs)
9. ONE_PAIR (1 pair)
10. HIGH_CARD (no combinations)

#### 6. MatchingService
**Purpose**: Player matching and game creation
**Responsibilities**:
- Queue players for random matching
- Create private rooms with codes
- Start games when queue is full
- Add AI players if needed

**Public Methods**:
```java
public MatchingRequest joinRandomMatching(JoinRandomMatchingCommand command)
public MatchingRequest joinCodeMatching(JoinCodeMatchingCommand command)
public MatchingCode createMatchingCode()
public void cancelMatching(String sessionId)
```

---

## Domain Model

### Aggregates

#### Game Aggregate (Future)
**Status**: Structure exists, not fully utilized

The Game aggregate will encapsulate:
- Game state (active players, round, pot)
- Game rules enforcement
- Domain event emission

**Planned Structure**:
```java
public class Game extends AggregateRoot {
    private GameId id;
    private List<Player> players;
    private BettingRound currentRound;
    private int pot;
    private List<Card> communityCards;
    
    public Game startGame(long seed);
    public void processAction(PlayerId playerId, PlayerAction action, int amount);
    public void progressRound();
    public List<Player> determineWinners();
}
```

#### Player Aggregate
**Status**: Implemented

```java
public class Player {
    private PlayerId id;               // UUID
    private Nickname nickname;         // Display name
    private int chips;                 // Current chips
    private PlayerStatus status;       // ACTIVE, FOLDED, ALL_IN, OUT
    private List<Card> hand;          // 2 hole cards (Texas Hold'em)
    private int currentBet;           // Current bet in round
    
    // Public methods
    public void bet(int amount);
    public void fold();
    public void win(int amount);
    public void receiveCard(Card card);
}
```

### Value Objects (Immutable)

#### Card
```java
public record Card(Suit suit, Rank rank) {
    // Immutable, comparable
}
```

#### Deck
```java
public class Deck {
    private List<Card> cards; // 52 cards
    
    public static Deck newDeck();
    public void shuffle();
    public Card drawCard();
}
```

#### BettingRound
```java
public enum BettingRound {
    PRE_FLOP,   // After hole cards dealt
    FLOP,       // After 3 community cards
    TURN,       // After 4th community card
    RIVER,      // After 5th community card
    SHOWDOWN    // Final reveal
}
```

#### PlayerAction
```java
public enum PlayerAction {
    FOLD,       // Exit game, forfeit pot
    CHECK,      // Pass (when currentBet == 0)
    CALL,       // Match current bet
    RAISE,      // Increase bet
    ALL_IN      // Bet all remaining chips
}
```

### Domain Services

#### HandEvaluator
Evaluates 5-card poker hands:
```java
HandResult evaluate(List<Card> fiveCards)
```

Returns `HandResult` with:
- `Tier tier` - Hand ranking
- `List<Rank> kickers` - Tie-breaking cards

#### PotDistributor
Distributes pot to winners:
```java
List<Pot> distributePots(List<Player> players)
```

Handles:
- Main pot distribution
- Side pots (all-in scenarios) - **Not yet implemented**
- Split pots (tied hands)

#### WinnerResolver
Determines game winners:
```java
List<Player> resolveWinners(List<Player> players, List<Card> communityCards)
```

Process:
1. Evaluate each player's best 5-card hand
2. Compare hand results
3. Resolve ties with kickers
4. Return list of winners (can be multiple for split pots)

---

## WebSocket Protocol

### Endpoint
```
ws://localhost:8080/ws/game
```

### Message Format

All messages use JSON format:
```json
{
  "type": "MESSAGE_TYPE",
  "timestamp": 1234567890,
  "payload": { /* type-specific data */ }
}
```

### Client → Server Messages

| Type | Payload | Description | When to Send |
|------|---------|-------------|--------------|
| `REGISTER` | `{uuid, nickname}` | Initial registration | After connection |
| `HEARTBEAT` | `{}` | Keep connection alive | Every 30 seconds |
| `JOIN_RANDOM_MATCH` | `{}` | Join random matching | Before game starts |
| `JOIN_CODE_MATCH` | `{code}` | Join with room code | Before game starts |
| `CANCEL_MATCHING` | `{}` | Cancel matching | While in queue |
| `CALL` | `{}` | Call current bet | During your turn |
| `RAISE` | `{amount}` | Raise bet | During your turn |
| `FOLD` | `{}` | Fold hand | During your turn |
| `CHECK` | `{}` | Check (no bet) | During your turn (currentBet == 0) |
| `ALL_IN` | `{}` | Bet all chips | During your turn |
| `CHAT_MESSAGE` | `{message}` | Send chat | Anytime in game |
| `LEAVE_GAME` | `{}` | Leave game | Anytime in game |

### Server → Client Messages

| Type | Payload | Description | When Sent |
|------|---------|-------------|-----------|
| `REGISTER_SUCCESS` | `{uuid, nickname}` | Registration confirmed | After REGISTER |
| `REGISTER_FAILURE` | `{reason}` | Registration failed | After REGISTER error |
| `MATCHING_STARTED` | `{matchingId}` | Matching begun | After JOIN_*_MATCH |
| `MATCHING_PROGRESS` | `{currentCount, requiredCount}` | Queue status | When player joins |
| `MATCHING_COMPLETED` | `{gameId, players}` | Game starting | When queue full |
| `MATCHING_CANCELLED` | `{}` | Matching cancelled | After CANCEL_MATCHING |
| `GAME_STARTED` | `{gameId, players, yourCards}` | Game started | Game begins |
| `GAME_STATE_UPDATE` | `{full game state}` | State sync | After each action |
| `PLAYER_ACTION` | `{nickname, action, amount}` | Action notification | After player acts |
| `TURN_CHANGED` | `{currentPlayer}` | Turn changed | After action processed |
| `ROUND_PROGRESSED` | `{fromRound, toRound, communityCards}` | Round advanced | FLOP/TURN/RIVER |
| `ROUND_COMPLETED` | `{winners, pots}` | Round ended | After SHOWDOWN |
| `GAME_ENDED` | `{finalStandings}` | Game finished | Game over |
| `TURN_TIMEOUT_STARTED` | `{playerNickname, remainingSeconds}` | Timeout started | Turn begins |
| `PLAYER_TIMED_OUT` | `{playerNickname}` | Player timeout | After timeout |
| `CHAT_MESSAGE` | `{nickname, message, timestamp}` | Chat message | After CHAT_MESSAGE |
| `ERROR` | `{message}` | Error occurred | On error |
| `INVALID_ACTION` | `{reason}` | Invalid action | On validation failure |

### Game State Update Payload

The `GAME_STATE_UPDATE` message contains the complete game state:

```json
{
  "type": "GAME_STATE_UPDATE",
  "timestamp": 1696454400000,
  "payload": {
    "gameId": "room-uuid",
    "roomName": "Quick Match #42",
    "playerCount": 3,
    "round": "FLOP",
    "pot": 600,
    "currentBet": 200,
    "communityCards": ["AH", "KD", "QC"],
    "currentPlayer": "Alice",
    "players": [
      {
        "nickname": "Alice",
        "chips": 9800,
        "bet": 200,
        "status": "ACTIVE"
      },
      {
        "nickname": "Bob",
        "chips": 9600,
        "bet": 200,
        "status": "ACTIVE"
      },
      {
        "nickname": "Charlie",
        "chips": 0,
        "bet": 200,
        "status": "ALL_IN"
      }
    ]
  }
}
```

**Field Explanations**:
- `gameId`: Unique room identifier
- `roomName`: Human-readable room name
- `playerCount`: Number of players in room
- `round`: Current betting round (PRE_FLOP, FLOP, TURN, RIVER, SHOWDOWN)
- `pot`: Total pot amount (sum of all bets)
- `currentBet`: Current bet amount to call
- `communityCards`: Community cards in compact format (e.g., "AH" = Ace of Hearts)
- `currentPlayer`: Nickname of player whose turn it is
- `players`: Array of player states
  - `nickname`: Player display name
  - `chips`: Remaining chips
  - `bet`: Amount bet in current round
  - `status`: ACTIVE, FOLDED, ALL_IN, or OUT

### Card Format

Cards use **compact string format** for efficiency:
- Format: `{Rank}{Suit}`
- Rank: `A`, `K`, `Q`, `J`, `10`, `9`, `8`, `7`, `6`, `5`, `4`, `3`, `2`
- Suit: `S` (Spades), `H` (Hearts), `D` (Diamonds), `C` (Clubs)

Examples:
- `"AS"` = Ace of Spades
- `"10H"` = Ten of Hearts
- `"2C"` = Two of Clubs

**Benefit**: 70% smaller payload compared to full JSON objects

---

## Client Architecture

### Overview
The client is a **Go TUI (Terminal User Interface)** application using the Bubble Tea framework.

**Repository**: `pokerhole-cli` (separate repository)

### Client Components

#### 1. WebSocket Client
**Purpose**: Maintain connection to server
**Responsibilities**:
- Establish WebSocket connection
- Send client messages (JSON encoding)
- Receive server messages (JSON decoding)
- Handle reconnection on disconnect

#### 2. Game State Manager
**Purpose**: Maintain local game state
**Responsibilities**:
- Store current game state from server
- Update state on GAME_STATE_UPDATE messages
- Provide state to UI components

#### 3. UI Renderer
**Purpose**: Render game state to terminal
**Responsibilities**:
- Display poker table layout
- Show player positions, chips, bets
- Render community cards
- Display action buttons
- Show chat messages

#### 4. Input Handler
**Purpose**: Handle user input
**Responsibilities**:
- Keyboard input for actions (CALL, RAISE, FOLD, CHECK, ALL_IN)
- Chat message input
- Action validation before sending

### Client Screens

#### 1. Connection Screen
- Enter server URL
- Enter nickname
- Connect button

#### 2. Lobby Screen
- Join random match
- Join with code
- Create private room
- Display waiting players

#### 3. Game Screen
```
┌─────────────────────────────────────────────────────────────┐
│  Texas Hold'em - Room: Quick Match #42                      │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│         [Bob]                    [Charlie]                  │
│        Chips: 9600              Chips: 0 (ALL-IN)           │
│        Bet: 200                 Bet: 200                    │
│                                                             │
│              ┌───┐ ┌───┐ ┌───┐                             │
│              │ A │ │ K │ │ Q │                             │
│              │ ♥ │ │ ♦ │ │ ♣ │                             │
│              └───┘ └───┘ └───┘                             │
│                                                             │
│         Pot: 600 | Current Bet: 200 | Round: FLOP          │
│                                                             │
│                    [You: Alice]                             │
│                   Chips: 9800                               │
│                   Bet: 200                                  │
│                                                             │
│         ┌───┐ ┌───┐                                         │
│         │ J │ │ 10│  (Your Hole Cards)                     │
│         │ ♠ │ │ ♠ │                                         │
│         └───┘ └───┘                                         │
│                                                             │
├─────────────────────────────────────────────────────────────┤
│ Actions: [F]old  [C]all 200  [R]aise  [A]ll-in            │
├─────────────────────────────────────────────────────────────┤
│ Chat:                                                       │
│ Bob: Good hand!                                             │
│ > _                                                         │
└─────────────────────────────────────────────────────────────┘
```

**Features**:
- Player positions around table
- Community cards display
- Hole cards (face-up for you, face-down for others)
- Pot and bet information
- Action buttons (keyboard shortcuts)
- Chat window
- Timer display (for turn timeout)

#### 4. Results Screen
- Show winning hand
- Display chip changes
- Show hand rankings
- Continue or quit options

---

## Data Flow

### Game Start Flow

```
┌─────────┐                  ┌─────────┐                  ┌─────────┐
│ Client  │                  │ Server  │                  │ Domain  │
└────┬────┘                  └────┬────┘                  └────┬────┘
     │                            │                            │
     │ 1. REGISTER                │                            │
     ├───────────────────────────>│                            │
     │                            │ 2. Register session        │
     │                            ├───────────────────────────>│
     │                            │                            │
     │ 3. REGISTER_SUCCESS        │                            │
     │<───────────────────────────┤                            │
     │                            │                            │
     │ 4. JOIN_RANDOM_MATCH       │                            │
     ├───────────────────────────>│                            │
     │                            │ 5. Add to matching queue   │
     │                            ├───────────────────────────>│
     │                            │                            │
     │ 6. MATCHING_PROGRESS       │                            │
     │<───────────────────────────┤                            │
     │                            │                            │
     │      ... wait for other players ...                     │
     │                            │                            │
     │                            │ 7. Queue full, match       │
     │                            │    complete                │
     │                            │<───────────────────────────┤
     │                            │                            │
     │ 8. MATCHING_COMPLETED      │                            │
     │<───────────────────────────┤                            │
     │                            │                            │
     │                            │ 9. Create GameRoom         │
     │                            │ 10. Start Texas Hold'em    │
     │                            ├───────────────────────────>│
     │                            │                            │
     │                            │ 11. Deal hole cards        │
     │                            │<───────────────────────────┤
     │                            │                            │
     │ 12. GAME_STARTED           │                            │
     │     (with your hole cards) │                            │
     │<═══════════════════════════┤                            │
     │                            │                            │
     │ 13. GAME_STATE_UPDATE      │                            │
     │     (initial state)        │                            │
     │<═══════════════════════════┤                            │
     │                            │                            │
```

### Player Action Flow

```
┌─────────┐                  ┌─────────┐                  ┌─────────┐
│ Client  │                  │ Server  │                  │  Game   │
└────┬────┘                  └────┬────┘                  └────┬────┘
     │                            │                            │
     │ 1. RAISE {amount: 400}     │                            │
     ├───────────────────────────>│                            │
     │                            │ 2. Validate turn           │
     │                            │ 3. Validate chips          │
     │                            │                            │
     │                            │ 4. Process action          │
     │                            ├───────────────────────────>│
     │                            │ 5. Update pot, bet         │
     │                            │ 6. Move to next player     │
     │                            │<───────────────────────────┤
     │                            │                            │
     │ 7. PLAYER_ACTION           │                            │
     │    (broadcast to all)      │                            │
     │<═══════════════════════════┤                            │
     │                            │                            │
     │ 8. GAME_STATE_UPDATE       │                            │
     │    (full state sync)       │                            │
     │<═══════════════════════════┤                            │
     │                            │                            │
     │ 9. TURN_TIMEOUT_STARTED    │                            │
     │    (next player's timer)   │                            │
     │<═══════════════════════════┤                            │
     │                            │                            │
```

**Notes**:
- `─────>` = single request/response
- `═════>` = broadcast to all players in room

### Round Progression Flow

```
┌─────────┐                  ┌─────────┐                  ┌─────────┐
│ Client  │                  │ Server  │                  │  Game   │
└────┬────┘                  └────┬────┘                  └────┬────┘
     │                            │                            │
     │                            │ 1. All players acted       │
     │                            │ 2. All bets equal          │
     │                            │                            │
     │                            │ 3. progressToNextRound()   │
     │                            ├───────────────────────────>│
     │                            │ 4. Reveal community cards  │
     │                            │    - FLOP: 3 cards         │
     │                            │    - TURN: 1 card          │
     │                            │    - RIVER: 1 card         │
     │                            │<───────────────────────────┤
     │                            │                            │
     │ 5. ROUND_PROGRESSED        │                            │
     │    {fromRound, toRound,    │                            │
     │     communityCards}        │                            │
     │<═══════════════════════════┤                            │
     │                            │                            │
     │ 6. GAME_STATE_UPDATE       │                            │
     │    (updated state with     │                            │
     │     new community cards)   │                            │
     │<═══════════════════════════┤                            │
     │                            │                            │
```

### Showdown Flow

```
┌─────────┐                  ┌─────────┐                  ┌─────────┐
│ Client  │                  │ Server  │                  │  Game   │
└────┬────┘                  └────┬────┘                  └────┬────┘
     │                            │                            │
     │                            │ 1. RIVER round complete    │
     │                            │                            │
     │                            │ 2. determineWinners()      │
     │                            ├───────────────────────────>│
     │                            │ 3. Evaluate all hands      │
     │                            │ 4. Compare results         │
     │                            │ 5. Distribute pot          │
     │                            │<───────────────────────────┤
     │                            │                            │
     │ 6. ROUND_COMPLETED         │                            │
     │    {winners, pots,         │                            │
     │     playerHands}           │                            │
     │<═══════════════════════════┤                            │
     │                            │                            │
     │ 7. GAME_ENDED              │                            │
     │    (if game over)          │                            │
     │<═══════════════════════════┤                            │
     │                            │                            │
```

---

## Component Interactions

### Matching to Game Start

```
┌──────────────────┐
│  Client sends    │
│  JOIN_RANDOM     │
└────────┬─────────┘
         │
         ↓
┌────────────────────────────────────────────────────┐
│     GameWebSocketHandler                           │
│  • Receives JOIN_RANDOM_MATCH message              │
│  • Calls joinRandomMatchingUseCase.join()          │
└─────────────────────┬──────────────────────────────┘
                      │
                      ↓
┌────────────────────────────────────────────────────┐
│     MatchingService (Use Case)                     │
│  • Creates MatchingRequest                         │
│  • Adds to MatchingQueue                           │
│  • Sends MATCHING_PROGRESS notification            │
└─────────────────────┬──────────────────────────────┘
                      │
                      ↓
┌────────────────────────────────────────────────────┐
│     InMemoryMatchingAdapter (Queue Port)           │
│  • Stores request in queue                         │
│  • Checks if queue full (4 players)                │
│  • If full, triggers matching completion           │
└─────────────────────┬──────────────────────────────┘
                      │
                      ↓
┌────────────────────────────────────────────────────┐
│     MatchingScheduler (Scheduled Task)             │
│  • Runs every 1 second                             │
│  • Checks all queues for complete matches          │
│  • Processes matched players                       │
└─────────────────────┬──────────────────────────────┘
                      │
                      ↓
┌────────────────────────────────────────────────────┐
│     GameCommandService                             │
│  • createAndStartGame(gameSessionId, players)      │
│  • Creates GameRoom                                │
│  • Adds players to room                            │
│  • Starts Texas Hold'em game                       │
└─────────────────────┬──────────────────────────────┘
                      │
                      ↓
┌────────────────────────────────────────────────────┐
│     GameRoom + Dealer                              │
│  • dealer.startTexasHoldem()                       │
│  • Deals hole cards to players                    │
│  • Initializes game state                          │
└─────────────────────┬──────────────────────────────┘
                      │
                      ↓
┌────────────────────────────────────────────────────┐
│     WebSocket Broadcasting                         │
│  • Sends MATCHING_COMPLETED to all players         │
│  • Sends GAME_STARTED with hole cards              │
│  • Sends GAME_STATE_UPDATE with initial state      │
└────────────────────────────────────────────────────┘
```

### Player Action Processing

```
┌──────────────────┐
│  Client sends    │
│  RAISE action    │
└────────┬─────────┘
         │
         ↓
┌────────────────────────────────────────────────────┐
│     GameWebSocketHandler                           │
│  • handleGameAction(session, "RAISE", payload)     │
│  • Validates amount parameter                      │
│  • Calls gameCommandService.executeAction()        │
└─────────────────────┬──────────────────────────────┘
                      │
                      ↓
┌────────────────────────────────────────────────────┐
│     GameCommandService                             │
│  • executeAction(sessionId, "RAISE", amount)       │
│  • Looks up PlayerSession from sessionRegistry     │
│  • Finds GameRoom from roomRegistry                │
│  • Validates it's player's turn                    │
└─────────────────────┬──────────────────────────────┘
                      │
                      ↓
┌────────────────────────────────────────────────────┐
│     GameRoom                                       │
│  • processPlayerActionByNickname(nickname,         │
│    PlayerAction.RAISE, amount)                     │
│  • Finds player by nickname                        │
│  • Delegates to Dealer                             │
└─────────────────────┬──────────────────────────────┘
                      │
                      ↓
┌────────────────────────────────────────────────────┐
│     Dealer (Game Logic)                            │
│  • processPlayerAction(player, RAISE, amount)      │
│  • Validates action legality                       │
│  • Updates player bet and chips                    │
│  • Updates pot and currentBet                      │
│  • Moves to next player                            │
│  • Checks if round complete                        │
└─────────────────────┬──────────────────────────────┘
                      │
                      ↓
┌────────────────────────────────────────────────────┐
│     GameCommandService (Broadcasting)              │
│  • Broadcasts PLAYER_ACTION to all players         │
│  • Broadcasts GAME_STATE_UPDATE with new state     │
│  • Starts turn timeout for next player             │
└─────────────────────┬──────────────────────────────┘
                      │
                      ↓
┌────────────────────────────────────────────────────┐
│     TurnTimeoutService                             │
│  • Schedules timeout task (30 seconds)             │
│  • Broadcasts TURN_TIMEOUT_STARTED                 │
│  • If timeout expires, auto-folds player           │
└────────────────────────────────────────────────────┘
```

---

## Public APIs

### WebSocket Session Registry

**Class**: `WebSocketSessionRegistry`
**Purpose**: Manage active WebSocket sessions

```java
public class WebSocketSessionRegistry {
    public void register(PlayerSession session);
    public Optional<PlayerSession> findBySessionId(String sessionId);
    public Optional<PlayerSession> findByUuid(String uuid);
    public void unregisterBySessionId(String sessionId);
    public boolean containsUuid(String uuid);
    public Collection<PlayerSession> getAllSessions();
}
```

### Room Registry

**Class**: `RoomRegistry`
**Purpose**: Manage game rooms

```java
public class RoomRegistry {
    public GameRoom createRoom(String name);
    public Optional<GameRoom> findRoom(String roomId);
    public List<GameRoomSummary> listRooms();
    public boolean removeRoom(String roomId);
    public int getRoomCount();
}
```

### Matching Use Cases

**Interface**: `JoinRandomMatchingUseCase`
```java
public interface JoinRandomMatchingUseCase {
    MatchingRequest joinRandomMatching(JoinRandomMatchingCommand command);
    
    record JoinRandomMatchingCommand(String sessionId, String nickname) {}
}
```

**Interface**: `JoinCodeMatchingUseCase`
```java
public interface JoinCodeMatchingUseCase {
    MatchingRequest joinCodeMatching(JoinCodeMatchingCommand command);
    
    record JoinCodeMatchingCommand(
        String sessionId, 
        String nickname, 
        String matchingCode
    ) {}
}
```

### Game Command Service

**Class**: `GameCommandService`

```java
public class GameCommandService {
    /**
     * Execute player action (CALL, RAISE, FOLD, CHECK, ALL_IN)
     * @param sessionId WebSocket session ID
     * @param action Action type string
     * @param amount Amount for RAISE (null for other actions)
     * @throws IllegalStateException if invalid turn or action
     */
    public void executeAction(String sessionId, String action, Integer amount);
    
    /**
     * Broadcast chat message to all players in room
     * @param roomId Game room ID
     * @param senderNickname Sender's nickname
     * @param message Chat message content
     */
    public void broadcastChatMessage(String roomId, String senderNickname, String message);
    
    /**
     * Create and start a new game from matched players
     * @param gameSessionId Unique game session ID
     * @param matchedPlayers List of matched players
     * @return Room ID of created game
     */
    public String createAndStartGame(UUID gameSessionId, List<MatchingRequest> matchedPlayers);
}
```

---

## Implementation Status

### ✅ Completed Features

1. **Domain Model**
   - Card, Deck, Rank, Suit, Tier
   - Player aggregate with status management
   - Hand evaluation with 21 golden test vectors
   - Betting rounds and player actions

2. **Texas Hold'em Game Logic**
   - Full game flow (PRE_FLOP → SHOWDOWN)
   - Community card dealing (0 → 3 → 4 → 5)
   - Player actions (FOLD, CHECK, CALL, RAISE, ALL_IN)
   - Turn management and rotation
   - Basic pot accumulation
   - Winner determination

3. **WebSocket Communication**
   - Real-time bidirectional messaging
   - 12 client message types
   - 16 server message types
   - JSON encoding/decoding
   - Session management
   - Message broadcasting

4. **Matching System**
   - Random matching queue
   - Code-based private rooms
   - Automatic game start
   - AI player integration (RuleBasedAI)

5. **Architecture**
   - Hexagonal architecture pattern
   - Pure domain layer (zero framework deps)
   - Port & Adapter pattern
   - ArchUnit tests for validation

6. **Testing**
   - 49 tests, 100% pass rate
   - 18 Texas Hold'em integration tests
   - Hand evaluator golden test vectors
   - Architecture validation tests

### 🔄 Partial / In Progress

1. **Event Sourcing**
   - Structure exists (Event, EventStore interfaces)
   - Not actively used yet
   - Events emitted but not persisted
   - No event replay mechanism

2. **Timeout Handling**
   - Turn timeout detection working
   - Timeout notification sent
   - Automatic fold not implemented

3. **Documentation**
   - README.md complete
   - ROADMAP.md created (this document)
   - ARCHITECTURE.md created (this document)
   - API documentation needed

### ❌ Not Implemented

1. **Side Pots**
   - All-in scenario handling incomplete
   - Only single pot currently supported
   - See `SIDE_POT_IMPLEMENTATION.md` for spec

2. **Blind Management**
   - No small blind / big blind enforcement
   - Dealer button doesn't rotate
   - Players can check on first bet

3. **Reconnection**
   - No session recovery
   - Disconnect = leave game
   - No grace period for reconnection

4. **Multi-Table**
   - Only single room concept
   - No room listing/browsing
   - No observer mode

5. **Analytics**
   - No game statistics
   - No player performance tracking
   - No monitoring/metrics

6. **Client SDK**
   - No Go client library yet
   - CLI client in separate repo (not started)

---

## Next Steps

### Priority 1: Core Gameplay Completion
1. Implement side pot logic
2. Add blind management (small blind / big blind)
3. Implement automatic fold on timeout
4. Complete reconnection handling

### Priority 2: Testing & Stability
1. Increase test coverage to 85%+
2. Add stress tests (concurrent users)
3. Test side pot scenarios
4. Test disconnect/reconnect scenarios

### Priority 3: Client Development
1. Create Go client SDK
2. Build TUI client (Bubble Tea)
3. Implement client-side validation
4. Add chat functionality

### Priority 4: Production Readiness
1. Enable event sourcing persistence
2. Add monitoring (Prometheus/Grafana)
3. Implement authentication (JWT)
4. Add rate limiting
5. Set up CI/CD pipeline

---

## Appendix

### Glossary

| Term | Definition |
|------|------------|
| **Aggregate** | DDD pattern - cluster of domain objects treated as a unit |
| **Betting Round** | Stage of Texas Hold'em (PRE_FLOP, FLOP, TURN, RIVER, SHOWDOWN) |
| **Blind** | Forced bet at start of hand (small blind, big blind) |
| **Community Cards** | Shared cards in center of table (0-5 cards) |
| **Dealer Button** | Marker indicating theoretical dealer position |
| **Event Sourcing** | Pattern of storing all state changes as events |
| **Hexagonal Architecture** | Pattern separating domain from infrastructure |
| **Hole Cards** | Private cards dealt to each player (2 in Texas Hold'em) |
| **Kicker** | Tie-breaking card in poker hand comparison |
| **Port** | Interface defining boundary between layers |
| **Pot** | Total amount of chips bet in current hand |
| **Showdown** | Final reveal of hands to determine winner |
| **Side Pot** | Separate pot for all-in scenarios |
| **Value Object** | Immutable domain object with no identity |

### References

**Design Patterns**:
- [Hexagonal Architecture](https://alistair.cockburn.us/hexagonal-architecture/) - Alistair Cockburn
- [Event Sourcing](https://martinfowler.com/eaaDev/EventSourcing.html) - Martin Fowler
- [Domain-Driven Design](https://www.domainlanguage.com/ddd/) - Eric Evans

**Texas Hold'em Rules**:
- [PokerNews Rules](https://www.pokernews.com/poker-rules/texas-holdem.htm)
- [Wikipedia](https://en.wikipedia.org/wiki/Texas_hold_%27em)

**Technologies**:
- [Spring Boot 3.4.0](https://spring.io/projects/spring-boot)
- [WebSocket RFC 6455](https://datatracker.ietf.org/doc/html/rfc6455)
- [Java 23 Virtual Threads](https://openjdk.org/jeps/444)
- [Bubble Tea (Go TUI)](https://github.com/charmbracelet/bubbletea)

---

**Last Updated**: 2025-10-05
**Version**: 0.6.0
**Status**: Documentation Complete - Ready for Team Development
