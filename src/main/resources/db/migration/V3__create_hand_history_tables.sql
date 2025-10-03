-- =============================================================================
-- Hand History Schema for Texas Hold'em Event-Sourced Poker
-- =============================================================================
-- Version: V3
-- Purpose: Store complete hand history for replay, analysis, and audit
-- Architecture: Event Sourcing + JSONB for flexibility
-- Database: PostgreSQL 18+
-- =============================================================================

-- -----------------------------------------------------------------------------
-- Table: hand_history
-- Description: Stores metadata for each poker hand played
-- -----------------------------------------------------------------------------
-- A "hand" is a complete round of poker from deal to showdown.
-- This table captures high-level information about the hand,
-- while detailed events are stored in hand_events table.
--
-- Key Features:
-- - UUIDs for distributed system compatibility
-- - Deterministic deck seed for replay verification
-- - JSONB for winner_ids to support split pots (multiple winners)
-- - Blind amounts stored for historical accuracy
-- - Dealer button position for complete game state
-- -----------------------------------------------------------------------------

CREATE TABLE hand_history (
    -- Primary identifier
    hand_id UUID PRIMARY KEY,

    -- Game association
    game_id UUID NOT NULL,

    -- Sequential hand number within the game (1, 2, 3, ...)
    -- Useful for human-readable references and ordering
    hand_number INTEGER NOT NULL,

    -- Timestamps
    started_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    ended_at TIMESTAMP WITHOUT TIME ZONE,

    -- Final results
    final_pot BIGINT,  -- Total pot amount (in chips)
    winner_ids JSONB,  -- Array of UUID strings for winners (supports split pots)
                       -- Example: ["uuid1", "uuid2"] for split pot
                       -- Example: ["uuid1"] for single winner

    -- Deterministic replay support (ADR-005)
    deck_seed BIGINT NOT NULL,  -- Seed used for deck shuffling
                                -- Same seed = same shuffle = verifiable fairness

    -- Game state metadata
    dealer_button_position INTEGER NOT NULL,  -- Position of dealer button (0-based)
    small_blind BIGINT NOT NULL,              -- Small blind amount for this hand
    big_blind BIGINT NOT NULL,                -- Big blind amount for this hand

    -- Audit timestamp
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),

    -- Constraints
    CONSTRAINT hand_history_hand_number_positive CHECK (hand_number > 0),
    CONSTRAINT hand_history_dealer_position_non_negative CHECK (dealer_button_position >= 0),
    CONSTRAINT hand_history_blinds_positive CHECK (small_blind > 0 AND big_blind > 0),
    CONSTRAINT hand_history_big_blind_greater CHECK (big_blind >= small_blind),
    CONSTRAINT hand_history_final_pot_non_negative CHECK (final_pot IS NULL OR final_pot >= 0)
);

-- Index for querying hands by game (most common query pattern)
CREATE INDEX idx_hand_history_game_id ON hand_history(game_id);

-- Index for date range queries (e.g., "get hands from last week")
CREATE INDEX idx_hand_history_started_at ON hand_history(started_at DESC);

-- Composite index for game + hand number (ensures uniqueness and fast lookup)
CREATE UNIQUE INDEX idx_hand_history_game_hand_number ON hand_history(game_id, hand_number);

-- GIN index for JSONB winner_ids (supports queries like "find hands won by player X")
CREATE INDEX idx_hand_history_winner_ids ON hand_history USING GIN(winner_ids);

-- Index for finding ongoing hands (ended_at IS NULL)
CREATE INDEX idx_hand_history_ongoing ON hand_history(game_id, started_at DESC)
    WHERE ended_at IS NULL;

-- Comments for database documentation
COMMENT ON TABLE hand_history IS 'Stores metadata for each poker hand played';
COMMENT ON COLUMN hand_history.hand_id IS 'Unique identifier for this hand';
COMMENT ON COLUMN hand_history.game_id IS 'Reference to the game this hand belongs to';
COMMENT ON COLUMN hand_history.hand_number IS 'Sequential hand number within the game (1, 2, 3, ...)';
COMMENT ON COLUMN hand_history.started_at IS 'Timestamp when this hand started (first card dealt)';
COMMENT ON COLUMN hand_history.ended_at IS 'Timestamp when this hand ended (pot distributed), NULL if ongoing';
COMMENT ON COLUMN hand_history.final_pot IS 'Total pot amount at end of hand (in chips), NULL if ongoing';
COMMENT ON COLUMN hand_history.winner_ids IS 'JSONB array of winner player IDs (supports split pots)';
COMMENT ON COLUMN hand_history.deck_seed IS 'Seed used for deterministic deck shuffling (for replay verification)';
COMMENT ON COLUMN hand_history.dealer_button_position IS 'Position of dealer button (0-based index)';
COMMENT ON COLUMN hand_history.small_blind IS 'Small blind amount for this hand (in chips)';
COMMENT ON COLUMN hand_history.big_blind IS 'Big blind amount for this hand (in chips)';
COMMENT ON COLUMN hand_history.created_at IS 'Timestamp when this record was created';

-- -----------------------------------------------------------------------------
-- Table: hand_events
-- Description: Stores all events that occurred during a hand (Event Sourcing)
-- -----------------------------------------------------------------------------
-- This is the core of the Event Sourcing architecture.
-- Every action, state change, and decision is recorded as an immutable event.
--
-- Key Features:
-- - event_sequence ensures total ordering within a hand
-- - event_type is indexed for fast filtering (e.g., "get all PlayerActed events")
-- - event_data (JSONB) stores the full event payload with flexibility
-- - player_id is denormalized for fast queries (nullable for system events)
--
-- Event Types (from GameEvent sealed interface):
-- - RoundStarted: Hand begins, hole cards dealt
-- - BettingPhaseStarted: New betting phase begins
-- - PlayerActed: Player action (FOLD/CHECK/CALL/BET/RAISE/ALL_IN)
-- - RoundProgressed: Betting round advances (PRE_FLOP → FLOP → TURN → RIVER)
-- - TurnChanged: Current turn player changes
-- - PotDistributed: Pot distributed to winner(s)
-- - RoundEnded: Hand ends
-- -----------------------------------------------------------------------------

CREATE TABLE hand_events (
    -- Primary identifier
    event_id UUID PRIMARY KEY,

    -- Hand association (foreign key to hand_history)
    hand_id UUID NOT NULL REFERENCES hand_history(hand_id) ON DELETE CASCADE,

    -- Sequential ordering within this hand (1, 2, 3, ...)
    -- This is CRITICAL for event replay - events MUST be replayed in order
    event_sequence INTEGER NOT NULL,

    -- Event type (matches GameEvent sealed interface)
    -- Examples: PlayerActed, RoundProgressed, TurnChanged, PotDistributed
    event_type VARCHAR(50) NOT NULL,

    -- Player who triggered this event (nullable for system events)
    -- Denormalized for fast queries like "get all actions by player X"
    player_id UUID,

    -- Full event payload as JSONB (flexible schema)
    -- Contains all event-specific data
    -- Example PlayerActed: {"action": "RAISE", "amount": 200, "bettingRound": "FLOP"}
    -- Example RoundProgressed: {"fromRound": "FLOP", "toRound": "TURN", "newCommunityCards": ["AH"]}
    event_data JSONB NOT NULL,

    -- Timestamp when event occurred (for chronological ordering)
    occurred_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,

    -- Constraints
    CONSTRAINT hand_events_sequence_positive CHECK (event_sequence > 0),
    CONSTRAINT hand_events_unique_sequence UNIQUE (hand_id, event_sequence)
);

-- Index for replaying events in order (MOST IMPORTANT INDEX)
-- This composite index supports: "SELECT * FROM hand_events WHERE hand_id = ? ORDER BY event_sequence"
CREATE INDEX idx_hand_events_hand_sequence ON hand_events(hand_id, event_sequence);

-- Index for filtering by event type (e.g., "get all PlayerActed events")
CREATE INDEX idx_hand_events_type ON hand_events(event_type);

-- Index for player-specific queries (e.g., "get all actions by player X")
CREATE INDEX idx_hand_events_player_id ON hand_events(player_id)
    WHERE player_id IS NOT NULL;

-- Index for time-based queries (e.g., "get events in last hour")
CREATE INDEX idx_hand_events_occurred_at ON hand_events(occurred_at DESC);

-- GIN index for JSONB event_data (supports queries on JSON fields)
-- Example: WHERE event_data @> '{"action": "RAISE"}'
CREATE INDEX idx_hand_events_data ON hand_events USING GIN(event_data);

-- Composite index for player + event type queries
CREATE INDEX idx_hand_events_player_type ON hand_events(player_id, event_type)
    WHERE player_id IS NOT NULL;

-- Comments for database documentation
COMMENT ON TABLE hand_events IS 'Stores all events that occurred during a hand (Event Sourcing)';
COMMENT ON COLUMN hand_events.event_id IS 'Unique identifier for this event';
COMMENT ON COLUMN hand_events.hand_id IS 'Reference to the hand this event belongs to';
COMMENT ON COLUMN hand_events.event_sequence IS 'Sequential order of events within the hand (1, 2, 3, ...)';
COMMENT ON COLUMN hand_events.event_type IS 'Type of event (matches GameEvent sealed interface)';
COMMENT ON COLUMN hand_events.player_id IS 'Player who triggered this event (NULL for system events)';
COMMENT ON COLUMN hand_events.event_data IS 'Full event payload as JSONB (flexible schema)';
COMMENT ON COLUMN hand_events.occurred_at IS 'Timestamp when this event occurred';

-- =============================================================================
-- Storage Estimates & Performance Notes
-- =============================================================================
--
-- STORAGE ESTIMATES (per hand with 4 players, 50 events):
-- - hand_history: ~200 bytes per row
-- - hand_events: ~500 bytes per row (JSONB overhead)
-- - Total per hand: 200 + (50 * 500) = ~25 KB
-- - 1 million hands: ~25 GB (acceptable for PostgreSQL)
--
-- QUERY PATTERNS SUPPORTED:
-- 1. Replay hand: SELECT * FROM hand_events WHERE hand_id = ? ORDER BY event_sequence
--    - Uses: idx_hand_events_hand_sequence
--    - Performance: O(log n) lookup + sequential scan of events
--
-- 2. Find hands by game: SELECT * FROM hand_history WHERE game_id = ? ORDER BY started_at DESC
--    - Uses: idx_hand_history_game_id
--    - Performance: O(log n) lookup + index scan
--
-- 3. Find hands won by player: SELECT * FROM hand_history WHERE winner_ids @> '["player_uuid"]'
--    - Uses: idx_hand_history_winner_ids (GIN index)
--    - Performance: O(log n) GIN index lookup
--
-- 4. Get player actions: SELECT * FROM hand_events WHERE player_id = ? AND event_type = 'PlayerActed'
--    - Uses: idx_hand_events_player_type
--    - Performance: O(log n) lookup + index scan
--
-- 5. Date range query: SELECT * FROM hand_history WHERE started_at BETWEEN ? AND ?
--    - Uses: idx_hand_history_started_at
--    - Performance: O(log n) range scan
--
-- MAINTENANCE NOTES:
-- - VACUUM ANALYZE hand_events regularly (high insert rate)
-- - Consider partitioning hand_events by game_id if > 100M rows
-- - Monitor GIN index size (can grow large with many unique JSONB values)
-- - Archive old hands to cold storage if retention policy requires
--
-- =============================================================================
