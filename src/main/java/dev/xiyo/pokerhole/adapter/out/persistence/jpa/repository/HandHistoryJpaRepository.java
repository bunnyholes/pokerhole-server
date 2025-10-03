package dev.xiyo.pokerhole.adapter.out.persistence.jpa.repository;

import dev.xiyo.pokerhole.adapter.out.persistence.jpa.entity.HandHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA Repository for HandHistoryEntity.
 *
 * Provides query methods for hand history persistence and retrieval.
 *
 * Query patterns supported:
 * 1. Find hands by game ID
 * 2. Find hands by game ID and hand number
 * 3. Find hands won by a specific player (JSONB query)
 * 4. Find ongoing hands (ended_at IS NULL)
 * 5. Find hands by date range
 */
@Repository
public interface HandHistoryJpaRepository extends JpaRepository<HandHistoryEntity, UUID> {

    /**
     * Find all hands for a specific game.
     * Ordered by started_at descending (most recent first).
     *
     * @param gameId the game ID
     * @return list of hand histories
     */
    List<HandHistoryEntity> findByGameIdOrderByStartedAtDesc(UUID gameId);

    /**
     * Find all hands for a specific game.
     * Ordered by hand number descending (most recent first).
     *
     * @param gameId the game ID
     * @return list of hand histories
     */
    List<HandHistoryEntity> findByGameIdOrderByHandNumberDesc(UUID gameId);

    /**
     * Find a specific hand by game ID and hand number.
     *
     * @param gameId the game ID
     * @param handNumber the hand number
     * @return optional hand history
     */
    Optional<HandHistoryEntity> findByGameIdAndHandNumber(UUID gameId, Integer handNumber);

    /**
     * Find all hands won by a specific player.
     * Uses PostgreSQL JSONB @> operator for containment query.
     *
     * Example SQL: WHERE winner_ids @> '["uuid"]'::jsonb
     *
     * @param playerId the player ID
     * @return list of hand histories where player was a winner
     */
    @Query(value = "SELECT h FROM HandHistoryEntity h WHERE CAST(FUNCTION('jsonb_contains', h.winnerIds, CAST(:playerId AS string)) AS boolean) = true")
    List<HandHistoryEntity> findByWinnerIdsContaining(@Param("playerId") UUID playerId);

    /**
     * Find ongoing hands for a specific game (ended_at IS NULL).
     *
     * @param gameId the game ID
     * @return list of ongoing hand histories
     */
    List<HandHistoryEntity> findByGameIdAndEndedAtIsNull(UUID gameId);

    /**
     * Find hands that started within a specific time range.
     *
     * @param startTime the start of the time range
     * @param endTime the end of the time range
     * @return list of hand histories
     */
    List<HandHistoryEntity> findByStartedAtBetweenOrderByStartedAtDesc(Instant startTime, Instant endTime);

    /**
     * Count total hands for a specific game.
     *
     * @param gameId the game ID
     * @return count of hands
     */
    long countByGameId(UUID gameId);

    /**
     * Find the most recent hand for a specific game.
     *
     * @param gameId the game ID
     * @return optional hand history
     */
    Optional<HandHistoryEntity> findFirstByGameIdOrderByHandNumberDesc(UUID gameId);

    /**
     * Find hands by game ID with pagination.
     * Uses native query for better performance with JSONB.
     *
     * @param gameId the game ID
     * @param limit the maximum number of results
     * @param offset the number of results to skip
     * @return list of hand histories
     */
    @Query(value = "SELECT * FROM hand_history WHERE game_id = :gameId ORDER BY hand_number DESC LIMIT :limit OFFSET :offset", nativeQuery = true)
    List<HandHistoryEntity> findByGameIdWithPagination(
        @Param("gameId") UUID gameId,
        @Param("limit") int limit,
        @Param("offset") int offset
    );
}
