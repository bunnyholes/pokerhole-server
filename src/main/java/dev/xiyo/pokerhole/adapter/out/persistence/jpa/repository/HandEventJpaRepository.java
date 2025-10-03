package dev.xiyo.pokerhole.adapter.out.persistence.jpa.repository;

import dev.xiyo.pokerhole.adapter.out.persistence.jpa.entity.HandEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * JPA Repository for HandEventEntity.
 *
 * Provides query methods for hand event persistence and retrieval.
 *
 * Query patterns supported:
 * 1. Find events by hand ID (ordered by sequence for replay)
 * 2. Find events by hand ID and event type
 * 3. Find events by player ID
 * 4. Find events by player ID and event type
 * 5. Find events by time range
 */
@Repository
public interface HandEventJpaRepository extends JpaRepository<HandEventEntity, UUID> {

    /**
     * Find all events for a specific hand, ordered by sequence.
     * This is the primary query for event replay.
     *
     * Uses composite index: idx_hand_events_hand_sequence
     *
     * @param handId the hand ID
     * @return list of events ordered by event_sequence ASC
     */
    @Query("SELECT e FROM HandEventEntity e WHERE e.handHistory.handId = :handId ORDER BY e.eventSequence ASC")
    List<HandEventEntity> findByHandIdOrderByEventSequence(@Param("handId") UUID handId);

    /**
     * Find all events of a specific type for a specific hand.
     *
     * @param handId the hand ID
     * @param eventType the event type
     * @return list of events ordered by event_sequence ASC
     */
    @Query("SELECT e FROM HandEventEntity e WHERE e.handHistory.handId = :handId AND e.eventType = :eventType ORDER BY e.eventSequence ASC")
    List<HandEventEntity> findByHandIdAndEventType(@Param("handId") UUID handId, @Param("eventType") String eventType);

    /**
     * Find all events triggered by a specific player.
     *
     * Uses index: idx_hand_events_player_id
     *
     * @param playerId the player ID
     * @return list of events ordered by occurred_at DESC
     */
    List<HandEventEntity> findByPlayerIdOrderByOccurredAtDesc(UUID playerId);

    /**
     * Find all events of a specific type triggered by a specific player.
     *
     * Uses composite index: idx_hand_events_player_type
     *
     * @param playerId the player ID
     * @param eventType the event type
     * @return list of events ordered by occurred_at DESC
     */
    List<HandEventEntity> findByPlayerIdAndEventTypeOrderByOccurredAtDesc(UUID playerId, String eventType);

    /**
     * Find all events that occurred within a specific time range.
     *
     * Uses index: idx_hand_events_occurred_at
     *
     * @param startTime the start of the time range
     * @param endTime the end of the time range
     * @return list of events ordered by occurred_at DESC
     */
    List<HandEventEntity> findByOccurredAtBetweenOrderByOccurredAtDesc(Instant startTime, Instant endTime);

    /**
     * Find all events of a specific type.
     *
     * Uses index: idx_hand_events_type
     *
     * @param eventType the event type
     * @return list of events ordered by occurred_at DESC
     */
    List<HandEventEntity> findByEventTypeOrderByOccurredAtDesc(String eventType);

    /**
     * Count events for a specific hand.
     *
     * @param handId the hand ID
     * @return count of events
     */
    @Query("SELECT COUNT(e) FROM HandEventEntity e WHERE e.handHistory.handId = :handId")
    long countByHandId(@Param("handId") UUID handId);

    /**
     * Count events of a specific type for a specific hand.
     *
     * @param handId the hand ID
     * @param eventType the event type
     * @return count of events
     */
    @Query("SELECT COUNT(e) FROM HandEventEntity e WHERE e.handHistory.handId = :handId AND e.eventType = :eventType")
    long countByHandIdAndEventType(@Param("handId") UUID handId, @Param("eventType") String eventType);

    /**
     * Find the last event for a specific hand.
     *
     * @param handId the hand ID
     * @return the last event or null
     */
    @Query("SELECT e FROM HandEventEntity e WHERE e.handHistory.handId = :handId ORDER BY e.eventSequence DESC LIMIT 1")
    HandEventEntity findLastEventByHandId(@Param("handId") UUID handId);

    /**
     * Find events by hand ID with JSONB query.
     * Example: Find all RAISE actions with amount > 100
     *
     * Uses GIN index: idx_hand_events_data
     *
     * @param handId the hand ID
     * @param jsonbQuery the JSONB query (e.g., '{"action": "RAISE"}')
     * @return list of events ordered by event_sequence ASC
     */
    @Query(value = "SELECT * FROM hand_events WHERE hand_id = :handId AND event_data @> CAST(:jsonbQuery AS jsonb) ORDER BY event_sequence ASC", nativeQuery = true)
    List<HandEventEntity> findByHandIdAndEventDataContains(
        @Param("handId") UUID handId,
        @Param("jsonbQuery") String jsonbQuery
    );
}
