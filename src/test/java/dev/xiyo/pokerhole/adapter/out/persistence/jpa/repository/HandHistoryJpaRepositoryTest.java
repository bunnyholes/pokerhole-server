package dev.xiyo.pokerhole.adapter.out.persistence.jpa.repository;

import dev.xiyo.pokerhole.adapter.out.persistence.jpa.entity.HandEventEntity;
import dev.xiyo.pokerhole.adapter.out.persistence.jpa.entity.HandHistoryEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for HandHistoryJpaRepository.
 *
 * Tests repository methods, JSONB queries, and database constraints.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class HandHistoryJpaRepositoryTest {

    @Autowired
    private HandHistoryJpaRepository handHistoryRepository;

    @Autowired
    private HandEventJpaRepository handEventRepository;

    @Autowired
    private TestEntityManager entityManager;

    private UUID gameId1;
    private UUID gameId2;
    private UUID player1Id;
    private UUID player2Id;
    private UUID player3Id;

    @BeforeEach
    void setUp() {
        // Clean up
        handEventRepository.deleteAll();
        handHistoryRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();

        // Initialize test data
        gameId1 = UUID.randomUUID();
        gameId2 = UUID.randomUUID();
        player1Id = UUID.randomUUID();
        player2Id = UUID.randomUUID();
        player3Id = UUID.randomUUID();
    }

    @Test
    @DisplayName("Should save and retrieve HandHistoryEntity")
    void shouldSaveAndRetrieveHandHistory() {
        // Given
        HandHistoryEntity handHistory = createHandHistory(gameId1, 1, player1Id);

        // When
        HandHistoryEntity saved = handHistoryRepository.save(handHistory);
        entityManager.flush();
        entityManager.clear();

        Optional<HandHistoryEntity> retrieved = handHistoryRepository.findById(saved.getHandId());

        // Then
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getHandId()).isEqualTo(saved.getHandId());
        assertThat(retrieved.get().getGameId()).isEqualTo(gameId1);
        assertThat(retrieved.get().getHandNumber()).isEqualTo(1);
        assertThat(retrieved.get().getDeckSeed()).isEqualTo(12345L);
        assertThat(retrieved.get().getWinnerIds()).containsExactly(player1Id);
    }

    @Test
    @DisplayName("Should save HandHistoryEntity with multiple winners (split pot)")
    void shouldSaveHandHistoryWithMultipleWinners() {
        // Given
        List<UUID> winners = List.of(player1Id, player2Id);
        HandHistoryEntity handHistory = createHandHistory(gameId1, 1, winners);

        // When
        HandHistoryEntity saved = handHistoryRepository.save(handHistory);
        entityManager.flush();
        entityManager.clear();

        Optional<HandHistoryEntity> retrieved = handHistoryRepository.findById(saved.getHandId());

        // Then
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getWinnerIds()).hasSize(2);
        assertThat(retrieved.get().getWinnerIds()).containsExactlyInAnyOrder(player1Id, player2Id);
    }

    @Test
    @DisplayName("Should find hands by game ID ordered by hand number descending")
    void shouldFindByGameIdOrderByHandNumberDesc() {
        // Given
        HandHistoryEntity hand1 = createHandHistory(gameId1, 1, player1Id);
        HandHistoryEntity hand2 = createHandHistory(gameId1, 2, player2Id);
        HandHistoryEntity hand3 = createHandHistory(gameId1, 3, player3Id);
        HandHistoryEntity hand4 = createHandHistory(gameId2, 1, player1Id); // Different game

        handHistoryRepository.saveAll(List.of(hand1, hand2, hand3, hand4));
        entityManager.flush();
        entityManager.clear();

        // When
        List<HandHistoryEntity> result = handHistoryRepository.findByGameIdOrderByHandNumberDesc(gameId1);

        // Then
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getHandNumber()).isEqualTo(3);
        assertThat(result.get(1).getHandNumber()).isEqualTo(2);
        assertThat(result.get(2).getHandNumber()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should find hand by game ID and hand number")
    void shouldFindByGameIdAndHandNumber() {
        // Given
        HandHistoryEntity hand1 = createHandHistory(gameId1, 1, player1Id);
        HandHistoryEntity hand2 = createHandHistory(gameId1, 2, player2Id);

        handHistoryRepository.saveAll(List.of(hand1, hand2));
        entityManager.flush();
        entityManager.clear();

        // When
        Optional<HandHistoryEntity> result = handHistoryRepository.findByGameIdAndHandNumber(gameId1, 2);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getHandNumber()).isEqualTo(2);
        assertThat(result.get().getWinnerIds()).containsExactly(player2Id);
    }

    @Test
    @DisplayName("Should find hands won by a specific player using JSONB query")
    void shouldFindByWinnerIdsContaining() {
        // Given
        HandHistoryEntity hand1 = createHandHistory(gameId1, 1, player1Id);
        HandHistoryEntity hand2 = createHandHistory(gameId1, 2, player2Id);
        HandHistoryEntity hand3 = createHandHistory(gameId1, 3, List.of(player1Id, player2Id)); // Split pot

        handHistoryRepository.saveAll(List.of(hand1, hand2, hand3));
        entityManager.flush();
        entityManager.clear();

        // When
        List<HandHistoryEntity> player1Wins = handHistoryRepository.findByWinnerIdsContaining(player1Id);
        List<HandHistoryEntity> player2Wins = handHistoryRepository.findByWinnerIdsContaining(player2Id);
        List<HandHistoryEntity> player3Wins = handHistoryRepository.findByWinnerIdsContaining(player3Id);

        // Then
        assertThat(player1Wins).hasSize(2); // hand1 and hand3
        assertThat(player2Wins).hasSize(2); // hand2 and hand3
        assertThat(player3Wins).isEmpty();
    }

    @Test
    @DisplayName("Should find ongoing hands (ended_at IS NULL)")
    void shouldFindOngoingHands() {
        // Given
        HandHistoryEntity ongoingHand = createHandHistory(gameId1, 1, null);
        ongoingHand.setEndedAt(null);
        ongoingHand.setFinalPot(null);
        ongoingHand.setWinnerIds(new ArrayList<>());

        HandHistoryEntity completedHand = createHandHistory(gameId1, 2, player1Id);
        completedHand.setEndedAt(Instant.now());
        completedHand.setFinalPot(1000L);

        handHistoryRepository.saveAll(List.of(ongoingHand, completedHand));
        entityManager.flush();
        entityManager.clear();

        // When
        List<HandHistoryEntity> ongoingHands = handHistoryRepository.findByGameIdAndEndedAtIsNull(gameId1);

        // Then
        assertThat(ongoingHands).hasSize(1);
        assertThat(ongoingHands.get(0).getEndedAt()).isNull();
        assertThat(ongoingHands.get(0).getFinalPot()).isNull();
    }

    @Test
    @DisplayName("Should find hands by time range")
    void shouldFindByStartedAtBetween() {
        // Given
        Instant now = Instant.now();
        Instant oneHourAgo = now.minusSeconds(3600);
        Instant twoHoursAgo = now.minusSeconds(7200);

        HandHistoryEntity hand1 = createHandHistory(gameId1, 1, player1Id);
        hand1.setStartedAt(twoHoursAgo);

        HandHistoryEntity hand2 = createHandHistory(gameId1, 2, player2Id);
        hand2.setStartedAt(oneHourAgo);

        HandHistoryEntity hand3 = createHandHistory(gameId1, 3, player3Id);
        hand3.setStartedAt(now);

        handHistoryRepository.saveAll(List.of(hand1, hand2, hand3));
        entityManager.flush();
        entityManager.clear();

        // When
        List<HandHistoryEntity> lastHourHands = handHistoryRepository.findByStartedAtBetweenOrderByStartedAtDesc(
            oneHourAgo.minusSeconds(1), now.plusSeconds(1)
        );

        // Then
        assertThat(lastHourHands).hasSize(2);
        assertThat(lastHourHands.get(0).getStartedAt()).isAfter(lastHourHands.get(1).getStartedAt());
    }

    @Test
    @DisplayName("Should count hands by game ID")
    void shouldCountByGameId() {
        // Given
        HandHistoryEntity hand1 = createHandHistory(gameId1, 1, player1Id);
        HandHistoryEntity hand2 = createHandHistory(gameId1, 2, player2Id);
        HandHistoryEntity hand3 = createHandHistory(gameId2, 1, player3Id);

        handHistoryRepository.saveAll(List.of(hand1, hand2, hand3));
        entityManager.flush();
        entityManager.clear();

        // When
        long game1Count = handHistoryRepository.countByGameId(gameId1);
        long game2Count = handHistoryRepository.countByGameId(gameId2);

        // Then
        assertThat(game1Count).isEqualTo(2);
        assertThat(game2Count).isEqualTo(1);
    }

    @Test
    @DisplayName("Should find most recent hand")
    void shouldFindFirstByGameIdOrderByHandNumberDesc() {
        // Given
        HandHistoryEntity hand1 = createHandHistory(gameId1, 1, player1Id);
        HandHistoryEntity hand2 = createHandHistory(gameId1, 2, player2Id);
        HandHistoryEntity hand3 = createHandHistory(gameId1, 3, player3Id);

        handHistoryRepository.saveAll(List.of(hand1, hand2, hand3));
        entityManager.flush();
        entityManager.clear();

        // When
        Optional<HandHistoryEntity> mostRecentHand = handHistoryRepository.findFirstByGameIdOrderByHandNumberDesc(gameId1);

        // Then
        assertThat(mostRecentHand).isPresent();
        assertThat(mostRecentHand.get().getHandNumber()).isEqualTo(3);
    }

    @Test
    @DisplayName("Should cascade delete events when hand history is deleted")
    void shouldCascadeDeleteEvents() {
        // Given
        HandHistoryEntity handHistory = createHandHistory(gameId1, 1, player1Id);
        handHistoryRepository.save(handHistory);

        HandEventEntity event1 = createEvent(handHistory, 1, "PlayerActed", player1Id);
        HandEventEntity event2 = createEvent(handHistory, 2, "RoundProgressed", null);
        handHistory.addEvent(event1);
        handHistory.addEvent(event2);

        handHistoryRepository.save(handHistory);
        entityManager.flush();
        entityManager.clear();

        UUID handId = handHistory.getHandId();

        // When
        handHistoryRepository.deleteById(handId);
        entityManager.flush();
        entityManager.clear();

        // Then
        assertThat(handHistoryRepository.findById(handId)).isEmpty();
        assertThat(handEventRepository.findByHandIdOrderByEventSequence(handId)).isEmpty();
    }

    // Helper methods

    private HandHistoryEntity createHandHistory(UUID gameId, int handNumber, UUID winnerId) {
        return createHandHistory(gameId, handNumber, winnerId != null ? List.of(winnerId) : new ArrayList<>());
    }

    private HandHistoryEntity createHandHistory(UUID gameId, int handNumber, List<UUID> winnerIds) {
        return HandHistoryEntity.builder()
            .handId(UUID.randomUUID())
            .gameId(gameId)
            .handNumber(handNumber)
            .startedAt(Instant.now())
            .endedAt(Instant.now())
            .finalPot(1000L)
            .winnerIds(new ArrayList<>(winnerIds))
            .deckSeed(12345L)
            .dealerButtonPosition(0)
            .smallBlind(50L)
            .bigBlind(100L)
            .createdAt(Instant.now())
            .events(new ArrayList<>())
            .build();
    }

    private HandEventEntity createEvent(HandHistoryEntity handHistory, int sequence, String eventType, UUID playerId) {
        return HandEventEntity.builder()
            .eventId(UUID.randomUUID())
            .handHistory(handHistory)
            .eventSequence(sequence)
            .eventType(eventType)
            .playerId(playerId)
            .eventData("{\"action\":\"CALL\",\"amount\":100}")
            .occurredAt(Instant.now())
            .build();
    }
}
