package dev.xiyo.pokerhole.adapter.out.persistence.jpa.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA Entity for hand_events table.
 *
 * Stores all events that occurred during a hand (Event Sourcing).
 * Every action, state change, and decision is recorded as an immutable event.
 *
 * Event Types (from GameEvent sealed interface):
 * - RoundStarted: Hand begins, hole cards dealt
 * - BettingPhaseStarted: New betting phase begins
 * - PlayerActed: Player action (FOLD/CHECK/CALL/BET/RAISE/ALL_IN)
 * - RoundProgressed: Betting round advances (PRE_FLOP → FLOP → TURN → RIVER)
 * - TurnChanged: Current turn player changes
 * - PotDistributed: Pot distributed to winner(s)
 * - RoundEnded: Hand ends
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
    name = "hand_events",
    indexes = {
        @Index(name = "idx_hand_events_hand_sequence", columnList = "hand_id, event_sequence"),
        @Index(name = "idx_hand_events_type", columnList = "event_type"),
        @Index(name = "idx_hand_events_player_id", columnList = "player_id"),
        @Index(name = "idx_hand_events_occurred_at", columnList = "occurred_at DESC"),
        @Index(name = "idx_hand_events_player_type", columnList = "player_id, event_type")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "hand_events_unique_sequence", columnNames = {"hand_id", "event_sequence"})
    }
)
public class HandEventEntity {

    /**
     * Primary identifier for this event
     */
    @Id
    @Column(name = "event_id", nullable = false, updatable = false)
    private UUID eventId;

    /**
     * Reference to the hand this event belongs to
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hand_id", nullable = false, foreignKey = @ForeignKey(name = "fk_hand_events_hand_id"))
    private HandHistoryEntity handHistory;

    /**
     * Sequential order of events within the hand (1, 2, 3, ...)
     * CRITICAL for event replay - events MUST be replayed in order
     */
    @Column(name = "event_sequence", nullable = false, updatable = false)
    private Integer eventSequence;

    /**
     * Type of event (matches GameEvent sealed interface)
     * Examples: PlayerActed, RoundProgressed, TurnChanged, PotDistributed
     */
    @Column(name = "event_type", nullable = false, length = 50, updatable = false)
    private String eventType;

    /**
     * Player who triggered this event (nullable for system events)
     * Denormalized for fast queries like "get all actions by player X"
     */
    @Column(name = "player_id")
    private UUID playerId;

    /**
     * Full event payload as JSONB (flexible schema)
     * Contains all event-specific data
     *
     * Example PlayerActed: {"action": "RAISE", "amount": 200, "bettingRound": "FLOP"}
     * Example RoundProgressed: {"fromRound": "FLOP", "toRound": "TURN", "newCommunityCards": ["AH"]}
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "event_data", nullable = false, columnDefinition = "jsonb")
    private String eventData;

    /**
     * Timestamp when event occurred (for chronological ordering)
     */
    @Column(name = "occurred_at", nullable = false, updatable = false)
    private Instant occurredAt;

    /**
     * Override equals to compare by event ID only
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HandEventEntity)) return false;
        HandEventEntity that = (HandEventEntity) o;
        return eventId != null && eventId.equals(that.eventId);
    }

    /**
     * Override hashCode to use event ID only
     */
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
