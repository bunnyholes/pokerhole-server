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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JPA Entity for hand_history table.
 *
 * Stores metadata for each poker hand played.
 * A "hand" is a complete round of poker from deal to showdown.
 *
 * Architecture: Event Sourcing + JSONB for flexibility
 * Database: PostgreSQL with JSONB support
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
    name = "hand_history",
    indexes = {
        @Index(name = "idx_hand_history_game_id", columnList = "game_id"),
        @Index(name = "idx_hand_history_started_at", columnList = "started_at DESC")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "idx_hand_history_game_hand_number", columnNames = {"game_id", "hand_number"})
    }
)
public class HandHistoryEntity {

    /**
     * Primary identifier for this hand
     */
    @Id
    @Column(name = "hand_id", nullable = false, updatable = false)
    private UUID handId;

    /**
     * Reference to the game this hand belongs to
     */
    @Column(name = "game_id", nullable = false, updatable = false)
    private UUID gameId;

    /**
     * Sequential hand number within the game (1, 2, 3, ...)
     * Useful for human-readable references and ordering
     */
    @Column(name = "hand_number", nullable = false, updatable = false)
    private Integer handNumber;

    /**
     * Timestamp when this hand started (first card dealt)
     */
    @Column(name = "started_at", nullable = false, updatable = false)
    private Instant startedAt;

    /**
     * Timestamp when this hand ended (pot distributed)
     * NULL if hand is still ongoing
     */
    @Column(name = "ended_at")
    private Instant endedAt;

    /**
     * Total pot amount at end of hand (in chips)
     * NULL if hand is still ongoing
     */
    @Column(name = "final_pot")
    private Long finalPot;

    /**
     * Array of winner player IDs (supports split pots)
     * Stored as JSONB in PostgreSQL
     * Example: ["uuid1", "uuid2"] for split pot
     * Example: ["uuid1"] for single winner
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "winner_ids", columnDefinition = "jsonb")
    @Builder.Default
    private List<UUID> winnerIds = new ArrayList<>();

    /**
     * Seed used for deterministic deck shuffling (ADR-005)
     * Same seed = same shuffle = verifiable fairness
     */
    @Column(name = "deck_seed", nullable = false, updatable = false)
    private Long deckSeed;

    /**
     * Position of dealer button (0-based index)
     */
    @Column(name = "dealer_button_position", nullable = false, updatable = false)
    private Integer dealerButtonPosition;

    /**
     * Small blind amount for this hand (in chips)
     */
    @Column(name = "small_blind", nullable = false, updatable = false)
    private Long smallBlind;

    /**
     * Big blind amount for this hand (in chips)
     */
    @Column(name = "big_blind", nullable = false, updatable = false)
    private Long bigBlind;

    /**
     * Timestamp when this record was created
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    /**
     * One-to-many relationship with hand events
     */
    @OneToMany(mappedBy = "handHistory", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("eventSequence ASC")
    @Builder.Default
    private List<HandEventEntity> events = new ArrayList<>();

    /**
     * Helper method to add an event to this hand
     */
    public void addEvent(HandEventEntity event) {
        events.add(event);
        event.setHandHistory(this);
    }

    /**
     * Helper method to remove an event from this hand
     */
    public void removeEvent(HandEventEntity event) {
        events.remove(event);
        event.setHandHistory(null);
    }
}
